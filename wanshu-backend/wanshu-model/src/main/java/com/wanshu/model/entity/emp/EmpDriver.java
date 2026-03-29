package com.wanshu.model.entity.emp;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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

    /** 与 {@code sys_user.organ_id} 保持一致，由用户更新时同步 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long organId;
    private String vehicleTypes;
    private String licenseImage;
    private Integer workStatus;
}
