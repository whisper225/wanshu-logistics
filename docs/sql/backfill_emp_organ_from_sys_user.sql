-- 将快递员/司机扩展表中的 organ_id 与 sys_user.organ_id 对齐（在用户管理绑定机构后若列表仍为空，可执行一次）
UPDATE emp_courier c
INNER JOIN sys_user u ON c.id = u.id
SET c.organ_id = u.organ_id
WHERE u.organ_id IS NOT NULL AND (c.organ_id IS NULL OR c.organ_id <> u.organ_id);

UPDATE emp_driver d
INNER JOIN sys_user u ON d.id = u.id
SET d.organ_id = u.organ_id
WHERE u.organ_id IS NOT NULL AND (d.organ_id IS NULL OR d.organ_id <> u.organ_id);
