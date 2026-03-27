package com.wanshu.model.entity.emp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 快递员作业范围
 */
@Data
@TableName("emp_courier_scope")
public class EmpCourierScope implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long courierId;
    private Long provinceId;
    private Long cityId;
    private Long countyId;
}
