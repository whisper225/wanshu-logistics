-- 已有库缺少 light_throw_ratio 列时执行（执行一次即可）
ALTER TABLE base_economic_zone
    ADD COLUMN light_throw_ratio INT DEFAULT 6000 COMMENT '普快轻抛系数(cm³/kg)' AFTER provinces;
