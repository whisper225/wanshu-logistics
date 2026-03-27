package com.wanshu.model.entity.dispatch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 车次车辆关联
 */
@Data
@TableName("dispatch_trip_vehicle")
public class DispatchTripVehicle implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tripId;
    private Long vehicleId;
}
