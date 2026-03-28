package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 经济区定义
 */
@Data
@TableName("base_economic_zone")
public class BaseEconomicZone implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String zoneName;
    private String provinces;
    /**
     * 普快轻抛系数 cm³/kg（经济区互寄按区配置）。
     * 库表已执行 ALTER 增加列后，应去掉 {@code exist = false} 以便读写该列。
     */
    @TableField(exist = false)
    private Integer lightThrowRatio;
}
