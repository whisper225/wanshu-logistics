package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 车辆司机关联
 */
@Data
@TableName("base_vehicle_driver")
public class BaseVehicleDriver implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long vehicleId;
    private Long driverId;
}
