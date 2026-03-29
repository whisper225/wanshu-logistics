package com.wanshu.web.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.base.service.AppAddressBookService;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.app.AppAddressBook;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.biz.BizWaybill;
import com.wanshu.system.service.SysUserService;
import com.wanshu.web.service.app.AppOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "用户端（小程序）")
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppController {

    private final AppAddressBookService addressBookService;
    private final AppOrderService appOrderService;
    private final SysUserService sysUserService;

    // ========== 个人信息 ==========

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user")
    public R<Map<String, Object>> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        var user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return R.ok(Map.of(
                "id", user.getId(),
                "name", user.getRealName() != null ? user.getRealName() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                "gender", user.getGender() != null ? user.getGender() : 0
        ));
    }

    // ========== 地址簿 ==========

    @Operation(summary = "地址簿列表")
    @GetMapping("/address")
    public R<List<AppAddressBook>> listAddress(
            @RequestParam(required = false) String keyword) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(addressBookService.listByUser(userId, keyword));
    }

    @Operation(summary = "获取地址详情")
    @GetMapping("/address/{id}")
    public R<AppAddressBook> getAddress(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(addressBookService.getById(id, userId));
    }

    @Operation(summary = "新增地址")
    @PostMapping("/address")
    public R<Void> createAddress(@RequestBody AppAddressBook book) {
        Long userId = StpUtil.getLoginIdAsLong();
        addressBookService.create(book, userId);
        return R.ok();
    }

    @Operation(summary = "更新地址")
    @PutMapping("/address/{id}")
    public R<Void> updateAddress(@PathVariable Long id, @RequestBody AppAddressBook book) {
        Long userId = StpUtil.getLoginIdAsLong();
        addressBookService.update(id, book, userId);
        return R.ok();
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/address/{id}")
    public R<Void> deleteAddress(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        addressBookService.delete(id, userId);
        return R.ok();
    }

    @Operation(summary = "批量删除地址")
    @DeleteMapping("/address/batch")
    public R<Void> deleteAddressBatch(@RequestBody List<Long> ids) {
        Long userId = StpUtil.getLoginIdAsLong();
        addressBookService.deleteBatch(ids, userId);
        return R.ok();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/address/{id}/default")
    public R<Void> setDefaultAddress(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        addressBookService.setDefault(id, userId);
        return R.ok();
    }

    // ========== 订单 ==========

    @Operation(summary = "用户订单列表（type: 0=寄件 1=收件 null=全部）")
    @GetMapping("/order")
    public R<Map<String, Object>> listOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(appOrderService.listUserOrders(userId, keyword, type, pageNum, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/order/{id}")
    public R<BizOrder> getOrderDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(appOrderService.getOrderDetail(id, userId));
    }

    @Operation(summary = "订单关联运单")
    @GetMapping("/order/{id}/waybill")
    public R<BizWaybill> getOrderWaybill(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(appOrderService.getWaybillByOrder(id, userId));
    }

    @Operation(summary = "寄快递下单")
    @PostMapping("/order")
    public R<BizOrder> createOrder(@RequestBody BizOrder order) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(appOrderService.createOrder(order, userId));
    }

    @Operation(summary = "取消寄件")
    @PostMapping("/order/{id}/cancel")
    public R<Void> cancelOrder(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, String> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        String reason = body != null ? body.get("reason") : null;
        appOrderService.cancelOrder(id, userId, reason);
        return R.ok();
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/order/{id}")
    public R<Void> deleteOrder(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        appOrderService.deleteOrder(id, userId);
        return R.ok();
    }

    @Operation(summary = "运费估算")
    @GetMapping("/freight/estimate")
    public R<Map<String, Object>> estimateFreight(
            @RequestParam(defaultValue = "1") BigDecimal weight,
            @RequestParam(required = false) String goodsType) {
        return R.ok(appOrderService.estimateFreight(weight, goodsType));
    }
}
