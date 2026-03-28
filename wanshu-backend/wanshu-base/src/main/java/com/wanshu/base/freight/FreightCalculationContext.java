package com.wanshu.base.freight;

import com.wanshu.model.entity.base.BaseFreightTemplate;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 运费职责链共享上下文：入参、中间量、结果。
 */
@Data
public class FreightCalculationContext {

    private Long templateId;
    /** 实重 kg */
    private BigDecimal actualWeight;
    /** 体积 cm³：由长×宽×高或直传 volume */
    private BigDecimal volumeCm3;

    private BaseFreightTemplate template;
    /** 最终采用的轻抛系数 */
    private int lightThrowRatio;
    /** 体积重 kg */
    private BigDecimal volumeWeight;
    /** max(实重, 体积重)，进位前 */
    private BigDecimal billableWeightRaw;
    /** 计费重量 kg（按规则进位后） */
    private BigDecimal billableWeight;
    /** 运费（进位总运费前） */
    private BigDecimal freightAmount;
    /** 总运费，保留 1 位小数 */
    private BigDecimal freightTotal;

    private final Map<String, Object> extras = new HashMap<>();

    public void putExtra(String key, Object value) {
        extras.put(key, value);
    }

    public Map<String, Object> toResultMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("freight", freightTotal);
        m.put("billableWeight", billableWeight);
        m.put("billableWeightRaw", billableWeightRaw);
        m.put("volumeWeight", volumeWeight);
        m.put("lightThrowRatio", lightThrowRatio);
        m.put("templateName", template != null ? template.getTemplateName() : null);
        m.put("templateId", templateId);
        m.putAll(extras);
        return m;
    }
}
