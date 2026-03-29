-- 线路类型精简迁移：将 5 种类型统一为 3 种（干线/支线/接驳路线）
-- 请勿修改 docs/sql/schema.sql，对已有库单独执行本脚本
--
-- 变更说明：
--   接驳路线 type=5 → 3（原 3=专线、4=临时线路 废弃，接驳路线从 5 降编为 3）
--   专线(3)、临时线路(4) 设停用（保留历史数据，不物理删除）

-- 1. 若已有 type=3（专线）或 type=4（临时线路）的线路，先设为停用
UPDATE dispatch_line SET status = 0 WHERE line_type IN (3, 4) AND deleted = 0;

-- 2. 将接驳路线从 type=5 更新为 type=3（接驳路线的新编号）
UPDATE dispatch_line SET line_type = 3 WHERE line_type = 5;
