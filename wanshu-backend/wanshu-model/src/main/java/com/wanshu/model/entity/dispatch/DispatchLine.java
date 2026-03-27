package com.wanshu.model.entity.dispatch;

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
    private Long estimatedTime;
    private Integer status;
}
