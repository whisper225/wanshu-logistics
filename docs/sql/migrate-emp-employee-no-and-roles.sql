-- 已有库升级：快递员工号列 + 快递员/司机角色（与 init-admin-data 中 id=2,3 一致）
-- 执行：mysql -u root -p wanshu_logistics < docs/sql/migrate-emp-employee-no-and-roles.sql

USE `wanshu_logistics`;

-- emp_courier：增加工号（若列已存在，跳过本句或执行前检查 INFORMATION_SCHEMA.COLUMNS）
ALTER TABLE `emp_courier`
    ADD COLUMN `employee_no` VARCHAR(50) DEFAULT NULL COMMENT '工号（业务编号，非身份证）' AFTER `organ_id`;

-- 角色：快递员、司机（若已存在 uk_role_code 冲突则跳过或改 id）
INSERT IGNORE INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `login_scope`, `status`, `deleted`, `created_time`, `updated_time`)
VALUES
    (2, 'Courier', 'COURIER', '快递员', 'courier', 1, 0, NOW(), NOW()),
    (3, 'Driver', 'DRIVER', '司机', 'driver', 1, 0, NOW(), NOW());
