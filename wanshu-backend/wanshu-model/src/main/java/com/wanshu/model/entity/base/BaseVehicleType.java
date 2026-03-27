package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 车型
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_vehicle_type")
public class BaseVehicleType extends BaseEntity {

    private String typeNumber;
    private String typeName;
    private Integer loadWeight;
    private Integer loadVolume;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
}
