package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 机构
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_organ")
public class BaseOrgan extends BaseEntity {

    private Long parentId;
    private String organName;
    private Integer organType;
    private Long provinceId;
    private Long cityId;
    private Long countyId;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String managerName;
    private String managerPhone;
    private String contactName;
    private String contactPhone;
    private Integer sortOrder;
    private Integer status;
}
