-- 为 sys_user 表添加微信登录字段
-- 兼容 MySQL 5.7 / 8.0（通过 information_schema 判断列是否存在）

DROP PROCEDURE IF EXISTS add_wx_columns;

DELIMITER $$
CREATE PROCEDURE add_wx_columns()
BEGIN
    -- 添加 wx_openid 列
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'sys_user'
          AND COLUMN_NAME  = 'wx_openid'
    ) THEN
        ALTER TABLE sys_user
            ADD COLUMN wx_openid VARCHAR(100) DEFAULT NULL COMMENT '微信小程序 openid' AFTER last_login_ip;
    END IF;

    -- 添加 wx_unionid 列
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'sys_user'
          AND COLUMN_NAME  = 'wx_unionid'
    ) THEN
        ALTER TABLE sys_user
            ADD COLUMN wx_unionid VARCHAR(100) DEFAULT NULL COMMENT '微信 unionid' AFTER wx_openid;
    END IF;

    -- 添加唯一索引（若不存在）
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'sys_user'
          AND INDEX_NAME   = 'uk_wx_openid'
    ) THEN
        ALTER TABLE sys_user
            ADD UNIQUE INDEX uk_wx_openid (wx_openid);
    END IF;
END$$
DELIMITER ;

CALL add_wx_columns();
DROP PROCEDURE IF EXISTS add_wx_columns;
