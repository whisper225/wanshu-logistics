-- 线路表扩展：三条路线每公里成本、调度配置 JSON、地图折线
--
-- 说明：请勿在 docs/sql/schema.sql 中改历史建表语句来追加列，以免全量脚本含 DROP TABLE 时误伤数据；
--       已有库请单独执行本脚本。若某列已存在，删除对应 ADD COLUMN 行后再执行。
--

ALTER TABLE dispatch_line
    ADD COLUMN cost_per_km_1 DECIMAL(12, 4) DEFAULT NULL COMMENT '路线1每公里平均成本(元)' AFTER cost,
    ADD COLUMN cost_per_km_2 DECIMAL(12, 4) DEFAULT NULL COMMENT '路线2每公里平均成本(元)' AFTER cost_per_km_1,
    ADD COLUMN cost_per_km_3 DECIMAL(12, 4) DEFAULT NULL COMMENT '路线3每公里平均成本(元)' AFTER cost_per_km_2,
    ADD COLUMN dispatch_config TEXT DEFAULT NULL COMMENT '调度配置JSON' AFTER cost_per_km_3,
    ADD COLUMN map_polyline TEXT DEFAULT NULL COMMENT '地图线路坐标JSON [[lng,lat],...]' AFTER dispatch_config;
