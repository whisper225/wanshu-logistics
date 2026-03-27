package com.wanshu.model.entity.dispatch;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 回车登记
 */
@Data
@TableName("dispatch_return_register")
public class DispatchReturnRegister implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long transportTaskId;
    private Long vehicleId;
    private Long driverId;
    private LocalDateTime registerTime;
    private LocalDate registerDate;
    private String description;
    private String images;
    private LocalDateTime createdTime;
}
