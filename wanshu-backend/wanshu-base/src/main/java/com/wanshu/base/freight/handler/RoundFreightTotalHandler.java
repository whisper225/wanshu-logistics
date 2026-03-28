package com.wanshu.base.freight.handler;

import com.wanshu.base.freight.AbstractFreightHandler;
import com.wanshu.base.freight.FreightCalculationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

/**
 * 总运费四舍五入保留 1 位小数。
 */
@Component
@Order(7)
public class RoundFreightTotalHandler extends AbstractFreightHandler {

    @Override
    public void handle(FreightCalculationContext ctx) {
        ctx.setFreightTotal(ctx.getFreightAmount().setScale(1, RoundingMode.HALF_UP));
        // 链尾，不再 forward
    }
}
