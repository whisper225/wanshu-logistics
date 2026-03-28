package com.wanshu.dispatch.sync;

import cn.hutool.json.JSONUtil;
import com.wanshu.common.event.OrganGraphSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 将 MySQL 机构同步为 Neo4j 节点（bid = base_organ.id，标签 OLT/TLT/AGENCY）。
 * <p>
 * 使用 {@link Session#executeWrite} 在单一 Neo4j 写事务内完成「删旧节点 + 建新节点」；不套用 Spring
 * {@code @Transactional}，避免与 JDBC 的 {@code PlatformTransactionManager}（已 {@code @Primary}）
 * 及 Neo4j 反应式事务管理器混用产生歧义。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganNeo4jSyncService {

    private final Driver neo4jDriver;

    public void upsert(OrganGraphSnapshot s) {
        String label = labelForOrganType(s.organType());
        if (label == null) {
            log.warn("跳过 Neo4j 同步：未知 organType={}, bid={}", s.organType(), s.id());
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("bid", s.id());
        params.put("name", s.organName());
        params.put("parentId", s.parentId() != null ? s.parentId() : 0L);
        params.put("phone", resolvePhone(s));
        params.put("address", buildAddressJson(s));
        params.put("managerName", s.managerName());
        params.put("status", s.status() != null && s.status() == 1);

        StringBuilder cypher = new StringBuilder("CREATE (n:`")
                .append(label)
                .append("` { bid: $bid, name: $name, parentId: $parentId, phone: $phone, ")
                .append("address: $address, managerName: $managerName, status: $status");

        BigDecimal lng = s.longitude();
        BigDecimal lat = s.latitude();
        if (lng != null && lat != null) {
            // Neo4j 5：仅 longitude/latitude 即可推断 WGS-84，避免部分版本对 crs 解析差异
            cypher.append(", location: point({longitude: $lng, latitude: $lat})");
            params.put("lng", lng.doubleValue());
            params.put("lat", lat.doubleValue());
        }
        cypher.append(" })");

        final String createCypher = cypher.toString();
        final Long bid = s.id();

        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH (n {bid: $bid}) DETACH DELETE n", Values.parameters("bid", bid));
                tx.run(createCypher, params);
                return null;
            });
            log.info("Neo4j 机构节点已写入, bid={}, label={}", bid, label);
        }
    }

    public void deleteByBid(Long organId) {
        if (organId == null) {
            return;
        }
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH (n {bid: $bid}) DETACH DELETE n", Values.parameters("bid", organId));
        }
    }

    private static String labelForOrganType(Integer organType) {
        if (organType == null) {
            return null;
        }
        return switch (organType) {
            case 1 -> "OLT";
            case 2 -> "TLT";
            case 3 -> "AGENCY";
            default -> null;
        };
    }

    private static String resolvePhone(OrganGraphSnapshot s) {
        if (StringUtils.hasText(s.managerPhone())) {
            return s.managerPhone();
        }
        return StringUtils.hasText(s.contactPhone()) ? s.contactPhone() : "";
    }

    private static String buildAddressJson(OrganGraphSnapshot s) {
        Map<String, Object> m = new HashMap<>();
        m.put("provinceId", s.provinceId());
        m.put("cityId", s.cityId());
        m.put("countyId", s.countyId());
        m.put("detail", s.address());
        return JSONUtil.toJsonStr(m);
    }
}
