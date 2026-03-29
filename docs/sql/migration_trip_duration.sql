-- 车次表新增「预计行驶时长（分钟）」列
-- 请在已有库上单独执行本脚本（勿修改 schema.sql）
ALTER TABLE dispatch_trip
    ADD COLUMN duration_minutes INT NULL COMMENT '预计行驶时长（分钟）' AFTER depart_time;
