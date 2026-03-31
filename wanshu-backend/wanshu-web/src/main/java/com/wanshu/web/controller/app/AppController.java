package com.wanshu.web.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.base.service.AppAddressBookService;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.R;
import com.wanshu.common.utils.OssUtil;
import com.wanshu.model.entity.app.AppAddressBook;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.biz.BizWaybill;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.model.entity.track.TrackNode;
import com.wanshu.model.entity.track.TrackRoute;
import com.wanshu.system.service.SysUserService;
import com.wanshu.web.service.app.AppOrderService;
import com.wanshu.web.service.app.AppTrackService;
import com.wanshu.web.service.app.TrackNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "用户端（小程序）")
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppController {

    private final AppAddressBookService addressBookService;
    private final AppOrderService appOrderService;
    private final AppTrackService appTrackService;
    private final TrackNodeService trackNodeService;
    private final SysUserService sysUserService;
    private final OssUtil ossUtil;

    // ========== 个人信息 ==========

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user")
    public R<Map<String, Object>> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        return R.ok(Map.of(
                "id", user.getId(),
                "name", user.getRealName() != null ? user.getRealName() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                "gender", user.getGender() != null ? user.getGender() : 0,
                "birthday", user.getBirthday() != null ? user.getBirthday().toString() : ""
        ));
    }

    @Operation(summary = "更新个人信息（性别、生日）")
    @PutMapping("/user")
    public R<Void> updateUserInfo(@RequestBody Map<String, Object> body) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser update = new SysUser();
        update.setId(userId);
        if (body.containsKey("gender")) {
            update.setGender(Integer.valueOf(body.get("gender").toString()));
        }
        if (body.containsKey("birthday") && body.get("birthday") != null
                && !body.get("birthday").toString().isBlank()) {
            update.setBirthday(LocalDate.parse(body.get("birthday").toString()));
        }
        if (body.containsKey("realName") && body.get("realName") != null) {
            update.setRealName(body.get("realName").toString());
        }
        sysUserService.update(update);
        return R.ok();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/user/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        try {
            String avatarUrl = ossUtil.upload(file.getInputStream(), file.getOriginalFilename());
            SysUser update = new SysUser();
            update.setId(userId);
            update.setAvatar(avatarUrl);
            sysUserService.update(update);
            return R.ok(avatarUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("头像上传失败：" + e.getMessage());
        }
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

    // ========== 轨迹 ==========

    @Operation(summary = "查询订单路线轨迹（不存在时自动生成）")
    @GetMapping("/order/{id}/track")
    public R<TrackRoute> getOrderTrack(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 校验订单归属
        appOrderService.getOrderDetail(id, userId);
        TrackRoute route = appTrackService.getOrCreate(id);
        return R.ok(route);
    }

    @Operation(summary = "刷新订单路线轨迹（地址变更后重新规划）")
    @PostMapping("/order/{id}/track/refresh")
    public R<TrackRoute> refreshOrderTrack(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        appOrderService.getOrderDetail(id, userId);
        TrackRoute route = appTrackService.refresh(id);
        return R.ok(route);
    }

    @Operation(summary = "查询订单物流跟踪节点（时间线）")
    @GetMapping("/order/{id}/tracking-nodes")
    public R<List<TrackNode>> getTrackingNodes(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        BizWaybill waybill = appOrderService.getWaybillByOrder(id, userId);
        if (waybill == null) {
            return R.ok(List.of());
        }
        return R.ok(trackNodeService.listByWaybillId(waybill.getId()));
    }
}
