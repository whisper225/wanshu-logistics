package com.wanshu.base.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.emp.EmpCourier;
import com.wanshu.model.entity.emp.EmpCourierScope;
import com.wanshu.base.service.EmpCourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "快递员管理")
@RestController
@RequestMapping("/api/emp/courier")
@RequiredArgsConstructor
public class EmpCourierController {

    private final EmpCourierService courierService;

    @Operation(summary = "快递员分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long organId,
            @RequestParam(required = false) Integer workStatus) {
        IPage<EmpCourier> page = courierService.page(pageNum, pageSize, organId, workStatus);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "快递员详情")
    @GetMapping("/{id}")
    public R<EmpCourier> detail(@PathVariable Long id) {
        return R.ok(courierService.getById(id));
    }

    @Operation(summary = "创建快递员")
    @PostMapping
    public R<Void> create(@RequestBody EmpCourier courier) {
        courierService.create(courier);
        return R.ok();
    }

    @Operation(summary = "更新快递员")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody EmpCourier courier) {
        courier.setId(id);
        courierService.update(courier);
        return R.ok();
    }

    @Operation(summary = "删除快递员")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        courierService.delete(id);
        return R.ok();
    }

    @Operation(summary = "获取快递员作业范围")
    @GetMapping("/{id}/scopes")
    public R<List<EmpCourierScope>> getScopes(@PathVariable Long id) {
        return R.ok(courierService.getScopes(id));
    }

    @Operation(summary = "更新快递员作业范围")
    @PutMapping("/{id}/scopes")
    public R<Void> updateScopes(@PathVariable Long id, @RequestBody List<EmpCourierScope> scopes) {
        courierService.updateScopes(id, scopes);
        return R.ok();
    }
}
