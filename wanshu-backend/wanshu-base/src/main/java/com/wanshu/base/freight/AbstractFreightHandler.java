package com.wanshu.base.freight;

import lombok.Setter;

/**
 * 职责链抽象基类，持有下一环。
 */
public abstract class AbstractFreightHandler implements FreightHandler {

    @Setter
    private FreightHandler next;

    protected void forward(FreightCalculationContext ctx) {
        if (next != null) {
            next.handle(ctx);
        }
    }
}
