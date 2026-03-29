package com.wanshu.model.entity.emp;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wanshu.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 快递员扩展信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("emp_courier")
public class EmpCourier extends BaseEntity {

    private String employeeNo;
    /** 与 {@code sys_user.organ_id} 保持一致，由用户更新时同步 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long organId;
    private Integer workStatus;
}
