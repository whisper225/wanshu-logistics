package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 运费模板
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_freight_template")
public class BaseFreightTemplate extends BaseEntity {

    private String templateName;
    private Integer templateType;
    private BigDecimal firstWeight;
    private BigDecimal firstWeightPrice;
    private BigDecimal extraWeight;
    private BigDecimal extraWeightPrice;
    private Integer lightThrowRatio;
    private Integer status;
}
