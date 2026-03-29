package com.wanshu.web.service.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.business.mapper.BizOrderMapper;
import com.wanshu.business.mapper.BizWaybillMapper;
import com.wanshu.business.service.BizOrderService;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.biz.BizWaybill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppOrderService {

    private final BizOrderMapper orderMapper;
    private final BizWaybillMapper waybillMapper;
    private final BizOrderService orderService;

    /**
     * 查询当前用户的订单和运单列表（合并展示：寄件=订单视角，收件=运单视角）
     */
    public Map<String, Object> listUserOrders(Long userId, String keyword, Integer type, int pageNum, int pageSize) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (type == null || type == 0) {
            // 寄件列表（以订单为主体）
            LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizOrder::getUserId, userId);
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(BizOrder::getOrderNumber, keyword)
                        .or().like(BizOrder::getSenderName, keyword)
                        .or().like(BizOrder::getSenderPhone, keyword)
                        .or().like(BizOrder::getReceiverName, keyword)
                        .or().like(BizOrder::getReceiverPhone, keyword));
            }
            wrapper.orderByDesc(BizOrder::getCreatedTime);
            List<BizOrder> orders = orderMapper.selectList(wrapper);
            for (BizOrder o : orders) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "send");
                item.put("id", o.getId());
                item.put("number", o.getOrderNumber());
                item.put("status", o.getStatus());
                item.put("statusText", getOrderStatusText(o.getStatus()));
                item.put("senderName", o.getSenderName());
                item.put("senderPhone", o.getSenderPhone());
                item.put("receiverName", o.getReceiverName());
                item.put("receiverPhone", o.getReceiverPhone());
                item.put("goodsName", o.getGoodsName());
                item.put("estimatedFee", o.getEstimatedFee());
                item.put("actualFee", o.getActualFee());
                item.put("createdTime", o.getCreatedTime());
                result.add(item);
            }
        }

        if (type == null || type == 1) {
            // 收件列表（以运单为主体，收件人手机号匹配）
            // 实际场景应关联wx用户手机号；当前用演示占位查询
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("list", result);
        resp.put("total", result.size());
        return resp;
    }

    public BizOrder getOrderDetail(Long id, Long userId) {
        BizOrder order = orderMapper.selectById(id);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return order;
    }

    /**
     * 获取订单关联的运单信息
     */
    public BizWaybill getWaybillByOrder(Long orderId, Long userId) {
        getOrderDetail(orderId, userId);
        LambdaQueryWrapper<BizWaybill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizWaybill::getOrderId, orderId);
        return waybillMapper.selectOne(wrapper);
    }

    @Transactional
    public BizOrder createOrder(BizOrder order, Long userId) {
        order.setUserId(userId);
        order.setSource(1);
        orderService.create(order);
        return orderMapper.selectById(order.getId());
    }

    @Transactional
    public void cancelOrder(Long id, Long userId, String reason) {
        getOrderDetail(id, userId);
        orderService.cancel(id, reason);
    }

    @Transactional
    public void deleteOrder(Long id, Long userId) {
        BizOrder order = getOrderDetail(id, userId);
        // 只有已取消(7)、已签收(5)、已拒收(6)的订单可以删除
        if (order.getStatus() != null && order.getStatus() != 7
                && order.getStatus() != 5 && order.getStatus() != 6) {
            throw new BusinessException("只有已取消、已签收、已拒收的订单才能删除");
        }
        orderService.delete(id);
    }

    /**
     * 简单运费估算（固定费率，实际应查询运费模板）
     */
    public Map<String, Object> estimateFreight(BigDecimal weight, String goodsType) {
        BigDecimal baseRate = new BigDecimal("12.00");
        BigDecimal fee = baseRate.add(weight.subtract(BigDecimal.ONE).max(BigDecimal.ZERO)
                .multiply(new BigDecimal("3.00")));
        Map<String, Object> result = new HashMap<>();
        result.put("estimatedFee", fee);
        result.put("note", "参考价格，实际以揽件后计费为准");
        return result;
    }

    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "已下单";
            case 1 -> "已接单";
            case 2 -> "已揽收";
            case 3 -> "运输中";
            case 4 -> "派送中";
            case 5 -> "已签收";
            case 6 -> "已拒收";
            case 7 -> "已取消";
            default -> "未知";
        };
    }
}
