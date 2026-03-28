package com.wanshu.base.freight.handler;

import com.wanshu.base.freight.AbstractFreightHandler;
import com.wanshu.base.freight.BillableWeightRules;
import com.wanshu.base.freight.FreightCalculationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 计费重量按普快规则进位。
 */
@Component
@Order(5)
public class BillableWeightRoundingHandler extends AbstractFreightHandler {

    @Override
    public void handle(FreightCalculationContext ctx) {
        ctx.setBillableWeight(BillableWeightRules.normalize(ctx.getBillableWeightRaw()));
        forward(ctx);
    }
}
