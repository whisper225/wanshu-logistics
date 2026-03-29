-- ============================================================
-- 管理端首个账号（空库时执行一次；若已存在 id=1 会主键冲突）
-- 登录: whsiper225 / 123456
-- 密码为 BCrypt（Python bcrypt 生成，与 Hutool BCrypt.checkpw 兼容）
--
-- Windows 建议在 cmd 中执行，避免 PowerShell 转义 $ 破坏密文:
--   mysql -u root -p wanshu_logistics < docs/sql/init-admin-data.sql
-- ============================================================

USE `wanshu_logistics`;

INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `login_scope`, `status`, `deleted`, `created_time`, `updated_time`)
VALUES (1, 'SuperAdmin', 'SUPER_ADMIN', 'bootstrap', 'admin', 1, 0, NOW(), NOW());

INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `login_scope`, `status`, `deleted`, `created_time`, `updated_time`)
VALUES
    (2, 'Courier', 'COURIER', '快递员', 'courier', 1, 0, NOW(), NOW()),
    (3, 'Driver', 'DRIVER', '司机', 'driver', 1, 0, NOW(), NOW());

INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `status`, `deleted`, `created_time`, `updated_time`)
VALUES (1, 'whsiper225', '$2b$12$MEc.ZGYbvDOe4Rxh5y.jLuI3nFKGZsrHBBGtQjDJXQ18yZjxiDrSq', 'Admin', 1, 0, NOW(), NOW());

INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`)
VALUES (1, 1, 1);
