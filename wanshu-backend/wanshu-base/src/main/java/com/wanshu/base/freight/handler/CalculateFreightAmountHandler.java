package com.wanshu.base.freight.handler;

import com.wanshu.base.freight.AbstractFreightHandler;
import com.wanshu.base.freight.FreightCalculationContext;
import com.wanshu.model.entity.base.BaseFreightTemplate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 首重 + 续重（续重单位向上取整档 × 续重单价）。
 */
@Component
@Order(6)
public class CalculateFreightAmountHandler extends AbstractFreightHandler {

    @Override
    public void handle(FreightCalculationContext ctx) {
        BaseFreightTemplate t = ctx.getTemplate();
        BigDecimal firstW = t.getFirstWeight() != null ? t.getFirstWeight() : BigDecimal.ONE;
        BigDecimal firstP = t.getFirstWeightPrice() != null ? t.getFirstWeightPrice() : BigDecimal.ZERO;
        BigDecimal extraUnit = t.getExtraWeight() != null && t.getExtraWeight().compareTo(BigDecimal.ZERO) > 0
                ? t.getExtraWeight()
                : BigDecimal.ONE;
        BigDecimal extraPrice = t.getExtraWeightPrice() != null ? t.getExtraWeightPrice() : BigDecimal.ZERO;

        BigDecimal bill = ctx.getBillableWeight();
        BigDecimal freight;
        if (bill.compareTo(firstW) <= 0) {
            freight = firstP;
        } else {
            BigDecimal extra = bill.subtract(firstW);
            BigDecimal parts = extra.divide(extraUnit, 0, RoundingMode.CEILING);
            freight = firstP.add(parts.multiply(extraPrice));
        }
        ctx.setFreightAmount(freight);
        forward(ctx);
    }
}
