package com.wanshu.web.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.common.result.R;
import com.wanshu.web.service.app.AppDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 司机端（小程序）— 运输任务、出库、入库、回车登记
 */
@Tag(name = "司机端（小程序）")
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class AppDriverController {

    private final AppDriverService driverService;

    // ── 运输任务 ──────────────────────────────────────────────────

    @Operation(summary = "我的运输任务列表（status: 0=待出发 1=运输中 2=已完成）")
    @GetMapping("/transport")
    public R<List<Map<String, Object>>> listTransportTasks(
            @RequestParam(required = false) Integer status) {
        Long driverId = StpUtil.getLoginIdAsLong();
        return R.ok(driverService.listTransportTasks(driverId, status));
    }

    @Operation(summary = "运输任务详情（含运单列表）")
    @GetMapping("/transport/{taskId}")
    public R<Map<String, Object>> getTransportTaskDetail(@PathVariable Long taskId) {
        Long driverId = StpUtil.getLoginIdAsLong();
        return R.ok(driverService.getTransportTaskDetail(taskId, driverId));
    }

    // ── 司机出库 ──────────────────────────────────────────────────

    @Operation(summary = "司机出库确认（开始运输：更新运单/订单为运输中，更新车辆状态）")
    @PostMapping("/transport/{taskId}/depart")
    public R<Void> confirmDeparture(@PathVariable Long taskId) {
        Long driverId = StpUtil.getLoginIdAsLong();
        driverService.confirmDeparture(taskId, driverId);
        return R.ok();
    }

    // ── 司机入库 ──────────────────────────────────────────────────

    @Operation(summary = "司机入库确认（完成运输：计算运单流转节点，触发派件/中转调度，更新车辆机构）")
    @PostMapping("/transport/{taskId}/arrive")
    public R<Void> confirmArrival(@PathVariable Long taskId) {
        Long driverId = StpUtil.getLoginIdAsLong();
        driverService.confirmArrival(taskId, driverId);
        return R.ok();
    }

    // ── 回车登记 ──────────────────────────────────────────────────

    @Operation(summary = "回车登记（入库后填写情况说明与照片）")
    @PostMapping("/transport/{taskId}/return")
    public R<Void> createReturnRegister(
            @PathVariable Long taskId,
            @RequestBody(required = false) Map<String, String> body) {
        Long driverId = StpUtil.getLoginIdAsLong();
        String description = body != null ? body.get("description") : null;
        String images = body != null ? body.get("images") : null;
        driverService.createReturnRegister(taskId, driverId, description, images);
        return R.ok();
    }

    // ── 位置上报 ──────────────────────────────────────────────────

    @Operation(summary = "司机上报实时位置（更新轨迹当前坐标）")
    @PostMapping("/location")
    public R<Void> reportLocation(@RequestBody Map<String, Object> body) {
        Long waybillId = Long.valueOf(body.get("waybillId").toString());
        double longitude = Double.parseDouble(body.get("longitude").toString());
        double latitude = Double.parseDouble(body.get("latitude").toString());
        driverService.updateLocation(waybillId, longitude, latitude);
        return R.ok();
    }
}
