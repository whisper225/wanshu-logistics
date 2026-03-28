package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 运费模板与经济区关联（多对多：多个经济区可共用一个模板；同一经济区只能绑定一个模板）。
 */
@Data
@TableName("base_freight_template_economic_zone")
public class BaseFreightTemplateEconomicZone implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long templateId;
    private Long economicZoneId;
}
