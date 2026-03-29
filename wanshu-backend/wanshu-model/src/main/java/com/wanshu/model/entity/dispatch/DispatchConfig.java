package com.wanshu.model.entity.dispatch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 调度配置
 */
@Data
@TableName("dispatch_config")
public class DispatchConfig implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Integer latestDispatchHour;
    private Integer maxAssignTime;
    private Integer priorityFirst;
    private Integer prioritySecond;
    /** 干线（线路类型 1）默认每公里成本（元） */
    private BigDecimal costPerKmType1;
    /** 支线（线路类型 2）默认每公里成本（元） */
    private BigDecimal costPerKmType2;
    /** 专线（线路类型 3）默认每公里成本（元） */
    private BigDecimal costPerKmType3;
    private Long organId;
    private LocalDateTime updatedTime;
}
