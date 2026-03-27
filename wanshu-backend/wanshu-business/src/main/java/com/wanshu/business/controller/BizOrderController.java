package com.wanshu.business.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.business.service.BizOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/biz/order")
@RequiredArgsConstructor
public class BizOrderController {

    private final BizOrderService orderService;

    @Operation(summary = "订单分页列表")
    @GetMapping("/page")
    public R<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<BizOrder> page = orderService.page(pageNum, pageSize, keyword, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return R.ok(result);
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public R<BizOrder> detail(@PathVariable Long id) {
        return R.ok(orderService.getById(id));
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public R<Void> create(@RequestBody BizOrder order) {
        orderService.create(order);
        return R.ok();
    }

    @Operation(summary = "更新订单")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody BizOrder order) {
        order.setId(id);
        orderService.update(order);
        return R.ok();
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id, @RequestBody Map<String, String> params) {
        orderService.cancel(id, params.get("reason"));
        return R.ok();
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return R.ok();
    }
}
