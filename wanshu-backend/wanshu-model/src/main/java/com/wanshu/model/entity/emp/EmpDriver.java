package com.wanshu.model.entity.emp;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 司机扩展信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("emp_driver")
public class EmpDriver extends BaseEntity {

    private Long organId;
    private String vehicleTypes;
    private String licenseImage;
    private Integer workStatus;
}
