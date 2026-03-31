package com.wanshu.web.service.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.base.service.EmpCourierService;
import com.wanshu.business.mapper.BizDeliveryTaskMapper;
import com.wanshu.business.mapper.BizOrderMapper;
import com.wanshu.business.mapper.BizPickupTaskMapper;
import com.wanshu.business.mapper.BizWaybillMapper;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.biz.*;
import com.wanshu.model.entity.emp.EmpCourier;
import com.wanshu.model.entity.track.TrackRoute;
import com.wanshu.web.repository.TrackRouteRepository;
import com.wanshu.web.service.mq.WaybillDispatchListener;
import com.wanshu.web.service.mq.WaybillMqProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 快递员端业务：揽收 & 派件
 *
 * <pre>
 * 揽收流程：
 *   快递员确认揽收 → 更新揽收任务状态 → 订单转运单（含路线规划）→ 更新订单状态
 *   → 发送"新运单"消息到 MQ → 调度中心处理
 *
 * 派件流程：
 *   快递员确认派件结果 → 签收：更新运单/订单为已签收
 *                      → 拒收：更新运单/订单为已拒收 + 发送重新调度消息
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppCourierService {

    private final BizPickupTaskMapper pickupTaskMapper;
    private final BizDeliveryTaskMapper deliveryTaskMapper;
    private final BizOrderMapper orderMapper;
    private final BizWaybillMapper waybillMapper;
    private final BaseOrganMapper organMapper;
    private final EmpCourierService courierService;
    private final WaybillMqProducer mqProducer;
    private final WaybillDispatchListener dispatchListener;
    private final TrackNodeService trackNodeService;
    private final TrackRouteRepository trackRouteRepository;

    // =====================================================================
    // 揽收任务
    // =====================================================================

    /**
     * 查询快递员的揽收任务列表（按状态筛选，不传则返回全部）
     * <p>
     * 包含两类任务：1）已分配给本人的；2）待分配（courier_id 为空且 status=0）的公共池，便于抢单/揽收。
     * 仅订单表有数据但从未分配快递员时，属于第 2 类，否则列表会一直为空。
     */
    public List<Map<String, Object>> listPickupTasks(Long courierId, Integer status) {
        LambdaQueryWrapper<BizPickupTask> wrapper = new LambdaQueryWrapper<>();
        // 显式 SQL，避免 nested or/and 在 MP 下解析歧义：本人任务 OR 待抢公共池（未分配且待处理）
        wrapper.apply("(courier_id = {0} OR (courier_id IS NULL AND status = 0))", courierId);
        if (status != null) {
            wrapper.eq(BizPickupTask::getStatus, status);
        }
        wrapper.orderByDesc(BizPickupTask::getCreatedTime);
        List<BizPickupTask> tasks = pickupTaskMapper.selectList(wrapper);
        return tasks.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("taskNumber", t.getTaskNumber());
            m.put("orderId", t.getOrderId());
            m.put("waybillId", t.getWaybillId());
            m.put("status", t.getStatus());
            m.put("statusText", pickupStatusText(t.getStatus()));
            m.put("assignTime", t.getAssignTime());
            m.put("pickupTime", t.getPickupTime());
            m.put("remark", t.getRemark());
            BizOrder order = orderMapper.selectById(t.getOrderId());
            if (order != null) {
                m.put("senderName", order.getSenderName());
                m.put("senderPhone", order.getSenderPhone());
                m.put("senderAddress", order.getSenderAddress());
                m.put("goodsName", order.getGoodsName());
                m.put("weight", order.getWeight());
            }
            return m;
        }).toList();
    }

    /**
     * 快递员确认揽收：
     * 1. 更新揽收任务为已完成
     * 2. 订单转运单（路线规划：寄件机构 = 快递员机构，收件机构 = 按收件人城市匹配）
     * 3. 更新订单状态为已揽收（2）
     * 4. 发送"新运单"消息
     */
    @Transactional
    public void completePickup(Long taskId, Long courierId, String remark) {
        BizPickupTask task = pickupTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("揽收任务不存在");
        }
        // 待分配：先归到当前快递员名下（抢单），再进入揽收
        if (Integer.valueOf(0).equals(task.getStatus()) && task.getCourierId() == null) {
            BizPickupTask assign = new BizPickupTask();
            assign.setId(taskId);
            assign.setCourierId(courierId);
            assign.setAssignTime(LocalDateTime.now());
            assign.setStatus(1);
            pickupTaskMapper.updateById(assign);
            task = pickupTaskMapper.selectById(taskId);

            // 抢单成功：同步将订单标记为"已接单"（status=1）
            BizOrder acceptedOrder = orderMapper.selectById(task.getOrderId());
            if (acceptedOrder != null) {
                BizOrder orderAccept = new BizOrder();
                orderAccept.setId(acceptedOrder.getId());
                orderAccept.setStatus(1);
                orderMapper.updateById(orderAccept);
                log.info("[揽收] 抢单成功，订单已接单，orderId={}", acceptedOrder.getId());
            }
        }
        if (task.getCourierId() == null || !courierId.equals(task.getCourierId())) {
            throw new BusinessException("无权操作该揽收任务");
        }
        if (task.getStatus() != 1) {
            throw new BusinessException("任务状态不允许揽收（需处于已分配状态）");
        }

        BizOrder order = orderMapper.selectById(task.getOrderId());
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }

        // 确定寄件机构：使用快递员所属机构
        EmpCourier courier = courierService.getById(courierId);
        Long sendOrganId = (courier != null) ? courier.getOrganId() : null;

        // 确定收件机构：按收件人城市匹配最近的末端网点（organType=3）
        Long receiveOrganId = matchOrganByRegion(
                order.getReceiverCityId(), order.getReceiverCountyId());

        // 创建运单
        BizWaybill waybill = buildWaybill(order, sendOrganId, receiveOrganId);
        waybillMapper.insert(waybill);

        // 更新揽收任务
        BizPickupTask taskUpdate = new BizPickupTask();
        taskUpdate.setId(taskId);
        taskUpdate.setWaybillId(waybill.getId());
        taskUpdate.setPickupTime(LocalDateTime.now());
        taskUpdate.setStatus(2);
        if (StringUtils.hasText(remark)) {
            taskUpdate.setRemark(remark);
        }
        pickupTaskMapper.updateById(taskUpdate);

        // 更新订单状态 → 已揽收（2）
        BizOrder orderUpdate = new BizOrder();
        orderUpdate.setId(order.getId());
        orderUpdate.setStatus(2);
        orderMapper.updateById(orderUpdate);

        // 写入物流跟踪节点：已揽收
        trackNodeService.recordPickup(waybill.getId(), courierId);

        // 异步触发轨迹路线规划（查途经转运中心 → Coze → MongoDB）
        mqProducer.sendTrackCreate(waybill.getId());

        // 同一网点：跳过运输调度，直接创建派件任务
        if (sendOrganId != null && sendOrganId.equals(receiveOrganId)) {
            log.info("[揽收] 寄收件同一网点(organId={})，跳过运输调度，直接创建派件任务，waybillId={}",
                    sendOrganId, waybill.getId());
            dispatchListener.createDeliveryTask(waybill);
        } else {
            // 不同网点：发送新运单消息到调度中心走正常运输流程
            mqProducer.sendNewWaybill(waybill.getId());
            log.info("[揽收] 运单已创建并发送调度消息，waybillId={}", waybill.getId());
        }
    }

    // =====================================================================
    // 派件任务
    // =====================================================================

    /**
     * 查询快递员的派件任务列表
     */
    public List<Map<String, Object>> listDeliveryTasks(Long courierId, Integer status) {
        LambdaQueryWrapper<BizDeliveryTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeliveryTask::getCourierId, courierId);
        if (status != null) {
            wrapper.eq(BizDeliveryTask::getStatus, status);
        }
        wrapper.orderByDesc(BizDeliveryTask::getCreatedTime);
        List<BizDeliveryTask> tasks = deliveryTaskMapper.selectList(wrapper);
        return tasks.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("taskNumber", t.getTaskNumber());
            m.put("waybillId", t.getWaybillId());
            m.put("status", t.getStatus());
            m.put("statusText", deliveryStatusText(t.getStatus()));
            m.put("assignTime", t.getAssignTime());
            m.put("deliveryTime", t.getDeliveryTime());
            BizWaybill waybill = waybillMapper.selectById(t.getWaybillId());
            if (waybill != null) {
                m.put("waybillNumber", waybill.getWaybillNumber());
                m.put("receiverName", waybill.getReceiverName());
                m.put("receiverPhone", waybill.getReceiverPhone());
                m.put("receiverAddress", waybill.getReceiverAddress());
                m.put("goodsName", waybill.getGoodsName());
                m.put("weight", waybill.getWeight());
            }
            return m;
        }).toList();
    }

    /**
     * 快递员确认签收：
     * 1. 更新派件任务为已完成（2）
     * 2. 更新运单状态为已签收（4），记录签收图片与时间
     * 3. 更新关联订单状态为已签收（5）
     */
    @Transactional
    public void signDelivery(Long taskId, Long courierId, Integer signType, String signImage) {
        BizDeliveryTask task = validateDeliveryTask(taskId, courierId);

        LocalDateTime now = LocalDateTime.now();

        // 更新派件任务
        BizDeliveryTask taskUpdate = new BizDeliveryTask();
        taskUpdate.setId(taskId);
        taskUpdate.setStatus(2);
        taskUpdate.setSignType(signType);
        taskUpdate.setSignImage(signImage);
        taskUpdate.setDeliveryTime(now);
        deliveryTaskMapper.updateById(taskUpdate);

        // 更新运单 → 已签收（4）
        BizWaybill waybillUpdate = new BizWaybill();
        waybillUpdate.setId(task.getWaybillId());
        waybillUpdate.setStatus(4);
        waybillUpdate.setSignImage(signImage);
        waybillUpdate.setSignTime(now);
        waybillMapper.updateById(waybillUpdate);

        // 更新订单 → 已签收（5）
        updateOrderStatusByWaybillId(task.getWaybillId(), 5);

        // 写入物流跟踪节点：已签收
        trackNodeService.recordSigned(task.getWaybillId(), courierId);

        // 发送轨迹完成消息：标记 TrackRoute.status=1，之后不再更新位置
        mqProducer.sendTrackComplete(task.getWaybillId());
        log.info("[派件] 签收完成，taskId={}", taskId);
    }

    /**
     * 快递员确认拒收：
     * 1. 更新派件任务为已拒收（3）
     * 2. 更新运单状态为已拒收（5）
     * 3. 更新关联订单状态为已拒收（6）
     * 4. 发送重新调度消息（调度中心重新分配派件快递员）
     */
    @Transactional
    public void rejectDelivery(Long taskId, Long courierId, String reason) {
        BizDeliveryTask task = validateDeliveryTask(taskId, courierId);

        // 更新派件任务 → 已拒收（3）
        BizDeliveryTask taskUpdate = new BizDeliveryTask();
        taskUpdate.setId(taskId);
        taskUpdate.setStatus(3);
        taskUpdate.setRejectReason(reason);
        taskUpdate.setDeliveryTime(LocalDateTime.now());
        deliveryTaskMapper.updateById(taskUpdate);

        // 更新运单 → 已拒收（5）
        BizWaybill waybillUpdate = new BizWaybill();
        waybillUpdate.setId(task.getWaybillId());
        waybillUpdate.setStatus(5);
        waybillMapper.updateById(waybillUpdate);

        // 更新订单 → 已拒收（6）
        updateOrderStatusByWaybillId(task.getWaybillId(), 6);

        // 写入物流跟踪节点：已拒收
        trackNodeService.recordRejected(task.getWaybillId(), courierId, reason);

        // 发送重新调度消息：调度中心将为该运单重新创建派件任务
        mqProducer.sendPendingDispatch(task.getWaybillId());
        log.info("[派件] 拒收，已发送重新调度消息，waybillId={}", task.getWaybillId());
    }

    // =====================================================================
    // 内部辅助
    // =====================================================================

    private BizDeliveryTask validateDeliveryTask(Long taskId, Long courierId) {
        BizDeliveryTask task = deliveryTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("派件任务不存在");
        }
        if (!courierId.equals(task.getCourierId())) {
            throw new BusinessException("无权操作该派件任务");
        }
        if (task.getStatus() != 1) {
            throw new BusinessException("任务状态不允许操作（需处于已分配状态）");
        }
        return task;
    }

    /**
     * 按收件人城市/区县匹配末端网点（organType=3）；
     * 优先精确匹配 countyId，次之 cityId，都找不到时返回 null。
     */
    private Long matchOrganByRegion(Long cityId, Long countyId) {
        if (countyId != null) {
            BaseOrgan organ = organMapper.selectOne(
                    new LambdaQueryWrapper<BaseOrgan>()
                            .eq(BaseOrgan::getOrganType, 3)
                            .eq(BaseOrgan::getCountyId, countyId)
                            .eq(BaseOrgan::getStatus, 1)
                            .last("LIMIT 1"));
            if (organ != null) {
                return organ.getId();
            }
        }
        if (cityId != null) {
            BaseOrgan organ = organMapper.selectOne(
                    new LambdaQueryWrapper<BaseOrgan>()
                            .eq(BaseOrgan::getOrganType, 3)
                            .eq(BaseOrgan::getCityId, cityId)
                            .eq(BaseOrgan::getStatus, 1)
                            .last("LIMIT 1"));
            if (organ != null) {
                return organ.getId();
            }
        }
        // 兜底：取任意启用的末端网点
        BaseOrgan organ = organMapper.selectOne(
                new LambdaQueryWrapper<BaseOrgan>()
                        .eq(BaseOrgan::getOrganType, 3)
                        .eq(BaseOrgan::getStatus, 1)
                        .last("LIMIT 1"));
        return organ != null ? organ.getId() : null;
    }

    /**
     * 根据订单信息构建运单（订单转运单）
     */
    private BizWaybill buildWaybill(BizOrder order, Long sendOrganId, Long receiveOrganId) {
        BizWaybill waybill = new BizWaybill();
        waybill.setWaybillNumber(generateWaybillNumber());
        waybill.setOrderId(order.getId());
        waybill.setSenderName(order.getSenderName());
        waybill.setSenderPhone(order.getSenderPhone());
        waybill.setSenderAddress(order.getSenderAddress());
        waybill.setReceiverName(order.getReceiverName());
        waybill.setReceiverPhone(order.getReceiverPhone());
        waybill.setReceiverAddress(order.getReceiverAddress());
        waybill.setGoodsName(order.getGoodsName());
        waybill.setWeight(order.getWeight());
        waybill.setVolume(order.getVolume());
        waybill.setFreight(order.getActualFee() != null
                ? order.getActualFee() : order.getEstimatedFee());
        waybill.setSendOrganId(sendOrganId);
        waybill.setReceiveOrganId(receiveOrganId);
        waybill.setStatus(1); // 已揽收
        return waybill;
    }

    private void updateOrderStatusByWaybillId(Long waybillId, int orderStatus) {
        BizWaybill waybill = waybillMapper.selectById(waybillId);
        if (waybill == null || waybill.getOrderId() == null) {
            return;
        }
        BizOrder orderUpdate = new BizOrder();
        orderUpdate.setId(waybill.getOrderId());
        orderUpdate.setStatus(orderStatus);
        orderMapper.updateById(orderUpdate);
    }

    private String generateWaybillNumber() {
        return "YD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    private String pickupStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待分配";
            case 1 -> "已分配";
            case 2 -> "已完成";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

    private String deliveryStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待分配";
            case 1 -> "已分配";
            case 2 -> "已签收";
            case 3 -> "已拒收";
            default -> "未知";
        };
    }

    // =====================================================================
    // 位置上报
    // =====================================================================

    /**
     * 快递员上报当前位置，更新关联运单的轨迹实时坐标。
     *
     * @param waybillId 运单 ID
     * @param longitude 经度
     * @param latitude  纬度
     */
    public void updateLocation(Long waybillId, double longitude, double latitude) {
        trackRouteRepository.findByWaybillId(waybillId).ifPresentOrElse(route -> {
            if (Integer.valueOf(1).equals(route.getStatus())) {
                log.info("[位置上报-快递员] 轨迹已完成，忽略上报，waybillId={}", waybillId);
                return;
            }
            route.setCurrentLocation(new TrackRoute.GeoPoint(longitude, latitude));
            route.setLastLocationTime(LocalDateTime.now());
            trackRouteRepository.save(route);
            log.info("[位置上报-快递员] waybillId={} lng={} lat={}", waybillId, longitude, latitude);
        }, () -> log.warn("[位置上报-快递员] 未找到轨迹记录，waybillId={}", waybillId));
    }
}
