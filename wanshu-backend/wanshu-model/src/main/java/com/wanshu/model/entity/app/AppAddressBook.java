package com.wanshu.model.entity.app;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 地址簿
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_address_book")
public class AppAddressBook extends BaseEntity {

    private Long userId;
    private String name;
    private String phone;
    private Long provinceId;
    private Long cityId;
    private Long countyId;
    private String address;
    private Integer isDefault;
}
