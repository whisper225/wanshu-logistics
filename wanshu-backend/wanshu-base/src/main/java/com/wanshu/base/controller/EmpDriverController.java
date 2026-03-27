package com.wanshu.base.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.emp.EmpDriver;
import com.wanshu.base.service.EmpDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "司机管理")
@RestController
@RequestMapping("/api/emp/driver")
@RequiredArgsConstructor
public class EmpDriverController {

    private final EmpDriverService driverService;

    @Operation(summary = "司机分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long organId,
            @RequestParam(required = false) Integer workStatus) {
        IPage<EmpDriver> page = driverService.page(pageNum, pageSize, organId, workStatus);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "司机详情")
    @GetMapping("/{id}")
    public R<EmpDriver> detail(@PathVariable Long id) {
        return R.ok(driverService.getById(id));
    }

    @Operation(summary = "创建司机")
    @PostMapping
    public R<Void> create(@RequestBody EmpDriver driver) {
        driverService.create(driver);
        return R.ok();
    }

    @Operation(summary = "更新司机")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody EmpDriver driver) {
        driver.setId(id);
        driverService.update(driver);
        return R.ok();
    }

    @Operation(summary = "删除司机")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        driverService.delete(id);
        return R.ok();
    }
}
