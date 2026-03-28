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
    /** 国标 GB/T 2260 省级行政区划代码（adcode，如 110000），与前端省市区数据源及地图一致 */
    private Long provinceId;
    /** 市级 adcode（如 110100），未选到市级时可为 null */
    private Long cityId;
    /** 区县级 adcode（如 110101），未选到区县时可为 null */
    private Long countyId;
}
