package com.wanshu.web.service.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.base.service.BaseFreightTemplateService;
import com.wanshu.business.mapper.BizOrderMapper;
import com.wanshu.business.mapper.BizWaybillMapper;
import com.wanshu.business.service.BizOrderService;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.base.BaseFreightTemplate;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.biz.BizWaybill;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppOrderService {

    private final BizOrderMapper orderMapper;
    private final BizWaybillMapper waybillMapper;
    private final BizOrderService orderService;
    private final SysUserService sysUserService;
    private final BaseFreightTemplateService freightTemplateService;

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
            // 收件列表：以运单为主体，按收件人手机号关联当前用户
            SysUser user = sysUserService.getById(userId);
            String phone = user.getPhone();
            if (StringUtils.hasText(phone)) {
                LambdaQueryWrapper<BizWaybill> wbWrapper = new LambdaQueryWrapper<>();
                wbWrapper.eq(BizWaybill::getReceiverPhone, phone);
                if (StringUtils.hasText(keyword)) {
                    wbWrapper.and(w -> w.like(BizWaybill::getWaybillNumber, keyword)
                            .or().like(BizWaybill::getSenderName, keyword)
                            .or().like(BizWaybill::getSenderPhone, keyword)
                            .or().like(BizWaybill::getReceiverName, keyword));
                }
                wbWrapper.orderByDesc(BizWaybill::getCreatedTime);
                List<BizWaybill> waybills = waybillMapper.selectList(wbWrapper);
                for (BizWaybill wb : waybills) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "receive");
                    item.put("id", wb.getId());
                    item.put("orderId", wb.getOrderId());
                    item.put("number", wb.getWaybillNumber());
                    item.put("status", wb.getStatus());
                    item.put("statusText", getWaybillStatusText(wb.getStatus()));
                    item.put("senderName", wb.getSenderName());
                    item.put("senderPhone", wb.getSenderPhone());
                    item.put("receiverName", wb.getReceiverName());
                    item.put("receiverPhone", wb.getReceiverPhone());
                    item.put("goodsName", wb.getGoodsName());
                    item.put("freight", wb.getFreight());
                    item.put("createdTime", wb.getCreatedTime());
                    result.add(item);
                }
            }
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

    /** 内部使用，不校验用户归属（供 TrackService 等调用） */
    public BizOrder getOrderDetailInternal(Long id) {
        BizOrder order = orderMapper.selectById(id);
        if (order == null) {
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

    /**
     * 用户下单：创建订单并自动生成一条待分配的揽收任务。
     */
    @Transactional
    public BizOrder createOrder(BizOrder order, Long userId) {
        order.setUserId(userId);
        order.setSource(1);
        orderService.create(order);

        // 广播新揽收任务给所有在线快递员
        com.wanshu.web.websocket.WebSocketNotifyServer.broadcast(
                String.format("{\"type\":\"new_pickup_task\",\"orderId\":\"%s\",\"message\":\"有新的揽收任务，请及时抢单\"}",
                        order.getId()));

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
     * 运费估算：优先查询同城寄(type=1)运费模板，无模板时降级为固定费率。
     */
    public Map<String, Object> estimateFreight(BigDecimal weight, String goodsType) {
        Map<String, Object> result = new HashMap<>();
        result.put("note", "参考价格，实际以揽件后计费为准");
        BigDecimal safeWeight = (weight == null || weight.compareTo(BigDecimal.ZERO) <= 0)
                ? BigDecimal.ONE : weight;
        try {
            BaseFreightTemplate template = freightTemplateService.page(1, 1, null, 1).getRecords()
                    .stream().findFirst().orElse(null);
            if (template != null) {
                Map<String, Object> calc = freightTemplateService.calculateFreight(
                        template.getId(), safeWeight, null, null, null, null);
                Object raw = calc.get("freightAmountRaw");
                if (raw != null) {
                    result.put("estimatedFee", raw);
                    result.put("firstWeight", calc.getOrDefault("firstWeight", "0"));
                    result.put("continuousWeight", calc.getOrDefault("continuousWeight", "0"));
                    result.put("templateName", template.getTemplateName());
                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("运费模板计算失败，降级为固定费率: {}", e.getMessage());
        }
        // 降级：固定费率 首重12元，续重3元/kg
        BigDecimal fee = new BigDecimal("12.00").add(
                safeWeight.subtract(BigDecimal.ONE).max(BigDecimal.ZERO)
                        .multiply(new BigDecimal("3.00")));
        result.put("estimatedFee", fee);
        result.put("firstWeight", "12.00");
        result.put("continuousWeight", "3.00");
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

    private String getWaybillStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待揽收";
            case 1 -> "已揽收";
            case 2 -> "运输中";
            case 3 -> "派送中";
            case 4 -> "已签收";
            case 5 -> "已拒收";
            default -> "未知";
        };
    }
}
