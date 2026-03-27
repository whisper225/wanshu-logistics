package com.wanshu.business.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.biz.BizDeliveryTask;
import com.wanshu.model.entity.biz.BizPickupTask;
import com.wanshu.business.service.BizTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "取件/派件作业")
@RestController
@RequestMapping("/api/biz/task")
@RequiredArgsConstructor
public class BizTaskController {

    private final BizTaskService taskService;

    // ========== 取件作业 ==========

    @Operation(summary = "取件作业分页列表")
    @GetMapping("/pickup/page")
    public R<Map<String, Object>> pagePickupTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<BizPickupTask> page = taskService.pagePickupTasks(pageNum, pageSize, keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "取件作业详情")
    @GetMapping("/pickup/{id}")
    public R<BizPickupTask> pickupDetail(@PathVariable Long id) {
        return R.ok(taskService.getPickupTaskById(id));
    }

    @Operation(summary = "创建取件作业")
    @PostMapping("/pickup")
    public R<Void> createPickupTask(@RequestBody BizPickupTask task) {
        taskService.createPickupTask(task);
        return R.ok();
    }

    @Operation(summary = "分配取件快递员")
    @PutMapping("/pickup/{id}/assign")
    public R<Void> assignPickupTask(@PathVariable Long id, @RequestParam Long courierId) {
        taskService.assignPickupTask(id, courierId);
        return R.ok();
    }

    @Operation(summary = "取消取件作业")
    @PostMapping("/pickup/{id}/cancel")
    public R<Void> cancelPickupTask(@PathVariable Long id, @RequestBody Map<String, String> params) {
        taskService.cancelPickupTask(id, params.get("reason"));
        return R.ok();
    }

    // ========== 派件作业 ==========

    @Operation(summary = "派件作业分页列表")
    @GetMapping("/delivery/page")
    public R<Map<String, Object>> pageDeliveryTasks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<BizDeliveryTask> page = taskService.pageDeliveryTasks(pageNum, pageSize, keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "派件作业详情")
    @GetMapping("/delivery/{id}")
    public R<BizDeliveryTask> deliveryDetail(@PathVariable Long id) {
        return R.ok(taskService.getDeliveryTaskById(id));
    }

    @Operation(summary = "创建派件作业")
    @PostMapping("/delivery")
    public R<Void> createDeliveryTask(@RequestBody BizDeliveryTask task) {
        taskService.createDeliveryTask(task);
        return R.ok();
    }

    @Operation(summary = "分配派件快递员")
    @PutMapping("/delivery/{id}/assign")
    public R<Void> assignDeliveryTask(@PathVariable Long id, @RequestParam Long courierId) {
        taskService.assignDeliveryTask(id, courierId);
        return R.ok();
    }
}
