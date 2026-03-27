package com.wanshu.model.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 取件作业
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_pickup_task")
public class BizPickupTask extends BaseEntity {

    private String taskNumber;
    private Long orderId;
    private Long waybillId;
    private Long courierId;
    private LocalDateTime assignTime;
    private LocalDateTime pickupTime;
    private Integer status;
    private String cancelReason;
    private String remark;
}
