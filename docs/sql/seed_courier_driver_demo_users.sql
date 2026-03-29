-- ============================================================
-- 演示账号：1 名快递员用户 + 1 名司机用户
-- 在已有库上单独执行（勿覆盖 schema.sql）
--
-- 前置条件：
--   1. 已存在角色 id=2（COURIER）、id=3（DRIVER），见 init-admin-data.sql
--   2. 若 id 或用户名冲突，请修改本脚本中的 id / username 后再执行
--
-- 登录信息（密码均为 123456，BCrypt 与 init-admin-data.sql 中 admin 一致）：
--   快递员  courier_demo  / 123456
--   司机    driver_demo   / 123456
--
-- Windows 示例：
--   mysql -u root -p wanshu_logistics < docs/sql/seed_courier_driver_demo_users.sql
-- ============================================================

USE `wanshu_logistics`;

-- 密码：$2b$12$MEc.ZGYbvDOe4Rxh5y.jLuI3nFKGZsrHBBGtQjDJXQ18yZjxiDrSq  => 123456
INSERT INTO `sys_user` (
    `id`, `username`, `password`, `real_name`, `phone`, `status`, `deleted`, `created_time`, `updated_time`
) VALUES
    (1900000000000000001, 'courier_demo', '$2b$12$MEc.ZGYbvDOe4Rxh5y.jLuI3nFKGZsrHBBGtQjDJXQ18yZjxiDrSq',
     '演示快递员', '13800000001', 1, 0, NOW(), NOW()),
    (1900000000000000002, 'driver_demo', '$2b$12$MEc.ZGYbvDOe4Rxh5y.jLuI3nFKGZsrHBBGtQjDJXQ18yZjxiDrSq',
     '演示司机', '13800000002', 1, 0, NOW(), NOW());

-- 角色：2=COURIER 快递员，3=DRIVER 司机
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
    (1900000000000000009, 1900000000000000001, 2),
    (1900000000000000010, 1900000000000000002, 3);

-- 快递员扩展（主键 = sys_user.id）
INSERT INTO `emp_courier` (
    `id`, `organ_id`, `employee_no`, `work_status`, `deleted`, `created_time`, `updated_time`
) VALUES
    (1900000000000000001, NULL, 'KD2026001001', 1, 0, NOW(), NOW());

-- 司机扩展（主键 = sys_user.id）
INSERT INTO `emp_driver` (
    `id`, `organ_id`, `vehicle_types`, `license_image`, `work_status`, `deleted`, `created_time`, `updated_time`
) VALUES
    (1900000000000000002, NULL, NULL, NULL, 1, 0, NOW(), NOW());
