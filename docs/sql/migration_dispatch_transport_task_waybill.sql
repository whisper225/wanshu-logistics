-- 运输任务与运单关联表
-- 对应实体：DispatchTransportTaskWaybill
-- 一个运输任务（dispatch_transport_task）可承载多个运单（biz_waybill）

CREATE TABLE IF NOT EXISTS `dispatch_transport_task_waybill`
(
    `id`                BIGINT      NOT NULL COMMENT '主键（雪花算法）',
    `transport_task_id` BIGINT      NOT NULL COMMENT '运输任务ID',
    `waybill_id`        BIGINT      NOT NULL COMMENT '运单ID',
    `created_time`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_transport_task_id` (`transport_task_id`),
    KEY `idx_waybill_id` (`waybill_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '运输任务-运单关联';
