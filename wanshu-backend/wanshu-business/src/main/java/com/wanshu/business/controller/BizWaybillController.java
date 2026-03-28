package com.wanshu.business.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.biz.BizWaybill;
import com.wanshu.business.service.BizWaybillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "运单管理")
@RestController
@RequestMapping("/api/biz/waybill")
@RequiredArgsConstructor
public class BizWaybillController {

    private final BizWaybillService waybillService;

    @Operation(summary = "运单分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<BizWaybill> page = waybillService.page(pageNum, pageSize, keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "运单详情")
    @GetMapping("/{id}")
    public R<BizWaybill> detail(@PathVariable Long id) {
        return R.ok(waybillService.getById(id));
    }

    @Operation(summary = "运单物流轨迹")
    @GetMapping("/{id}/track")
    public R<List<Map<String, Object>>> track(@PathVariable Long id) {
        return R.ok(waybillService.listTrack(id));
    }

    @Operation(summary = "创建运单")
    @PostMapping
    public R<Void> create(@RequestBody BizWaybill waybill) {
        waybillService.create(waybill);
        return R.ok();
    }

    @Operation(summary = "更新运单")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody BizWaybill waybill) {
        waybill.setId(id);
        waybillService.update(waybill);
        return R.ok();
    }

    @Operation(summary = "更新运单状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        waybillService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "删除运单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        waybillService.delete(id);
        return R.ok();
    }
}
