package com.wanshu.model.entity.emp;

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
    private Long organId;
    private String idCard;
    private Integer workStatus;
}
