package com.wanshu.web.service.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wanshu.base.mapper.BaseVehicleDriverMapper;
import com.wanshu.base.mapper.BaseVehicleMapper;
import com.wanshu.base.service.EmpCourierService;
import com.wanshu.business.mapper.BizDeliveryTaskMapper;
import com.wanshu.business.mapper.BizOrderMapper;
import com.wanshu.business.mapper.BizWaybillMapper;
import com.wanshu.common.utils.CozeUtil;
import com.wanshu.dispatch.mapper.DispatchLineMapper;
import com.wanshu.dispatch.mapper.DispatchTransportTaskMapper;
import com.wanshu.dispatch.mapper.DispatchTransportTaskWaybillMapper;
import com.wanshu.dispatch.mapper.DispatchTripMapper;
import com.wanshu.model.entity.base.BaseVehicle;
import com.wanshu.model.entity.base.BaseVehicleDriver;
import com.wanshu.model.entity.biz.BizDeliveryTask;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.biz.BizWaybill;
import com.wanshu.model.entity.dispatch.DispatchLine;
import com.wanshu.model.entity.dispatch.DispatchTransportTask;
import com.wanshu.model.entity.dispatch.DispatchTransportTaskWaybill;
import com.wanshu.model.entity.dispatch.DispatchTrip;
import com.wanshu.model.entity.emp.EmpCourier;
import com.wanshu.web.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 调度中心：
 *
 * <pre>
 * 1. 监听"新运单"队列 → 加入待调度池（将运单状态置为"已揽收待调度"）
 * 2. 监听"重新调度"队列（拒收后）→ 重新创建派件任务
 * 3. 定时任务（每30秒）：
 *    - 查询所有"待调度"运单
 *    - 按路线（sendOrganId+receiveOrganId）合并运单
 *    - 查询可用车辆（运力计算）
 *    - 生成运输任务 & 关联运单
 *    - 标记运单为"运输中"，更新订单状态
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WaybillDispatchListener {

    /** 运单状态：已揽收=1，待调度=待分配入运输任务；运输中=2；派送中=3；已签收=4；已拒收=5 */
    private static final int WAYBILL_STATUS_PICKED_UP   = 1;
    private static final int WAYBILL_STATUS_IN_TRANSIT  = 2;
    private static final int WAYBILL_STATUS_DELIVERING  = 3;

    /** 运输任务状态：待出发=0，运输中=1，已完成=2 */
    private static final int TRANSPORT_STATUS_PENDING   = 0;

    /** 车辆状态：可用=1 */
    private static final int VEHICLE_STATUS_AVAILABLE   = 1;

    private final BizWaybillMapper waybillMapper;
    private final BizOrderMapper orderMapper;
    private final BizDeliveryTaskMapper deliveryTaskMapper;
    private final DispatchTransportTaskMapper transportTaskMapper;
    private final DispatchTransportTaskWaybillMapper taskWaybillMapper;
    private final DispatchLineMapper lineMapper;
    private final DispatchTripMapper tripMapper;
    private final BaseVehicleMapper vehicleMapper;
    private final BaseVehicleDriverMapper vehicleDriverMapper;
    private final EmpCourierService courierService;
    private final com.wanshu.web.service.app.TrackNodeService trackNodeService;
    private final CozeUtil cozeUtil;

    // =====================================================================
    // MQ 监听：新运单
    // =====================================================================

    /**
     * 收到新运单消息：将运单标记为"已揽收、待分配运输任务"（status 保持 1，等待定时调度处理）。
     */
    @RabbitListener(queues = RabbitMqConfig.QUEUE_WAYBILL_NEW)
    public void onNewWaybill(Long waybillId) {
        log.info("[调度中心] 收到新运单通知，waybillId={}", waybillId);
        BizWaybill waybill = waybillMapper.selectById(waybillId);
        if (waybill == null) {
            log.warn("[调度中心] 运单不存在，忽略，waybillId={}", waybillId);
            return;
        }
        if (waybill.getStatus() != WAYBILL_STATUS_PICKED_UP) {
            log.info("[调度中心] 运单状态已变更（{}），跳过，waybillId={}", waybill.getStatus(), waybillId);
            return;
        }
        // 直接触发一次针对此运单的调度（也可等待定时任务批量处理）
        dispatchSingleWaybill(waybill);
    }

    // =====================================================================
    // MQ 监听：重新调度（拒收后）
    // =====================================================================

    /**
     * 收到拒收重新调度消息：重新为运单创建派件任务并分配快递员。
     */
    @RabbitListener(queues = RabbitMqConfig.QUEUE_DISPATCH_PENDING)
    public void onPendingDispatch(Long waybillId) {
        log.info("[调度中心] 收到重新调度通知（拒收），waybillId={}", waybillId);
        BizWaybill waybill = waybillMapper.selectById(waybillId);
        if (waybill == null) {
            log.warn("[调度中心] 运单不存在，忽略，waybillId={}", waybillId);
            return;
        }
        // 将运单状态重置为派送中（3），重新创建派件任务
        BizWaybill upd = new BizWaybill();
        upd.setId(waybillId);
        upd.setStatus(WAYBILL_STATUS_DELIVERING);
        waybillMapper.updateById(upd);

        createDeliveryTask(waybill);
    }

    // =====================================================================
    // 定时任务调度（每30秒执行一次）
    // =====================================================================

    /**
     * 定时批量调度：
     * 查询所有状态为"已揽收（1）"且尚未分配运输任务的运单 → 按路线合并 → 生成运输任务。
     */
    @Scheduled(fixedDelay = 30_000)
    public void scheduledDispatch() {
        log.debug("[调度中心-定时] 开始批量调度未分配运单...");

        // 查询已揽收但尚未关联运输任务的运单
        List<BizWaybill> pending = waybillMapper.selectList(
                new LambdaQueryWrapper<BizWaybill>()
                        .eq(BizWaybill::getStatus, WAYBILL_STATUS_PICKED_UP));

        if (pending.isEmpty()) {
            return;
        }

        // 过滤掉已经关联了运输任务的运单
        List<Long> alreadyLinked = taskWaybillMapper.selectList(
                        new LambdaQueryWrapper<DispatchTransportTaskWaybill>()
                                .in(DispatchTransportTaskWaybill::getWaybillId,
                                        pending.stream().map(BizWaybill::getId).toList()))
                .stream().map(DispatchTransportTaskWaybill::getWaybillId).toList();

        List<BizWaybill> toDispatch = pending.stream()
                .filter(w -> !alreadyLinked.contains(w.getId()))
                .toList();

        if (toDispatch.isEmpty()) {
            return;
        }

        log.info("[调度中心-定时] 待调度运单数量：{}", toDispatch.size());

        // 按路线（sendOrganId → receiveOrganId）分组
        toDispatch.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        w -> w.getSendOrganId() + "_" + w.getReceiveOrganId()))
                .forEach((routeKey, waybills) -> {
                    try {
                        dispatchWaybillGroup(waybills);
                    } catch (Exception e) {
                        log.error("[调度中心-定时] 路线 {} 调度失败：{}", routeKey, e.getMessage(), e);
                    }
                });
    }

    // =====================================================================
    // 核心调度逻辑
    // =====================================================================

    /**
     * 对单个运单执行调度（由 MQ 触发）
     */
    @Transactional
    public void dispatchSingleWaybill(BizWaybill waybill) {
        // 查找同方向待调度的其他运单，合并一起处理（合并运单）
        List<BizWaybill> sameLine = waybillMapper.selectList(
                new LambdaQueryWrapper<BizWaybill>()
                        .eq(BizWaybill::getStatus, WAYBILL_STATUS_PICKED_UP)
                        .eq(BizWaybill::getSendOrganId, waybill.getSendOrganId())
                        .eq(BizWaybill::getReceiveOrganId, waybill.getReceiveOrganId()));
        dispatchWaybillGroup(sameLine);
    }

    /**
     * 将同路线的一批运单分配到运输任务（运力计算 + 生成运输任务）
     */
    @Transactional
    public void dispatchWaybillGroup(List<BizWaybill> waybills) {
        if (waybills == null || waybills.isEmpty()) {
            return;
        }

        Long sendOrganId    = waybills.get(0).getSendOrganId();
        Long receiveOrganId = waybills.get(0).getReceiveOrganId();

        // 过滤已经关联了运输任务的运单（避免重复处理）
        List<Long> existingLinks = taskWaybillMapper.selectList(
                        new LambdaQueryWrapper<DispatchTransportTaskWaybill>()
                                .in(DispatchTransportTaskWaybill::getWaybillId,
                                        waybills.stream().map(BizWaybill::getId).toList()))
                .stream().map(DispatchTransportTaskWaybill::getWaybillId).toList();

        List<BizWaybill> unlinked = waybills.stream()
                .filter(w -> !existingLinks.contains(w.getId()))
                .toList();

        if (unlinked.isEmpty()) {
            return;
        }

        // 预计算待调度运单总重量/总体积（用于运力校验）
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        boolean hasWeightData = false;
        boolean hasVolumeData = false;
        for (BizWaybill w : unlinked) {
            if (w.getWeight() != null) { totalWeight = totalWeight.add(w.getWeight()); hasWeightData = true; }
            if (w.getVolume() != null) { totalVolume = totalVolume.add(w.getVolume()); hasVolumeData = true; }
        }
        if (!hasWeightData && !hasVolumeData) {
            log.warn("[调度中心] 本批 {} 条运单均无重量/体积数据，跳过运力校验，路线 {}→{}",
                    unlinked.size(), sendOrganId, receiveOrganId);
        }

        // 查询可用车辆（含运力校验：载重/体积必须满足本批运单需求）
        BaseVehicle vehicle = findAvailableVehicle(sendOrganId, totalWeight, totalVolume);
        if (vehicle == null) {
            log.warn("[调度中心] 暂无满足运力要求的车辆，路线 {}→{}，{}条运单暂不调度（需载重≥{} kg，体积≥{} m³）",
                    sendOrganId, receiveOrganId, unlinked.size(), totalWeight, totalVolume);
            // 调度失败时 fallback：异步请求 Coze AI 给出辅助选单建议
            requestCozeDispatchAdvice(sendOrganId, receiveOrganId, unlinked, totalWeight, totalVolume);
            return;
        }

        // 查找匹配线路
        DispatchLine line = findLine(sendOrganId, receiveOrganId);

        // 查找车次
        DispatchTrip trip = (line != null) ? findTrip(line.getId()) : null;

        // 生成运输任务（标记运单已调度）
        DispatchTransportTask task = buildTransportTask(vehicle, line, trip,
                sendOrganId, receiveOrganId, unlinked);
        transportTaskMapper.insert(task);

        // 关联运单与运输任务
        LocalDateTime now = LocalDateTime.now();
        for (BizWaybill w : unlinked) {
            DispatchTransportTaskWaybill link = new DispatchTransportTaskWaybill();
            link.setTransportTaskId(task.getId());
            link.setWaybillId(w.getId());
            link.setCreatedTime(now);
            taskWaybillMapper.insert(link);

            // 更新运单状态 → 运输中（2）
            BizWaybill waybillUpd = new BizWaybill();
            waybillUpd.setId(w.getId());
            waybillUpd.setStatus(WAYBILL_STATUS_IN_TRANSIT);
            waybillMapper.updateById(waybillUpd);

            // 更新订单状态 → 运输中（3）
            if (w.getOrderId() != null) {
                BizOrder orderUpd = new BizOrder();
                orderUpd.setId(w.getOrderId());
                orderUpd.setStatus(3);
                orderMapper.updateById(orderUpd);
            }
        }

        // 回填实际装载量
        DispatchTransportTask taskUpd = new DispatchTransportTask();
        taskUpd.setId(task.getId());
        taskUpd.setLoadWeight(totalWeight);
        taskUpd.setLoadVolume(totalVolume);
        taskUpd.setWaybillCount(unlinked.size());
        transportTaskMapper.updateById(taskUpd);

        log.info("[调度中心] 运输任务已生成，taskId={}，包含运单数={}，车辆={}",
                task.getId(), unlinked.size(), vehicle.getLicensePlate());

        // 推送通知给绑定司机
        if (task.getDriverId() != null) {
            com.wanshu.web.websocket.WebSocketNotifyServer.sendToUser(
                    String.valueOf(task.getDriverId()),
                    String.format("{\"type\":\"new_transport_task\",\"taskId\":\"%s\",\"message\":\"您有新的运输任务，请及时查看\"}",
                            task.getId()));
        }
    }

    // =====================================================================
    // 派件任务创建（由司机到达终点后触发）
    // =====================================================================

    /**
     * 为已到达目的机构的运单创建派件任务并分配快递员（计算快递员负载，选负载最轻的）。
     */
    @Transactional
    public void createDeliveryTask(BizWaybill waybill) {
        // 计算目的机构下各快递员当前待派件任务数（负载）
        Long receiveOrganId = waybill.getReceiveOrganId();
        List<EmpCourier> couriers = courierService.listByOrganId(receiveOrganId);

        Long bestCourierId = null;
        long minLoad = Long.MAX_VALUE;
        for (EmpCourier courier : couriers) {
            if (!Objects.equals(courier.getWorkStatus(), 1)) {
                continue; // 仅考虑上班中的快递员
            }
            long load = deliveryTaskMapper.selectCount(
                    new LambdaQueryWrapper<BizDeliveryTask>()
                            .eq(BizDeliveryTask::getCourierId, courier.getId())
                            .eq(BizDeliveryTask::getStatus, 1));
            if (load < minLoad) {
                minLoad = load;
                bestCourierId = courier.getId();
            }
        }

        // 创建派件任务
        BizDeliveryTask task = new BizDeliveryTask();
        task.setTaskNumber("PJ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000)));
        task.setWaybillId(waybill.getId());
        task.setStatus(bestCourierId != null ? 1 : 0);
        if (bestCourierId != null) {
            task.setCourierId(bestCourierId);
            task.setAssignTime(LocalDateTime.now());
        }
        deliveryTaskMapper.insert(task);

        // 更新运单状态 → 派送中（3）
        BizWaybill waybillUpd = new BizWaybill();
        waybillUpd.setId(waybill.getId());
        waybillUpd.setStatus(WAYBILL_STATUS_DELIVERING);
        waybillMapper.updateById(waybillUpd);

        // 更新订单状态 → 派送中（4）
        if (waybill.getOrderId() != null) {
            BizOrder orderUpd = new BizOrder();
            orderUpd.setId(waybill.getOrderId());
            orderUpd.setStatus(4);
            orderMapper.updateById(orderUpd);
        }

        // 写入物流跟踪节点：正在派送中
        trackNodeService.recordDelivering(waybill.getId(), bestCourierId);

        log.info("[调度中心] 派件任务已创建，waybillId={}，分配快递员={}",
                waybill.getId(), bestCourierId);

        // 推送通知给被分配快递员
        if (bestCourierId != null) {
            com.wanshu.web.websocket.WebSocketNotifyServer.sendToUser(
                    String.valueOf(bestCourierId),
                    String.format("{\"type\":\"new_delivery_task\",\"taskId\":\"%s\",\"message\":\"您有新的派件任务，请及时查看\"}",
                            task.getId()));
        }
    }

    // =====================================================================
    // 辅助方法
    // =====================================================================

    /**
     * 在指定机构下查找一辆可用且运力满足需求的车辆。
     *
     * <p>校验逻辑（对应图4核心分支）：
     * <ul>
     *   <li>车辆状态必须为可用（status=1）</li>
     *   <li>车辆未被进行中的运输任务占用</li>
     *   <li>车辆载重 ≥ 本批运单总重量（kg 向上取整）</li>
     *   <li>车辆体积 ≥ 本批运单总体积（m³ 向上取整）</li>
     * </ul>
     * 若无符合条件的车辆，返回 null，由调用方决定是否等待下次调度。
     */
    private BaseVehicle findAvailableVehicle(Long organId,
                                             BigDecimal requiredWeight,
                                             BigDecimal requiredVolume) {
        // 查找当前运输中任务占用的车辆
        List<Long> busyVehicleIds = transportTaskMapper.selectList(
                        new LambdaQueryWrapper<DispatchTransportTask>()
                                .eq(DispatchTransportTask::getStatus, 1)
                                .isNotNull(DispatchTransportTask::getVehicleId))
                .stream().map(DispatchTransportTask::getVehicleId)
                .filter(Objects::nonNull).toList();

        LambdaQueryWrapper<BaseVehicle> wrapper = new LambdaQueryWrapper<BaseVehicle>()
                .eq(BaseVehicle::getStatus, VEHICLE_STATUS_AVAILABLE);
        if (organId != null) {
            wrapper.eq(BaseVehicle::getOrganId, organId);
        }
        if (!busyVehicleIds.isEmpty()) {
            wrapper.notIn(BaseVehicle::getId, busyVehicleIds);
        }
        // 运力校验：车辆载重/体积必须 ≥ 本批运单所需（向上取整转为 Integer 与数据库字段比较）
        if (requiredWeight != null && requiredWeight.compareTo(BigDecimal.ZERO) > 0) {
            int reqWeightInt = requiredWeight.setScale(0, RoundingMode.CEILING).intValue();
            wrapper.ge(BaseVehicle::getLoadWeight, reqWeightInt);
        }
        if (requiredVolume != null && requiredVolume.compareTo(BigDecimal.ZERO) > 0) {
            int reqVolumeInt = requiredVolume.setScale(0, RoundingMode.CEILING).intValue();
            wrapper.ge(BaseVehicle::getLoadVolume, reqVolumeInt);
        }
        wrapper.last("LIMIT 1");
        return vehicleMapper.selectOne(wrapper);
    }

    private DispatchLine findLine(Long sendOrganId, Long receiveOrganId) {
        if (sendOrganId == null || receiveOrganId == null) {
            return null;
        }
        return lineMapper.selectOne(
                new LambdaQueryWrapper<DispatchLine>()
                        .eq(DispatchLine::getStartOrganId, sendOrganId)
                        .eq(DispatchLine::getEndOrganId, receiveOrganId)
                        .eq(DispatchLine::getStatus, 1)
                        .last("LIMIT 1"));
    }

    private DispatchTrip findTrip(Long lineId) {
        return tripMapper.selectOne(
                new LambdaQueryWrapper<DispatchTrip>()
                        .eq(DispatchTrip::getLineId, lineId)
                        .eq(DispatchTrip::getStatus, 1)
                        .last("LIMIT 1"));
    }

    private DispatchTransportTask buildTransportTask(BaseVehicle vehicle,
                                                     DispatchLine line,
                                                     DispatchTrip trip,
                                                     Long startOrganId, Long endOrganId,
                                                     List<BizWaybill> waybills) {
        DispatchTransportTask task = new DispatchTransportTask();
        task.setTaskNumber("YS" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000)));
        task.setVehicleId(vehicle != null ? vehicle.getId() : null);
        if (vehicle != null) {
            BaseVehicleDriver vd = vehicleDriverMapper.selectOne(
                    new LambdaQueryWrapper<BaseVehicleDriver>()
                            .eq(BaseVehicleDriver::getVehicleId, vehicle.getId())
                            .last("LIMIT 1"));
            if (vd != null) {
                task.setDriverId(vd.getDriverId());
            } else {
                log.warn("[调度中心] 车辆 {} 未绑定司机，运输任务将无司机分配", vehicle.getId());
            }
        }
        task.setStartOrganId(startOrganId);
        task.setEndOrganId(endOrganId);
        if (line != null) {
            task.setLineId(line.getId());
        }
        if (trip != null) {
            task.setTripId(trip.getId());
            if (trip.getDepartTime() != null) {
                task.setPlanDepartTime(LocalDateTime.now().toLocalDate()
                        .atTime(trip.getDepartTime()));
            }
            if (trip.getDepartTime() != null && trip.getDurationMinutes() != null) {
                task.setPlanArriveTime(task.getPlanDepartTime()
                        .plusMinutes(trip.getDurationMinutes()));
            }
        }
        task.setWaybillCount(waybills.size());
        task.setStatus(TRANSPORT_STATUS_PENDING);
        return task;
    }

    // =====================================================================
    // Coze AI 辅助调度（调度失败时 fallback）
    // =====================================================================

    /**
     * 当常规调度无法找到满足运力需求的车辆时，异步请求 Coze 调度 Bot 给出辅助选单建议。
     *
     * <p>调用逻辑：
     * <ol>
     *   <li>收集待调度运单摘要（数量、总重量/体积、货物类型）</li>
     *   <li>查询当前系统内所有状态可用的车辆（不限机构，供 AI 参考）</li>
     *   <li>查询与该路线相关的调度线路</li>
     *   <li>将上述信息拼成自然语言 prompt，发给 Coze dispatch-bot</li>
     *   <li>记录 AI 返回的建议方案到日志（管理端可通过日志平台查看）</li>
     * </ol>
     * 整个过程在独立线程中执行，不阻塞调度主流程。
     */
    private void requestCozeDispatchAdvice(Long sendOrganId, Long receiveOrganId,
                                           List<BizWaybill> waybills,
                                           BigDecimal totalWeight, BigDecimal totalVolume) {
        // 收集运单摘要（货物名称去重后列举，最多展示5种）
        String goodsSummary = waybills.stream()
                .map(BizWaybill::getGoodsName)
                .filter(Objects::nonNull)
                .distinct()
                .limit(5)
                .collect(Collectors.joining("、"));

        // 查询系统内所有可用车辆（不限机构，供 AI 参考全局资源）
        List<BaseVehicle> allVehicles = vehicleMapper.selectList(
                new LambdaQueryWrapper<BaseVehicle>()
                        .eq(BaseVehicle::getStatus, VEHICLE_STATUS_AVAILABLE));

        String vehicleSummary;
        if (allVehicles.isEmpty()) {
            vehicleSummary = "当前系统无可用车辆";
        } else {
            vehicleSummary = allVehicles.stream()
                    .limit(10)
                    .map(v -> String.format("车牌[%s] 载重%dkg 体积%dm³ 所属机构%d",
                            v.getLicensePlate(),
                            v.getLoadWeight() != null ? v.getLoadWeight() : 0,
                            v.getLoadVolume() != null ? v.getLoadVolume() : 0,
                            v.getOrganId() != null ? v.getOrganId() : 0))
                    .collect(Collectors.joining("\n"));
        }

        // 查询相关调度线路
        List<DispatchLine> relatedLines = lineMapper.selectList(
                new LambdaQueryWrapper<DispatchLine>()
                        .eq(DispatchLine::getStatus, 1)
                        .and(w -> w.eq(DispatchLine::getStartOrganId, sendOrganId)
                                .or().eq(DispatchLine::getEndOrganId, receiveOrganId)));

        String lineSummary;
        if (relatedLines.isEmpty()) {
            lineSummary = "无匹配线路";
        } else {
            lineSummary = relatedLines.stream()
                    .limit(5)
                    .map(l -> String.format("线路[%d] %d→%d", l.getId(),
                            l.getStartOrganId(), l.getEndOrganId()))
                    .collect(Collectors.joining("；"));
        }

        String prompt = String.format(
                "【物流调度辅助决策请求】\n\n" +
                "当前时间：%s\n" +
                "待调度路线：机构%d → 机构%d\n" +
                "待调度运单数量：%d 单\n" +
                "货物总重量：%s kg\n" +
                "货物总体积：%s m³\n" +
                "货物类型：%s\n\n" +
                "【系统当前可用车辆（共%d辆）】\n%s\n\n" +
                "【相关调度线路】\n%s\n\n" +
                "问题描述：本批运单无法找到满足运力（载重≥%s kg，体积≥%s m³）的可用车辆完成调度。\n\n" +
                "请根据以上信息，给出调度建议：\n" +
                "1. 是否可以拆分运单分批次调度？建议如何拆分？\n" +
                "2. 是否有其他机构的车辆可以调配支援？\n" +
                "3. 建议的等待时间窗口（预计何时会有可用运力）？\n" +
                "4. 其他优化建议？\n\n" +
                "请用简洁的中文给出具体可执行的建议方案。",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                sendOrganId, receiveOrganId,
                waybills.size(),
                totalWeight.toPlainString(),
                totalVolume.toPlainString(),
                goodsSummary.isEmpty() ? "未知" : goodsSummary,
                allVehicles.size(),
                vehicleSummary,
                lineSummary,
                totalWeight.toPlainString(),
                totalVolume.toPlainString());

        // 异步执行，避免阻塞调度主流程
        CompletableFuture.runAsync(() -> {
            try {
                log.info("[调度中心-AI] 开始请求 Coze 调度建议，路线 {}→{}，{}条运单",
                        sendOrganId, receiveOrganId, waybills.size());
                String advice = cozeUtil.dispatchChat("dispatch-system", prompt);
                if (advice != null && !advice.isBlank()) {
                    log.info("[调度中心-AI] Coze 调度建议（路线 {}→{}）：\n{}", sendOrganId, receiveOrganId, advice);
                } else {
                    log.warn("[调度中心-AI] Coze 未返回有效建议，路线 {}→{}", sendOrganId, receiveOrganId);
                }
            } catch (Exception e) {
                log.warn("[调度中心-AI] 请求 Coze 调度建议失败，路线 {}→{}：{}", sendOrganId, receiveOrganId, e.getMessage());
            }
        });
    }
}
