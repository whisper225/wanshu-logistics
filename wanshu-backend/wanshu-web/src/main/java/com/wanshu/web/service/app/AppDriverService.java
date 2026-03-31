package com.wanshu.web.service.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wanshu.base.mapper.BaseVehicleMapper;
import com.wanshu.business.mapper.BizOrderMapper;
import com.wanshu.business.mapper.BizWaybillMapper;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.dispatch.mapper.DispatchReturnRegisterMapper;
import com.wanshu.dispatch.mapper.DispatchTransportTaskMapper;
import com.wanshu.dispatch.mapper.DispatchTransportTaskWaybillMapper;
import com.wanshu.model.entity.base.BaseVehicle;
import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.biz.BizWaybill;
import com.wanshu.model.entity.dispatch.DispatchReturnRegister;
import com.wanshu.model.entity.dispatch.DispatchTransportTask;
import com.wanshu.model.entity.dispatch.DispatchTransportTaskWaybill;
import com.wanshu.model.entity.track.TrackRoute;
import com.wanshu.web.repository.TrackRouteRepository;
import com.wanshu.web.service.mq.WaybillDispatchListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 司机端业务：运输任务管理、出库确认、入库确认、回车登记
 *
 * <pre>
 * 司机出库流程：
 *   司机确认出发 → 更新运输任务状态为"运输中" → 更新运单/订单为"运输中" → 更新车辆状态
 *
 * 司机入库流程：
 *   司机确认到达 → 更新运输任务为"已完成" → 计算运单流转节点：
 *     - 若已到终点机构：创建派件任务（调度中心负责）
 *     - 若需中转：重新发起调度（待下一段运输）
 *   → 更新车辆所在机构 → 创建回车登记
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppDriverService {

    private final DispatchTransportTaskMapper transportTaskMapper;
    private final DispatchTransportTaskWaybillMapper taskWaybillMapper;
    private final DispatchReturnRegisterMapper returnRegisterMapper;
    private final BizWaybillMapper waybillMapper;
    private final BizOrderMapper orderMapper;
    private final BaseVehicleMapper vehicleMapper;
    private final WaybillDispatchListener dispatchListener;
    private final TrackNodeService trackNodeService;
    private final TrackRouteRepository trackRouteRepository;

    // =====================================================================
    // 查询运输任务
    // =====================================================================

    /**
     * 查询司机的运输任务列表（按状态筛选）
     */
    public List<Map<String, Object>> listTransportTasks(Long driverId, Integer status) {
        LambdaQueryWrapper<DispatchTransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchTransportTask::getDriverId, driverId);
        if (status != null) {
            wrapper.eq(DispatchTransportTask::getStatus, status);
        }
        wrapper.orderByDesc(DispatchTransportTask::getCreatedTime);
        List<DispatchTransportTask> tasks = transportTaskMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (DispatchTransportTask task : tasks) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", task.getId());
            m.put("taskNumber", task.getTaskNumber());
            m.put("vehicleId", task.getVehicleId());
            m.put("lineId", task.getLineId());
            m.put("tripId", task.getTripId());
            m.put("startOrganId", task.getStartOrganId());
            m.put("endOrganId", task.getEndOrganId());
            m.put("planDepartTime", task.getPlanDepartTime());
            m.put("actualDepartTime", task.getActualDepartTime());
            m.put("planArriveTime", task.getPlanArriveTime());
            m.put("actualArriveTime", task.getActualArriveTime());
            m.put("waybillCount", task.getWaybillCount());
            m.put("loadWeight", task.getLoadWeight());
            m.put("status", task.getStatus());
            m.put("statusText", transportStatusText(task.getStatus()));
            // 运单列表
            m.put("waybills", listWaybillsForTask(task.getId()));
            result.add(m);
        }
        return result;
    }

    /**
     * 获取运输任务详情
     */
    public Map<String, Object> getTransportTaskDetail(Long taskId, Long driverId) {
        DispatchTransportTask task = validateTask(taskId, driverId);
        Map<String, Object> m = new HashMap<>();
        m.put("id", task.getId());
        m.put("taskNumber", task.getTaskNumber());
        m.put("vehicleId", task.getVehicleId());
        m.put("startOrganId", task.getStartOrganId());
        m.put("endOrganId", task.getEndOrganId());
        m.put("planDepartTime", task.getPlanDepartTime());
        m.put("actualDepartTime", task.getActualDepartTime());
        m.put("planArriveTime", task.getPlanArriveTime());
        m.put("actualArriveTime", task.getActualArriveTime());
        m.put("waybillCount", task.getWaybillCount());
        m.put("loadWeight", task.getLoadWeight());
        m.put("loadVolume", task.getLoadVolume());
        m.put("status", task.getStatus());
        m.put("statusText", transportStatusText(task.getStatus()));
        m.put("waybills", listWaybillsForTask(taskId));
        return m;
    }

    // =====================================================================
    // 司机出库（开始运输任务）
    // =====================================================================

    /**
     * 司机出库确认：
     * 1. 更新运输任务状态为"运输中"（1），记录实际出发时间
     * 2. 更新该任务下所有运单状态为"运输中"（2）
     * 3. 更新关联订单状态为"运输中"（3）
     * 4. 更新车辆状态为"使用中"（2）
     */
    @Transactional
    public void confirmDeparture(Long taskId, Long driverId) {
        DispatchTransportTask task = validateTask(taskId, driverId);
        if (task.getStatus() != 0) {
            throw new BusinessException("运输任务状态不允许出库（需为待出发状态）");
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新运输任务 → 运输中（1）
        DispatchTransportTask taskUpd = new DispatchTransportTask();
        taskUpd.setId(taskId);
        taskUpd.setActualDepartTime(now);
        taskUpd.setStatus(1);
        transportTaskMapper.updateById(taskUpd);

        // 更新运单 & 订单状态，写入出库跟踪节点
        List<Long> waybillIds = getWaybillIds(taskId);
        for (Long waybillId : waybillIds) {
            BizWaybill waybillUpd = new BizWaybill();
            waybillUpd.setId(waybillId);
            waybillUpd.setStatus(2); // 运输中
            waybillMapper.updateById(waybillUpd);

            BizWaybill waybill = waybillMapper.selectById(waybillId);
            if (waybill != null && waybill.getOrderId() != null) {
                BizOrder orderUpd = new BizOrder();
                orderUpd.setId(waybill.getOrderId());
                orderUpd.setStatus(3); // 运输中
                orderMapper.updateById(orderUpd);
            }

            // 写入物流跟踪节点：已从出发机构发出
            trackNodeService.recordDeparture(waybillId, task.getStartOrganId());
        }

        // 更新车辆状态 → 使用中（2）
        if (task.getVehicleId() != null) {
            vehicleMapper.update(null, new LambdaUpdateWrapper<BaseVehicle>()
                    .eq(BaseVehicle::getId, task.getVehicleId())
                    .set(BaseVehicle::getStatus, 2));
        }

        log.info("[司机出库] 运输任务已开始，taskId={}，driverId={}，运单数={}",
                taskId, driverId, waybillIds.size());
    }

    // =====================================================================
    // 司机入库（完成运输任务）
    // =====================================================================

    /**
     * 司机入库确认：
     * 1. 更新运输任务状态为"已完成"（2），记录实际到达时间
     * 2. 计算运单流转节点：
     *    - 若运单终点 = 当前到达机构 → 触发创建派件任务
     *    - 若需继续中转 → 将运单状态重置为已揽收（1），等待下一段调度
     * 3. 更新车辆所在机构为到达机构
     * 4. 标记运输任务结束
     */
    @Transactional
    public void confirmArrival(Long taskId, Long driverId) {
        DispatchTransportTask task = validateTask(taskId, driverId);
        if (task.getStatus() != 1) {
            throw new BusinessException("运输任务状态不允许入库（需为运输中状态）");
        }

        LocalDateTime now = LocalDateTime.now();
        Long arrivedOrganId = task.getEndOrganId();

        // 更新运输任务 → 已完成（2）
        DispatchTransportTask taskUpd = new DispatchTransportTask();
        taskUpd.setId(taskId);
        taskUpd.setActualArriveTime(now);
        taskUpd.setStatus(2);
        transportTaskMapper.updateById(taskUpd);

        // 计算运单流转节点
        List<Long> waybillIds = getWaybillIds(taskId);
        for (Long waybillId : waybillIds) {
            BizWaybill waybill = waybillMapper.selectById(waybillId);
            if (waybill == null) {
                continue;
            }
            // 写入物流跟踪节点：已到达当前机构
            trackNodeService.recordArrival(waybillId, arrivedOrganId);

            if (arrivedOrganId != null && arrivedOrganId.equals(waybill.getReceiveOrganId())) {
                // 已到达目的机构 → 创建派件任务
                dispatchListener.createDeliveryTask(waybill);
            } else {
                // 需要继续中转 → 重置为已揽收状态，等待下一段运输调度
                BizWaybill waybillUpd = new BizWaybill();
                waybillUpd.setId(waybillId);
                waybillUpd.setStatus(1); // 已揽收，待下一段调度
                waybillUpd.setSendOrganId(arrivedOrganId); // 更新起点为当前到达机构
                waybillMapper.updateById(waybillUpd);
                log.info("[司机入库] 运单需继续中转，waybillId={}，当前机构={}，终点机构={}",
                        waybillId, arrivedOrganId, waybill.getReceiveOrganId());
            }
        }

        // 更新车辆状态 → 可用（1），并更新所在机构
        if (task.getVehicleId() != null) {
            vehicleMapper.update(null, new LambdaUpdateWrapper<BaseVehicle>()
                    .eq(BaseVehicle::getId, task.getVehicleId())
                    .set(BaseVehicle::getStatus, 1)
                    .set(BaseVehicle::getOrganId, arrivedOrganId));
        }

        log.info("[司机入库] 运输任务已完成，taskId={}，到达机构={}，运单数={}",
                taskId, arrivedOrganId, waybillIds.size());
    }

    // =====================================================================
    // 回车登记
    // =====================================================================

    /**
     * 司机完成回车登记（入库后填写说明与照片）
     */
    @Transactional
    public void createReturnRegister(Long taskId, Long driverId,
                                     String description, String images) {
        DispatchTransportTask task = validateTask(taskId, driverId);
        if (task.getStatus() != 2) {
            throw new BusinessException("运输任务尚未完成，不能进行回车登记");
        }

        DispatchReturnRegister register = new DispatchReturnRegister();
        register.setTransportTaskId(taskId);
        register.setVehicleId(task.getVehicleId());
        register.setDriverId(driverId);
        register.setRegisterTime(LocalDateTime.now());
        register.setRegisterDate(LocalDate.now());
        register.setDescription(description);
        register.setImages(images);
        register.setCreatedTime(LocalDateTime.now());
        returnRegisterMapper.insert(register);

        log.info("[回车登记] 登记成功，taskId={}，driverId={}", taskId, driverId);
    }

    // =====================================================================
    // 辅助方法
    // =====================================================================

    private DispatchTransportTask validateTask(Long taskId, Long driverId) {
        DispatchTransportTask task = transportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("运输任务不存在");
        }
        if (!driverId.equals(task.getDriverId())) {
            throw new BusinessException("无权操作该运输任务");
        }
        return task;
    }

    private List<Long> getWaybillIds(Long taskId) {
        return taskWaybillMapper.selectList(
                        new LambdaQueryWrapper<DispatchTransportTaskWaybill>()
                                .eq(DispatchTransportTaskWaybill::getTransportTaskId, taskId))
                .stream().map(DispatchTransportTaskWaybill::getWaybillId).toList();
    }

    private List<Map<String, Object>> listWaybillsForTask(Long taskId) {
        List<Long> ids = getWaybillIds(taskId);
        if (ids.isEmpty()) {
            return List.of();
        }
        return waybillMapper.selectList(
                        new LambdaQueryWrapper<BizWaybill>().in(BizWaybill::getId, ids))
                .stream().map(w -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", w.getId());
                    m.put("waybillNumber", w.getWaybillNumber());
                    m.put("senderName", w.getSenderName());
                    m.put("senderPhone", w.getSenderPhone());
                    m.put("receiverName", w.getReceiverName());
                    m.put("receiverPhone", w.getReceiverPhone());
                    m.put("receiverAddress", w.getReceiverAddress());
                    m.put("goodsName", w.getGoodsName());
                    m.put("weight", w.getWeight());
                    m.put("status", w.getStatus());
                    return m;
                }).toList();
    }

    private String transportStatusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待出发";
            case 1 -> "运输中";
            case 2 -> "已完成";
            default -> "未知";
        };
    }

    // =====================================================================
    // 位置上报
    // =====================================================================

    /**
     * 司机上报当前位置，更新关联运单的轨迹实时坐标。
     *
     * @param waybillId 运单 ID
     * @param longitude 经度
     * @param latitude  纬度
     */
    public void updateLocation(Long waybillId, double longitude, double latitude) {
        trackRouteRepository.findByWaybillId(waybillId).ifPresentOrElse(route -> {
            if (Integer.valueOf(1).equals(route.getStatus())) {
                log.info("[位置上报-司机] 轨迹已完成，忽略上报，waybillId={}", waybillId);
                return;
            }
            route.setCurrentLocation(new TrackRoute.GeoPoint(longitude, latitude));
            route.setLastLocationTime(LocalDateTime.now());
            trackRouteRepository.save(route);
            log.info("[位置上报-司机] waybillId={} lng={} lat={}", waybillId, longitude, latitude);
        }, () -> log.warn("[位置上报-司机] 未找到轨迹记录，waybillId={}", waybillId));
    }
}
