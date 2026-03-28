package com.wanshu.base.freight.handler;

import com.wanshu.base.freight.AbstractFreightHandler;
import com.wanshu.base.freight.FreightCalculationContext;
import com.wanshu.base.freight.LightThrowDefaults;
import com.wanshu.base.mapper.BaseEconomicZoneMapper;
import com.wanshu.base.mapper.BaseFreightTemplateEconomicZoneMapper;
import com.wanshu.model.entity.base.BaseEconomicZone;
import com.wanshu.model.entity.base.BaseFreightTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 解析轻抛系数：优先模板手工配置；否则按类型/绑定经济区取默认或最小系数（经济区多绑定取 min，计费偏保守）。
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class ResolveLightThrowRatioHandler extends AbstractFreightHandler {

    private final BaseFreightTemplateEconomicZoneMapper templateZoneMapper;
    private final BaseEconomicZoneMapper economicZoneMapper;

    @Override
    public void handle(FreightCalculationContext ctx) {
        BaseFreightTemplate t = ctx.getTemplate();
        Integer manual = t.getLightThrowRatio();
        if (manual != null && manual > 0) {
            ctx.setLightThrowRatio(manual);
            forward(ctx);
            return;
        }
        int type = t.getTemplateType() != null ? t.getTemplateType() : 1;
        if (type != 4) {
            ctx.setLightThrowRatio(LightThrowDefaults.byTemplateType(type));
            forward(ctx);
            return;
        }
        List<Long> zoneIds = templateZoneMapper.listEconomicZoneIdsByTemplateId(t.getId());
        if (zoneIds == null || zoneIds.isEmpty()) {
            ctx.setLightThrowRatio(LightThrowDefaults.ECONOMIC_ZONE_FALLBACK);
            forward(ctx);
            return;
        }
        int min = Integer.MAX_VALUE;
        for (Long zid : zoneIds) {
            BaseEconomicZone z = economicZoneMapper.selectById(zid);
            if (z == null) {
                continue;
            }
            int r = z.getLightThrowRatio() != null && z.getLightThrowRatio() > 0
                    ? z.getLightThrowRatio()
                    : LightThrowDefaults.ECONOMIC_ZONE_FALLBACK;
            min = Math.min(min, r);
        }
        ctx.setLightThrowRatio(min == Integer.MAX_VALUE ? LightThrowDefaults.ECONOMIC_ZONE_FALLBACK : min);
        forward(ctx);
    }
}
