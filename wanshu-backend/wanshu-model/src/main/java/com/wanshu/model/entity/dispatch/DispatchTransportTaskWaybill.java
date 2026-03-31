package com.wanshu.model.entity.dispatch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运输任务与运单的关联（一个运输任务可承载多个运单）
 */
@Data
@TableName("dispatch_transport_task_waybill")
public class DispatchTransportTaskWaybill implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long transportTaskId;
    private Long waybillId;
    private LocalDateTime createdTime;
}
