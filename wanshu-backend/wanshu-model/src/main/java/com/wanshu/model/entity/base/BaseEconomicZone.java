package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
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
}
