package com.wanshu.base.freight;

import com.wanshu.base.freight.handler.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 运费计算职责链装配与执行入口。
 */
@Component
@RequiredArgsConstructor
public class FreightCalculationPipeline {

    private final LoadTemplateHandler loadTemplateHandler;
    private final ResolveLightThrowRatioHandler resolveLightThrowRatioHandler;
    private final ComputeVolumeWeightHandler computeVolumeWeightHandler;
    private final MergeBillableWeightHandler mergeBillableWeightHandler;
    private final BillableWeightRoundingHandler billableWeightRoundingHandler;
    private final CalculateFreightAmountHandler calculateFreightAmountHandler;
    private final RoundFreightTotalHandler roundFreightTotalHandler;

    private FreightHandler head;

    @PostConstruct
    void wireChain() {
        loadTemplateHandler.setNext(resolveLightThrowRatioHandler);
        resolveLightThrowRatioHandler.setNext(computeVolumeWeightHandler);
        computeVolumeWeightHandler.setNext(mergeBillableWeightHandler);
        mergeBillableWeightHandler.setNext(billableWeightRoundingHandler);
        billableWeightRoundingHandler.setNext(calculateFreightAmountHandler);
        calculateFreightAmountHandler.setNext(roundFreightTotalHandler);
        head = loadTemplateHandler;
    }

    public void execute(FreightCalculationContext ctx) {
        head.handle(ctx);
    }
}
