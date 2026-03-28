package com.wanshu.base.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.base.dto.FreightTemplatePayload;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.base.BaseEconomicZone;
import com.wanshu.model.entity.base.BaseFreightTemplate;
import com.wanshu.base.service.BaseFreightTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "运费管理")
@RestController
@RequestMapping("/api/base/freight")
@RequiredArgsConstructor
public class BaseFreightTemplateController {

    private final BaseFreightTemplateService templateService;

    @Operation(summary = "运费模板分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer type) {
        IPage<BaseFreightTemplate> page = templateService.page(pageNum, pageSize, name, type);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "经济区字典（经济区互寄模板多选）")
    @GetMapping("/economic-zones")
    public R<List<BaseEconomicZone>> economicZones() {
        return R.ok(templateService.listEconomicZones());
    }

    @Operation(summary = "运费模板详情（含 economicZoneIds）")
    @GetMapping("/{id}")
    public R<FreightTemplatePayload> detail(@PathVariable Long id) {
        return R.ok(templateService.getDetailPayload(id));
    }

    @Operation(summary = "创建运费模板")
    @PostMapping
    public R<Void> create(@RequestBody FreightTemplatePayload template) {
        templateService.create(template);
        return R.ok();
    }

    @Operation(summary = "更新运费模板")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody FreightTemplatePayload template) {
        template.setId(id);
        templateService.update(template);
        return R.ok();
    }

    @Operation(summary = "删除运费模板")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return R.ok();
    }

    @Operation(summary = "运费计算（体积为 cm³；可传长×宽×高 cm 自动算体积）")
    @GetMapping("/calculate")
    public R<Map<String, Object>> calculate(
            @RequestParam Long templateId,
            @RequestParam BigDecimal weight,
            @RequestParam(required = false) BigDecimal volume,
            @RequestParam(required = false) BigDecimal length,
            @RequestParam(required = false) BigDecimal width,
            @RequestParam(required = false) BigDecimal height) {
        return R.ok(templateService.calculateFreight(templateId, weight, volume, length, width, height));
    }
}
