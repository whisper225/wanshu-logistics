package com.wanshu.base.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wanshu.model.entity.base.BaseFreightTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 创建/更新运费模板：经济区互寄可传多个经济区 ID（共用一个模板）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FreightTemplatePayload extends BaseFreightTemplate {

    /** templateType=4 时有效；同一经济区只能出现在一个模板下 */
    @TableField(exist = false)
    private List<Long> economicZoneIds;
}
