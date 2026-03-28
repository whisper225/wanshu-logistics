-- 运费模块：经济区轻抛系数 + 模板经济区关联表（在已有库上执行）

ALTER TABLE base_economic_zone
    ADD COLUMN light_throw_ratio INT DEFAULT 6000 COMMENT '普快轻抛系数(cm³/kg)' AFTER provinces;

CREATE TABLE IF NOT EXISTS base_freight_template_economic_zone (
    id                BIGINT NOT NULL COMMENT '主键ID',
    template_id       BIGINT NOT NULL COMMENT '运费模板ID',
    economic_zone_id  BIGINT NOT NULL COMMENT '经济区ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_economic_zone (economic_zone_id),
    KEY idx_template (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运费模板经济区关联';

-- 示例经济区（可按实际调整 ID；若已存在请改为 UPDATE）
INSERT INTO base_economic_zone (id, zone_name, provinces, light_throw_ratio) VALUES
(1900000000000000001, '京津冀', '11,12,13', 6000),
(1900000000000000002, '江浙沪', '31,32,33', 6000),
(1900000000000000003, '黑吉辽', '21,22,23', 12000),
(1900000000000000004, '川渝', '50,51', 8000)
ON DUPLICATE KEY UPDATE zone_name = VALUES(zone_name), light_throw_ratio = VALUES(light_throw_ratio);
