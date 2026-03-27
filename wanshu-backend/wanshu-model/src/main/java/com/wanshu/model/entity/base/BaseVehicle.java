package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_vehicle")
public class BaseVehicle extends BaseEntity {

    private String vehicleNumber;
    private Long vehicleTypeId;
    private String licensePlate;
    private Integer loadWeight;
    private Integer loadVolume;
    private Long organId;
    private Integer status;
    private String licenseImage;
}
