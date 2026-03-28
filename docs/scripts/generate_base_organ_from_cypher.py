# -*- coding: utf-8 -*-
"""
从 docs/full_backup.cypher 解析 AGENCY / OLT / TLT 节点，生成 base_organ 的 INSERT SQL。
用法: python docs/scripts/generate_base_organ_from_cypher.py
输出: docs/sql/base_organ_from_neo4j_backup.sql
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CYPHER = ROOT / "docs" / "full_backup.cypher"
OUT_SQL = ROOT / "docs" / "sql" / "base_organ_from_neo4j_backup.sql"

# 1=OLT 2=TLT 3=AGENCY；对应 UNWIND 行号（1-based）
LABEL_LINES = [
    (6, 3, "AGENCY"),
    (8, 1, "OLT"),
    (10, 2, "TLT"),
]


def sql_escape(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "''")


def parse_one_properties_chunk(chunk: str) -> dict | None:
    """chunk 为 properties 内部：address:... 到 ...status:true/false（不含外层括号）。"""
    chunk = chunk.strip()
    bid_m = re.search(r"bid:(\d+),\s*parentId:(\d+),\s*status:(true|false)", chunk)
    if not bid_m:
        return None
    bid = int(bid_m.group(1))
    parent_id = int(bid_m.group(2))
    status_bool = bid_m.group(3) == "true"
    name_m = re.search(r'name:"([^"]*)"', chunk)
    name = name_m.group(1).strip() if name_m else ""
    phone_m = re.search(r'phone:"([^"]*)"', chunk)
    phone = phone_m.group(1) if phone_m else ""
    mgr_m = re.search(r'managerName:"([^"]*)"', chunk)
    mgr = mgr_m.group(1) if mgr_m else ""
    loc_m = re.search(r"location:point\(\{x:\s*([\d.]+),\s*y:\s*([\d.]+)", chunk)
    lng = loc_m.group(1) if loc_m else None
    lat = loc_m.group(2) if loc_m else None

    province_id = city_id = county_id = None
    detail = ""
    addr_json_str = ""
    if 'address:"' in chunk and '", phone:' in chunk:
        raw = chunk.split('address:"', 1)[1].split('", phone:', 1)[0]
        addr_json_str = raw.replace('\\"', '"')
        try:
            addr_json = json.loads(addr_json_str)
            detail = str(addr_json.get("address") or "")
            pid = addr_json.get("province", {}).get("id")
            cid = addr_json.get("city", {}).get("id")
            coid = addr_json.get("county", {}).get("id")
            province_id = int(pid) if pid is not None else None
            city_id = int(cid) if cid is not None else None
            county_id = int(coid) if coid is not None else None
        except (json.JSONDecodeError, TypeError, ValueError):
            pass

    return {
        "bid": bid,
        "parent_id": parent_id,
        "organ_name": name,
        "phone": phone,
        "manager_name": mgr,
        "lng": lng,
        "lat": lat,
        "status": 1 if status_bool else 0,
        "address_detail": detail,
        "province_id": province_id,
        "city_id": city_id,
        "county_id": county_id,
    }


def extract_nodes(line: str) -> list[dict]:
    m = re.search(r"UNWIND\s*\[(.*)\]\s*AS\s*row\s*$", line, re.DOTALL)
    if not m:
        return []
    body = m.group(1)
    # 相邻节点之间：...status:true}}, {_id:10, properties:{...
    parts = re.split(r"\}\}\s*,\s*\{_id:\d+,\s*properties:\{", body)
    nodes: list[dict] = []
    for i, part in enumerate(parts):
        if i == 0:
            part = re.sub(r"^\[?\{_id:\d+,\s*properties:\{", "", part)
        else:
            part = re.sub(r"\}\s*\]\s*$", "", part)
            part = re.sub(r"\}\}\s*$", "", part)
        n = parse_one_properties_chunk(part)
        if n:
            nodes.append(n)
    return nodes


def row_to_sql(n: dict, organ_type: int, sort_order: int) -> str:
    lng, lat = n["lng"], n["lat"]
    lng_sql = "NULL" if lng is None else str(lng)
    lat_sql = "NULL" if lat is None else str(lat)

    def id_sql(v):
        return "NULL" if v is None else str(v)

    addr_text = n["address_detail"] or ""
    addr_sql = "'" + sql_escape(addr_text) + "'"

    return (
        f"({n['bid']}, {n['parent_id']}, '{sql_escape(n['organ_name'])}', {organ_type}, "
        f"{id_sql(n['province_id'])}, {id_sql(n['city_id'])}, {id_sql(n['county_id'])}, "
        f"{addr_sql}, {lng_sql}, {lat_sql}, "
        f"'{sql_escape(n['manager_name'])}', '{sql_escape(n['phone'])}', "
        f"NULL, NULL, {sort_order}, {n['status']}, 0, NOW(), NOW())"
    )


def main() -> int:
    lines = CYPHER.read_text(encoding="utf-8").splitlines()
    all_rows: list[tuple[dict, int, str]] = []
    for line_no, organ_type, label in LABEL_LINES:
        idx = line_no - 1
        if idx >= len(lines):
            print(f"Line {line_no} out of range", file=sys.stderr)
            return 1
        line = lines[idx]
        nodes = extract_nodes(line)
        for n in nodes:
            all_rows.append((n, organ_type, label))
        print(f"{label}: {len(nodes)} nodes")

    header = """-- ============================================================
-- 由 docs/full_backup.cypher 中 AGENCY/OLT/TLT 节点自动生成
-- id 使用 Neo4j 属性 bid，与图及业务主键一致；address 存 JSON 内 detail 文本
-- 执行前请确认 base_organ 无冲突主键；可先 TRUNCATE 或按需改库
-- mysql -u root -p wanshu_logistics < docs/sql/base_organ_from_neo4j_backup.sql
-- ============================================================

USE `wanshu_logistics`;

SET NAMES utf8mb4;

"""
    inserts = [
        "INSERT INTO `base_organ` (",
        "  `id`, `parent_id`, `organ_name`, `organ_type`,",
        "  `province_id`, `city_id`, `county_id`, `address`,",
        "  `longitude`, `latitude`, `manager_name`, `manager_phone`,",
        "  `contact_name`, `contact_phone`, `sort_order`, `status`, `deleted`,",
        "  `created_time`, `updated_time`",
        ") VALUES",
    ]
    values = []
    for sort_order, (n, organ_type, _) in enumerate(all_rows):
        values.append("  " + row_to_sql(n, organ_type, sort_order) + ",")
    if values:
        values[-1] = values[-1].rstrip(",") + ";"
    footer = "\n-- 共 {} 条机构\n".format(len(all_rows))

    OUT_SQL.parent.mkdir(parents=True, exist_ok=True)
    OUT_SQL.write_text(header + "\n".join(inserts) + "\n" + "\n".join(values) + "\n" + footer, encoding="utf-8")
    print(f"Wrote {OUT_SQL}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
