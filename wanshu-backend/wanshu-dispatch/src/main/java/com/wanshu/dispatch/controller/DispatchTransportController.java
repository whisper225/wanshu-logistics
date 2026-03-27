package com.wanshu.dispatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.dispatch.DispatchTransportTask;
import com.wanshu.model.entity.dispatch.DispatchReturnRegister;
import com.wanshu.dispatch.service.DispatchTransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "运输任务管理")
@RestController
@RequestMapping("/api/dispatch/transport")
@RequiredArgsConstructor
public class DispatchTransportController {

    private final DispatchTransportService transportService;

    @Operation(summary = "运输任务分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<DispatchTransportTask> page = transportService.page(pageNum, pageSize, keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "运输任务详情")
    @GetMapping("/{id}")
    public R<DispatchTransportTask> detail(@PathVariable Long id) {
        return R.ok(transportService.getById(id));
    }

    @Operation(summary = "创建运输任务")
    @PostMapping
    public R<Void> create(@RequestBody DispatchTransportTask task) {
        transportService.create(task);
        return R.ok();
    }

    @Operation(summary = "更新运输任务")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody DispatchTransportTask task) {
        task.setId(id);
        transportService.update(task);
        return R.ok();
    }

    @Operation(summary = "更新运输任务状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        transportService.updateStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "删除运输任务")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        transportService.delete(id);
        return R.ok();
    }

    // ========== 回车登记 ==========

    @Operation(summary = "回车登记分页列表")
    @GetMapping("/return/page")
    public R<Map<String, Object>> pageReturnRegisters(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<DispatchReturnRegister> page = transportService.pageReturnRegisters(pageNum, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "创建回车登记")
    @PostMapping("/return")
    public R<Void> createReturnRegister(@RequestBody DispatchReturnRegister register) {
        transportService.createReturnRegister(register);
        return R.ok();
    }
}
