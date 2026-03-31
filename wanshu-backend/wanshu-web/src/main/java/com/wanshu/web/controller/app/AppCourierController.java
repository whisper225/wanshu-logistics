package com.wanshu.web.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.common.result.R;
import com.wanshu.web.service.app.AppCourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 快递员端（小程序）— 揽收 & 派件
 */
@Tag(name = "快递员端（小程序）")
@RestController
@RequestMapping("/api/courier")
@RequiredArgsConstructor
public class AppCourierController {

    private final AppCourierService courierService;

    // ── 揽收任务 ──────────────────────────────────────────────────

    @Operation(summary = "我的揽收任务列表（status: 0=待分配 1=已分配 2=已完成 3=已取消）")
    @GetMapping("/pickup")
    public R<List<Map<String, Object>>> listPickupTasks(
            @RequestParam(required = false) Integer status) {
        Long courierId = StpUtil.getLoginIdAsLong();
        return R.ok(courierService.listPickupTasks(courierId, status));
    }

    @Operation(summary = "确认揽收（完成取件，生成运单并推送到调度队列）")
    @PostMapping("/pickup/{taskId}/complete")
    public R<Void> completePickup(
            @PathVariable Long taskId,
            @RequestBody(required = false) Map<String, String> body) {
        Long courierId = StpUtil.getLoginIdAsLong();
        String remark = body != null ? body.get("remark") : null;
        courierService.completePickup(taskId, courierId, remark);
        return R.ok();
    }

    // ── 派件任务 ──────────────────────────────────────────────────

    @Operation(summary = "我的派件任务列表（status: 0=待分配 1=已分配 2=已签收 3=已拒收）")
    @GetMapping("/delivery")
    public R<List<Map<String, Object>>> listDeliveryTasks(
            @RequestParam(required = false) Integer status) {
        Long courierId = StpUtil.getLoginIdAsLong();
        return R.ok(courierService.listDeliveryTasks(courierId, status));
    }

    @Operation(summary = "确认签收（收件人签收）")
    @PostMapping("/delivery/{taskId}/sign")
    public R<Void> signDelivery(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> body) {
        Long courierId = StpUtil.getLoginIdAsLong();
        Integer signType = body.get("signType") != null
                ? Integer.valueOf(body.get("signType").toString()) : 0;
        String signImage = body.get("signImage") != null
                ? body.get("signImage").toString() : null;
        courierService.signDelivery(taskId, courierId, signType, signImage);
        return R.ok();
    }

    @Operation(summary = "确认拒收（收件人拒收，触发重新调度）")
    @PostMapping("/delivery/{taskId}/reject")
    public R<Void> rejectDelivery(
            @PathVariable Long taskId,
            @RequestBody(required = false) Map<String, String> body) {
        Long courierId = StpUtil.getLoginIdAsLong();
        String reason = body != null ? body.get("reason") : null;
        courierService.rejectDelivery(taskId, courierId, reason);
        return R.ok();
    }

    // ── 位置上报 ──────────────────────────────────────────────────

    @Operation(summary = "快递员上报实时位置（更新轨迹当前坐标）")
    @PostMapping("/location")
    public R<Void> reportLocation(@RequestBody Map<String, Object> body) {
        Long waybillId = Long.valueOf(body.get("waybillId").toString());
        double longitude = Double.parseDouble(body.get("longitude").toString());
        double latitude = Double.parseDouble(body.get("latitude").toString());
        courierService.updateLocation(waybillId, longitude, latitude);
        return R.ok();
    }
}
