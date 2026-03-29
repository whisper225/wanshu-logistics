-- 全局调度配置表：按路线类型（干线/支线/专线）的默认每公里成本
-- 与 docs/sql/schema.sql 中原有 dispatch_config 并存，请勿修改原建表文件
-- 若列已存在请删除对应 ADD COLUMN 后再执行

ALTER TABLE dispatch_config
    ADD COLUMN cost_per_km_type1 DECIMAL(12, 4) DEFAULT NULL COMMENT '干线(线路类型1)默认每公里成本(元)' AFTER priority_second,
    ADD COLUMN cost_per_km_type2 DECIMAL(12, 4) DEFAULT NULL COMMENT '支线(线路类型2)默认每公里成本(元)' AFTER cost_per_km_type1,
    ADD COLUMN cost_per_km_type3 DECIMAL(12, 4) DEFAULT NULL COMMENT '专线(线路类型3)默认每公里成本(元)' AFTER cost_per_km_type2;
