package com.wanshu.base.freight;

/**
 * 运费计算职责链节点。
 */
public interface FreightHandler {

    void setNext(FreightHandler next);

    /**
     * 处理当前步骤；链尾应终止，不再调用 next。
     */
    void handle(FreightCalculationContext ctx);
}
