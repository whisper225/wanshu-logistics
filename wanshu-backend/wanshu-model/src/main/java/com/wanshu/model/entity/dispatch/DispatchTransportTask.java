package com.wanshu.model.entity.dispatch;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运输任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_transport_task")
public class DispatchTransportTask extends BaseEntity {

    private String taskNumber;
    private Long tripId;
    private Long lineId;
    private Long vehicleId;
    private Long driverId;
    private Long startOrganId;
    private Long endOrganId;
    private LocalDateTime planDepartTime;
    private LocalDateTime actualDepartTime;
    private LocalDateTime planArriveTime;
    private LocalDateTime actualArriveTime;
    private BigDecimal loadWeight;
    private BigDecimal loadVolume;
    private Integer waybillCount;
    private Integer status;
}
