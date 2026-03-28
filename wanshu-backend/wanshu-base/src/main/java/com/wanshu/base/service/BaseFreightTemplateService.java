package com.wanshu.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.base.dto.FreightTemplatePayload;
import com.wanshu.base.freight.FreightCalculationContext;
import com.wanshu.base.freight.FreightCalculationPipeline;
import com.wanshu.base.mapper.BaseEconomicZoneMapper;
import com.wanshu.base.mapper.BaseFreightTemplateEconomicZoneMapper;
import com.wanshu.base.mapper.BaseFreightTemplateMapper;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseEconomicZone;
import com.wanshu.model.entity.base.BaseFreightTemplate;
import com.wanshu.model.entity.base.BaseFreightTemplateEconomicZone;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaseFreightTemplateService {

    private final BaseFreightTemplateMapper templateMapper;
    private final BaseFreightTemplateEconomicZoneMapper templateZoneMapper;
    private final BaseEconomicZoneMapper economicZoneMapper;
    private final FreightCalculationPipeline freightCalculationPipeline;

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
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "运费模板不存在");
        }
        return template;
    }

    /**
     * 详情（含经济区 ID 列表，供前端编辑）。
     */
    public List<BaseEconomicZone> listEconomicZones() {
        return economicZoneMapper.selectList(null);
    }

    public FreightTemplatePayload getDetailPayload(Long id) {
        BaseFreightTemplate t = getById(id);
        FreightTemplatePayload p = new FreightTemplatePayload();
        BeanUtils.copyProperties(t, p);
        if (Integer.valueOf(4).equals(t.getTemplateType())) {
            p.setEconomicZoneIds(templateZoneMapper.listEconomicZoneIdsByTemplateId(id));
        }
        return p;
    }

    @Transactional
    public void create(FreightTemplatePayload payload) {
        validateTemplateConstraints(payload, null);
        templateMapper.insert(payload);
        saveEconomicZoneLinks(payload.getId(), payload.getTemplateType(), payload.getEconomicZoneIds());
    }

    @Transactional
    public void update(FreightTemplatePayload payload) {
        if (payload.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "缺少模板 ID");
        }
        validateTemplateConstraints(payload, payload.getId());
        templateMapper.updateById(payload);
        templateZoneMapper.deleteByTemplateId(payload.getId());
        saveEconomicZoneLinks(payload.getId(), payload.getTemplateType(), payload.getEconomicZoneIds());
    }

    @Transactional
    public void delete(Long id) {
        templateZoneMapper.deleteByTemplateId(id);
        templateMapper.deleteById(id);
    }

    /**
     * 运费试算：体积可为 cm³，或传长×宽×高(cm) 自动相乘。
     */
    public Map<String, Object> calculateFreight(
            Long templateId,
            BigDecimal weight,
            BigDecimal volumeCm3,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm) {
        BigDecimal vol = volumeCm3;
        if (vol == null || vol.compareTo(BigDecimal.ZERO) <= 0) {
            if (lengthCm != null && widthCm != null && heightCm != null
                    && lengthCm.compareTo(BigDecimal.ZERO) > 0
                    && widthCm.compareTo(BigDecimal.ZERO) > 0
                    && heightCm.compareTo(BigDecimal.ZERO) > 0) {
                vol = lengthCm.multiply(widthCm).multiply(heightCm);
            }
        }
        if (vol == null) {
            vol = BigDecimal.ZERO;
        }
        FreightCalculationContext ctx = new FreightCalculationContext();
        ctx.setTemplateId(templateId);
        ctx.setActualWeight(weight != null ? weight : BigDecimal.ZERO);
        ctx.setVolumeCm3(vol);
        freightCalculationPipeline.execute(ctx);
        Map<String, Object> result = ctx.toResultMap();
        result.put("freightAmountRaw", ctx.getFreightAmount());
        return result;
    }

    private void validateTemplateConstraints(FreightTemplatePayload p, Long excludeTemplateId) {
        Integer type = p.getTemplateType();
        if (type == null || type < 1 || type > 4) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "模板类型须为 1~4");
        }
        if (type <= 3) {
            LambdaQueryWrapper<BaseFreightTemplate> w = new LambdaQueryWrapper<BaseFreightTemplate>()
                    .eq(BaseFreightTemplate::getTemplateType, type);
            if (excludeTemplateId != null) {
                w.ne(BaseFreightTemplate::getId, excludeTemplateId);
            }
            long cnt = templateMapper.selectCount(w);
            if (cnt > 0) {
                throw new BusinessException(ResultCode.DATA_EXIST.getCode(),
                        "同城寄、省内寄、跨省寄每种仅能配置一个模板，请修改已有模板");
            }
        }
        if (type == 4 && !CollectionUtils.isEmpty(p.getEconomicZoneIds())) {
            for (Long zid : p.getEconomicZoneIds()) {
                if (zid == null) {
                    continue;
                }
                int used = excludeTemplateId == null
                        ? templateZoneMapper.countByZoneId(zid)
                        : templateZoneMapper.countByZoneIdExcludingTemplate(zid, excludeTemplateId);
                if (used > 0) {
                    throw new BusinessException(ResultCode.DATA_EXIST.getCode(),
                            "经济区已绑定其他运费模板，不可重复关联：" + zid);
                }
            }
        }
    }

    private void saveEconomicZoneLinks(Long templateId, Integer templateType, List<Long> zoneIds) {
        if (templateId == null || !Integer.valueOf(4).equals(templateType) || CollectionUtils.isEmpty(zoneIds)) {
            return;
        }
        List<Long> distinct = zoneIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        for (Long zid : distinct) {
            BaseFreightTemplateEconomicZone row = new BaseFreightTemplateEconomicZone();
            row.setTemplateId(templateId);
            row.setEconomicZoneId(zid);
            templateZoneMapper.insert(row);
        }
    }
}
