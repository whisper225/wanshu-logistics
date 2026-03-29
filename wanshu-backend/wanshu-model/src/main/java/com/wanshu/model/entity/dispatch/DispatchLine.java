package com.wanshu.model.entity.dispatch;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 线路
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dispatch_line")
public class DispatchLine extends BaseEntity {

    private String lineNumber;
    private String lineName;
    private Integer lineType;
    private Long startOrganId;
    private Long endOrganId;
    private BigDecimal distance;
    private BigDecimal cost;
    /** 三条备选路线的每公里平均成本（元），供成本测算与调度优先级使用 */
    private BigDecimal costPerKm1;
    private BigDecimal costPerKm2;
    private BigDecimal costPerKm3;
    /**
     * 调度配置 JSON，例如：
     * {"latestDispatchHour":2,"allocationScope":"SAME_DAY","priorityLevel1":"LEVEL1","priorityLevel2":"LEVEL2","tieBreakMinTransshipment":true,"tieBreakLowestCost":true}
     */
    private String dispatchConfig;
    /** 地图折线坐标 JSON：[[lng,lat],...] */
    private String mapPolyline;
    private Long estimatedTime;
    private Integer status;

    @TableField(exist = false)
    private String startOrganName;
    @TableField(exist = false)
    private String endOrganName;
}
