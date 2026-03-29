package com.wanshu.web.controller.emp;

import com.wanshu.common.result.R;
import com.wanshu.model.entity.base.BaseVehicle;
import com.wanshu.web.dto.emp.BindVehicleRequest;
import com.wanshu.web.dto.emp.DriverVO;
import com.wanshu.web.service.emp.EmpDriverAdminService;
import com.wanshu.web.service.emp.EmpDriverVehicleBindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 司机管理：仅查询；车辆绑定见 bind/unbind 接口（含业务校验）。
 */
@Tag(name = "司机管理")
@RestController
@RequestMapping("/api/emp/driver")
@RequiredArgsConstructor
public class EmpDriverController {

    private final EmpDriverAdminService driverAdminService;
    private final EmpDriverVehicleBindService vehicleBindService;

    @Operation(summary = "司机已绑定车辆列表")
    @GetMapping("/bound-vehicles")
    public R<List<BaseVehicle>> boundVehicles(@RequestParam Long driverId) {
        return R.ok(vehicleBindService.listBoundVehicles(driverId));
    }

    @Operation(summary = "绑定车辆（需满足资料、排班、车辆状态等条件）")
    @PostMapping("/bind-vehicle")
    public R<Void> bindVehicle(@RequestBody BindVehicleRequest req) {
        vehicleBindService.bind(req.getDriverId(), req.getVehicleId());
        return R.ok();
    }

    @Operation(summary = "解绑车辆")
    @DeleteMapping("/unbind-vehicle")
    public R<Void> unbindVehicle(@RequestParam Long driverId, @RequestParam Long vehicleId) {
        vehicleBindService.unbind(driverId, vehicleId);
        return R.ok();
    }

    @Operation(summary = "司机分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long organId,
            @RequestParam(required = false) Integer workStatus,
            @RequestParam(required = false) String keyword) {
        return R.ok(driverAdminService.page(pageNum, pageSize, organId, workStatus, keyword));
    }

    @Operation(summary = "司机详情")
    @GetMapping("/{id}")
    public R<DriverVO> detail(@PathVariable Long id) {
        return R.ok(driverAdminService.getDetail(id));
    }
}
