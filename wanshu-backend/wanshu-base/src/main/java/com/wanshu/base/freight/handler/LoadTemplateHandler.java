package com.wanshu.base.freight.handler;

import com.wanshu.base.freight.AbstractFreightHandler;
import com.wanshu.base.freight.FreightCalculationContext;
import com.wanshu.base.mapper.BaseFreightTemplateMapper;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseFreightTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 加载运费模板。
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class LoadTemplateHandler extends AbstractFreightHandler {

    private final BaseFreightTemplateMapper templateMapper;

    @Override
    public void handle(FreightCalculationContext ctx) {
        BaseFreightTemplate t = templateMapper.selectById(ctx.getTemplateId());
        if (t == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "运费模板不存在");
        }
        ctx.setTemplate(t);
        forward(ctx);
    }
}
