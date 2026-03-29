package com.wanshu.model.entity.dispatch;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 车次
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_trip")
public class DispatchTrip extends BaseEntity {

    private String tripNumber;
    private String tripName;
    private Long lineId;
    private Integer periodType;
    private String departDay;
    private LocalTime departTime;
    /** 预计行驶时长（分钟） */
    private Integer durationMinutes;
    private Integer status;
}
