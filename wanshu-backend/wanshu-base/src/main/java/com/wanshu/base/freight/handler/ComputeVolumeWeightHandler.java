package com.wanshu.base.freight.handler;

import com.wanshu.base.freight.AbstractFreightHandler;
import com.wanshu.base.freight.FreightCalculationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 体积重 = 体积(cm³) / 轻抛系数。
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class ComputeVolumeWeightHandler extends AbstractFreightHandler {

    @Override
    public void handle(FreightCalculationContext ctx) {
        BigDecimal vol = ctx.getVolumeCm3();
        if (vol == null || vol.compareTo(BigDecimal.ZERO) <= 0) {
            ctx.setVolumeWeight(BigDecimal.ZERO);
        } else {
            BigDecimal ratio = BigDecimal.valueOf(ctx.getLightThrowRatio());
            ctx.setVolumeWeight(vol.divide(ratio, 10, RoundingMode.HALF_UP));
        }
        forward(ctx);
    }
}
