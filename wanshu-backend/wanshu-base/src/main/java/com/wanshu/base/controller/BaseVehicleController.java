package com.wanshu.base.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.base.BaseVehicle;
import com.wanshu.model.entity.base.BaseVehicleType;
import com.wanshu.base.service.BaseVehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "车辆管理")
@RestController
@RequestMapping("/api/base/vehicle")
@RequiredArgsConstructor
public class BaseVehicleController {

    private final BaseVehicleService vehicleService;

    // ========== 车型 ==========

    @Operation(summary = "车型分页列表")
    @GetMapping("/type/page")
    public R<Map<String, Object>> pageTypes(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<BaseVehicleType> page = vehicleService.pageTypes(pageNum, pageSize, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "全部车型列表")
    @GetMapping("/type/list")
    public R<List<BaseVehicleType>> listAllTypes() {
        return R.ok(vehicleService.listAllTypes());
    }

    @Operation(summary = "创建车型")
    @PostMapping("/type")
    public R<Void> createType(@RequestBody BaseVehicleType type) {
        vehicleService.createType(type);
        return R.ok();
    }

    @Operation(summary = "更新车型")
    @PutMapping("/type/{id}")
    public R<Void> updateType(@PathVariable Long id, @RequestBody BaseVehicleType type) {
        type.setId(id);
        vehicleService.updateType(type);
        return R.ok();
    }

    @Operation(summary = "删除车型")
    @DeleteMapping("/type/{id}")
    public R<Void> deleteType(@PathVariable Long id) {
        vehicleService.deleteType(id);
        return R.ok();
    }

    // ========== 车辆 ==========

    @Operation(summary = "车辆分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> pageVehicles(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<BaseVehicle> page = vehicleService.pageVehicles(pageNum, pageSize, keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "车辆详情")
    @GetMapping("/{id}")
    public R<BaseVehicle> detail(@PathVariable Long id) {
        return R.ok(vehicleService.getVehicleById(id));
    }

    @Operation(summary = "创建车辆")
    @PostMapping
    public R<Void> createVehicle(@RequestBody BaseVehicle vehicle) {
        vehicleService.createVehicle(vehicle);
        return R.ok();
    }

    @Operation(summary = "更新车辆")
    @PutMapping("/{id}")
    public R<Void> updateVehicle(@PathVariable Long id, @RequestBody BaseVehicle vehicle) {
        vehicle.setId(id);
        vehicleService.updateVehicle(vehicle);
        return R.ok();
    }

    @Operation(summary = "删除车辆")
    @DeleteMapping("/{id}")
    public R<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return R.ok();
    }

    @Operation(summary = "获取车辆绑定的司机")
    @GetMapping("/{id}/drivers")
    public R<List<Long>> getDrivers(@PathVariable Long id) {
        return R.ok(vehicleService.getDriverIds(id));
    }

    @Operation(summary = "绑定司机")
    @PostMapping("/{id}/drivers/{driverId}")
    public R<Void> bindDriver(@PathVariable Long id, @PathVariable Long driverId) {
        vehicleService.bindDriver(id, driverId);
        return R.ok();
    }

    @Operation(summary = "解绑司机")
    @DeleteMapping("/{id}/drivers/{driverId}")
    public R<Void> unbindDriver(@PathVariable Long id, @PathVariable Long driverId) {
        vehicleService.unbindDriver(id, driverId);
        return R.ok();
    }
}
