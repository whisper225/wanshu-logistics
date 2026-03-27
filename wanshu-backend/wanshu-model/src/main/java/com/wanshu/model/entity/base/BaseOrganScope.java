package com.wanshu.model.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 机构作业范围
 */
@Data
@TableName("base_organ_scope")
public class BaseOrganScope implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long organId;
    private Long provinceId;
    private Long cityId;
    private Long countyId;
}
