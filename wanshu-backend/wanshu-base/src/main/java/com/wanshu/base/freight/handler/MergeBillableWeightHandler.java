package com.wanshu.base.freight.handler;

import com.wanshu.base.freight.AbstractFreightHandler;
import com.wanshu.base.freight.FreightCalculationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 取实重与体积重较大者为计费重量（进位前）。
 */
@Component
@Order(4)
public class MergeBillableWeightHandler extends AbstractFreightHandler {

    @Override
    public void handle(FreightCalculationContext ctx) {
        BigDecimal w = ctx.getActualWeight() != null ? ctx.getActualWeight() : BigDecimal.ZERO;
        BigDecimal vw = ctx.getVolumeWeight() != null ? ctx.getVolumeWeight() : BigDecimal.ZERO;
        ctx.setBillableWeightRaw(w.max(vw));
        forward(ctx);
    }
}
