package com.wanshu.model.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 派件作业
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_delivery_task")
public class BizDeliveryTask extends BaseEntity {

    private String taskNumber;
    private Long waybillId;
    private Long courierId;
    private LocalDateTime assignTime;
    private LocalDateTime deliveryTime;
    private Integer signType;
    private String signImage;
    private Integer status;
    private String rejectReason;
    private String remark;
}
