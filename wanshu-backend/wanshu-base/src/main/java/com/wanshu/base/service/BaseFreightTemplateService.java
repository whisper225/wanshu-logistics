package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseFreightTemplate;
import com.wanshu.base.mapper.BaseFreightTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BaseFreightTemplateService {

    private final BaseFreightTemplateMapper templateMapper;

    public IPage<BaseFreightTemplate> page(int pageNum, int pageSize, String name, Integer type) {
        LambdaQueryWrapper<BaseFreightTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(BaseFreightTemplate::getTemplateName, name);
        }
        if (type != null) {
            wrapper.eq(BaseFreightTemplate::getTemplateType, type);
        }
        wrapper.orderByDesc(BaseFreightTemplate::getCreatedTime);
        return templateMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public BaseFreightTemplate getById(Long id) {
        BaseFreightTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return template;
    }

    @Transactional
    public void create(BaseFreightTemplate template) {
        templateMapper.insert(template);
    }

    @Transactional
    public void update(BaseFreightTemplate template) {
        templateMapper.updateById(template);
    }

    @Transactional
    public void delete(Long id) {
        templateMapper.deleteById(id);
    }

    public Map<String, Object> calculateFreight(Long templateId, BigDecimal weight, BigDecimal volume) {
        BaseFreightTemplate template = getById(templateId);
        BigDecimal firstWeight = template.getFirstWeight();
        BigDecimal firstPrice = template.getFirstWeightPrice();
        BigDecimal extraWeight = template.getExtraWeight();
        BigDecimal extraPrice = template.getExtraWeightPrice();
        int lightThrowRatio = template.getLightThrowRatio() != null ? template.getLightThrowRatio() : 6000;

        // 计算抛重
        BigDecimal volumeWeight = volume.divide(BigDecimal.valueOf(lightThrowRatio), 2, RoundingMode.HALF_UP);
        BigDecimal billableWeight = weight.max(volumeWeight);

        BigDecimal freight;
        if (billableWeight.compareTo(firstWeight) <= 0) {
            freight = firstPrice;
        } else {
            BigDecimal extra = billableWeight.subtract(firstWeight);
            BigDecimal extraParts = extra.divide(extraWeight, 0, RoundingMode.CEILING);
            freight = firstPrice.add(extraParts.multiply(extraPrice));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("freight", freight.setScale(2, RoundingMode.HALF_UP));
        result.put("billableWeight", billableWeight);
        result.put("volumeWeight", volumeWeight);
        result.put("templateName", template.getTemplateName());
        return result;
    }
}
