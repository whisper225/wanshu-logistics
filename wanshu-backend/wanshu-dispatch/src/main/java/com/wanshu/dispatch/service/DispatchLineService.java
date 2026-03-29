package com.wanshu.dispatch.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.base.mapper.BaseVehicleMapper;
import com.wanshu.common.event.LineGraphDeleteEvent;
import com.wanshu.common.event.LineGraphUpsertEvent;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.dispatch.mapper.DispatchConfigMapper;
import com.wanshu.dispatch.mapper.DispatchLineMapper;
import com.wanshu.dispatch.mapper.DispatchTransportTaskMapper;
import com.wanshu.dispatch.mapper.DispatchTripMapper;
import com.wanshu.dispatch.mapper.DispatchTripVehicleMapper;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.base.BaseVehicle;
import com.wanshu.model.entity.dispatch.DispatchConfig;
import com.wanshu.model.entity.dispatch.DispatchLine;
import com.wanshu.model.entity.dispatch.DispatchTransportTask;
import com.wanshu.model.entity.dispatch.DispatchTrip;
import com.wanshu.model.entity.dispatch.DispatchTripVehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispatchLineService {

    private static final int MAX_VEHICLES_PER_TRIP = 200;
    /** 运输任务：已分配、进行中视为占用车辆 */
    private static final List<Integer> TRANSPORT_BUSY_STATUSES = Arrays.asList(1, 2);

    private final DispatchLineMapper lineMapper;
    private final DispatchTripMapper tripMapper;
    private final DispatchTripVehicleMapper tripVehicleMapper;
    private final BaseOrganMapper organMapper;
    private final BaseVehicleMapper vehicleMapper;
    private final DispatchTransportTaskMapper transportTaskMapper;
    private final DispatchConfigMapper configMapper;
    private final AmapRestService amapRestService;
    private final ApplicationEventPublisher eventPublisher;

    public IPage<DispatchLine> page(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<DispatchLine> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DispatchLine::getLineName, keyword)
                    .or().like(DispatchLine::getLineNumber, keyword));
        }
        if (status != null) {
            wrapper.eq(DispatchLine::getStatus, status);
        }
        wrapper.orderByDesc(DispatchLine::getCreatedTime);
        IPage<DispatchLine> page = lineMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        fillOrganNames(page.getRecords());
        return page;
    }

    public DispatchLine getById(Long id) {
        DispatchLine line = lineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        fillOrganNames(Collections.singletonList(line));
        return line;
    }

    private void fillOrganNames(List<DispatchLine> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> ids = new HashSet<>();
        for (DispatchLine l : records) {
            if (l.getStartOrganId() != null) {
                ids.add(l.getStartOrganId());
            }
            if (l.getEndOrganId() != null) {
                ids.add(l.getEndOrganId());
            }
        }
        if (ids.isEmpty()) {
            return;
        }
        List<BaseOrgan> organs = organMapper.selectBatchIds(ids);
        Map<Long, String> names = organs.stream()
                .collect(Collectors.toMap(BaseOrgan::getId, BaseOrgan::getOrganName, (a, b) -> a));
        for (DispatchLine l : records) {
            if (l.getStartOrganId() != null) {
                l.setStartOrganName(names.get(l.getStartOrganId()));
            }
            if (l.getEndOrganId() != null) {
                l.setEndOrganName(names.get(l.getEndOrganId()));
            }
        }
    }

    @Transactional
    public void create(DispatchLine line) {
        validateLineNumber(line.getLineNumber());

        // 查询起点/终点机构
        BaseOrgan startOrgan = organMapper.selectById(line.getStartOrganId());
        BaseOrgan endOrgan = organMapper.selectById(line.getEndOrganId());
        if (startOrgan == null) {
            throw new BusinessException("起始机构不存在");
        }
        if (endOrgan == null) {
            throw new BusinessException("目的机构不存在");
        }

        // 校验起始机构类型与线路类型是否匹配
        validateOrganTypeForLine(line.getLineType(), startOrgan);

        // 通过高德 REST API 计算驾车距离（米）
        double distanceMeters = amapRestService.getDrivingDistanceMeters(startOrgan, endOrgan);
        line.setDistance(BigDecimal.valueOf(distanceMeters).setScale(2, RoundingMode.HALF_UP));

        // 从全局调度配置取对应线路类型的每公里成本，计算总成本
        BigDecimal costPerKm = getGlobalCostPerKm(line.getLineType());
        if (costPerKm != null && costPerKm.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal distanceKm = line.getDistance().divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
            line.setCost(distanceKm.multiply(costPerKm).setScale(2, RoundingMode.HALF_UP));
        }

        lineMapper.insert(line);
        eventPublisher.publishEvent(new LineGraphUpsertEvent(line, startOrgan, endOrgan));

        // 自动创建返程线路（同等距离/成本，起点与终点互换）
        DispatchLine returnLine = new DispatchLine();
        returnLine.setLineNumber(line.getLineNumber() + "R");
        returnLine.setLineName(line.getLineName() + "（返程）");
        returnLine.setLineType(line.getLineType());
        returnLine.setStartOrganId(line.getEndOrganId());
        returnLine.setEndOrganId(line.getStartOrganId());
        returnLine.setDistance(line.getDistance());
        returnLine.setCost(line.getCost());
        returnLine.setEstimatedTime(line.getEstimatedTime());
        returnLine.setStatus(line.getStatus());
        lineMapper.insert(returnLine);
        // 返程线路 Neo4j 关系（方向互换）
        eventPublisher.publishEvent(new LineGraphUpsertEvent(returnLine, endOrgan, startOrgan));
    }

    @Transactional
    public void update(DispatchLine line) {
        validateLineNumber(line.getLineNumber());

        DispatchLine existing = lineMapper.selectById(line.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        // 如果更改了起点/终点，需要重新校验机构类型（但不自动重算距离，由前端传入）
        if (line.getStartOrganId() != null &&
                !Objects.equals(line.getStartOrganId(), existing.getStartOrganId())) {
            BaseOrgan startOrgan = organMapper.selectById(line.getStartOrganId());
            if (startOrgan == null) {
                throw new BusinessException("起始机构不存在");
            }
            Integer lineType = line.getLineType() != null ? line.getLineType() : existing.getLineType();
            validateOrganTypeForLine(lineType, startOrgan);
        }

        lineMapper.updateById(line);

        // 更新后也同步 Neo4j（用编辑后的机构信息）
        DispatchLine updated = lineMapper.selectById(line.getId());
        BaseOrgan startOrgan = organMapper.selectById(updated.getStartOrganId());
        BaseOrgan endOrgan = organMapper.selectById(updated.getEndOrganId());
        if (startOrgan != null && endOrgan != null) {
            eventPublisher.publishEvent(new LineGraphUpsertEvent(updated, startOrgan, endOrgan));
        }
    }

    private void validateLineNumber(String lineNumber) {
        if (!StringUtils.hasText(lineNumber) || !lineNumber.startsWith("XL")) {
            throw new BusinessException("线路编号须以 XL 开头");
        }
    }

    /**
     * 校验起始机构类型是否与线路类型一致：
     * 干线(1)：起点须为一级转运中心(organType=1)
     * 支线(2)：起点须为二级转运中心(organType=2)
     * 接驳路线(3)：起点须为网点/营业部(organType=3)
     */
    private void validateOrganTypeForLine(Integer lineType, BaseOrgan startOrgan) {
        if (lineType == null) {
            return;
        }
        Integer required = lineType; // 1→1, 2→2, 3→3（线路类型与机构类型恰好对应）
        if (!Objects.equals(startOrgan.getOrganType(), required)) {
            String lineTypeName = switch (lineType) {
                case 1 -> "干线";
                case 2 -> "支线";
                case 3 -> "接驳路线";
                default -> "未知类型";
            };
            String organTypeName = switch (required) {
                case 1 -> "一级转运中心";
                case 2 -> "二级转运中心";
                case 3 -> "营业部/网点";
                default -> "未知机构类型";
            };
            throw new BusinessException(lineTypeName + "的起点必须是" + organTypeName +
                    "，当前机构「" + startOrgan.getOrganName() + "」类型不符");
        }
    }

    /**
     * 从全局调度配置（organ_id IS NULL）获取指定线路类型的每公里成本。
     */
    private BigDecimal getGlobalCostPerKm(Integer lineType) {
        if (lineType == null) {
            return null;
        }
        DispatchConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<DispatchConfig>()
                        .isNull(DispatchConfig::getOrganId)
                        .last("LIMIT 1"));
        if (config == null) {
            return null;
        }
        return switch (lineType) {
            case 1 -> config.getCostPerKmType1();
            case 2 -> config.getCostPerKmType2();
            case 3 -> config.getCostPerKmType3();
            default -> null;
        };
    }

    @Transactional
    public void delete(Long id) {
        DispatchLine line = lineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        LambdaQueryWrapper<DispatchTrip> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DispatchTrip::getLineId, id);
        if (tripMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该线路下存在车次，不能删除");
        }
        lineMapper.deleteById(id);

        // MySQL 事务提交后同步删除 Neo4j 关系
        eventPublisher.publishEvent(
                new LineGraphDeleteEvent(line.getLineNumber(), line.getStartOrganId(), line.getEndOrganId()));
    }

    // ========== 车次 ==========

    public List<DispatchTrip> getTrips(Long lineId) {
        return tripMapper.selectList(
                new LambdaQueryWrapper<DispatchTrip>()
                        .eq(DispatchTrip::getLineId, lineId)
                        .orderByAsc(DispatchTrip::getDepartTime));
    }

    @Transactional
    public void createTrip(DispatchTrip trip) {
        if (!org.springframework.util.StringUtils.hasText(trip.getTripNumber())) {
            long id = com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
            trip.setId(id);
            trip.setTripNumber("TC" + (id % 1_000_000_000L));
        }
        tripMapper.insert(trip);
    }

    @Transactional
    public void updateTrip(DispatchTrip trip) {
        tripMapper.updateById(trip);
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        long bound = tripVehicleMapper.selectCount(
                new LambdaQueryWrapper<DispatchTripVehicle>().eq(DispatchTripVehicle::getTripId, tripId));
        if (bound > 0) {
            throw new BusinessException("该车次下已安排车辆，不能删除");
        }
        tripMapper.deleteById(tripId);
    }

    // ========== 车次车辆 ==========

    public List<Map<String, Object>> listTripVehicles(Long tripId) {
        ensureTripExists(tripId);
        List<DispatchTripVehicle> links = tripVehicleMapper.selectList(
                new LambdaQueryWrapper<DispatchTripVehicle>().eq(DispatchTripVehicle::getTripId, tripId));
        List<Map<String, Object>> out = new ArrayList<>();
        for (DispatchTripVehicle link : links) {
            BaseVehicle v = vehicleMapper.selectById(link.getVehicleId());
            if (v == null) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", String.valueOf(link.getId()));
            m.put("tripId", String.valueOf(tripId));
            m.put("vehicleId", String.valueOf(v.getId()));
            m.put("licensePlate", v.getLicensePlate());
            m.put("vehicleNumber", v.getVehicleNumber());
            m.put("organId", v.getOrganId());
            m.put("vehicleTypeId", v.getVehicleTypeId());
            m.put("status", v.getStatus());
            out.add(m);
        }
        return out;
    }

    public List<BaseVehicle> listEligibleVehiclesForTrip(Long tripId, String keyword) {
        DispatchTrip trip = ensureTripExists(tripId);
        DispatchLine line = lineMapper.selectById(trip.getLineId());
        if (line == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Set<Long> busyVehicleIds = transportTaskMapper.selectList(
                        new LambdaQueryWrapper<DispatchTransportTask>()
                                .in(DispatchTransportTask::getStatus, TRANSPORT_BUSY_STATUSES)
                                .isNotNull(DispatchTransportTask::getVehicleId))
                .stream()
                .map(DispatchTransportTask::getVehicleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> onThisTrip = tripVehicleMapper.selectList(
                        new LambdaQueryWrapper<DispatchTripVehicle>().eq(DispatchTripVehicle::getTripId, tripId))
                .stream()
                .map(DispatchTripVehicle::getVehicleId)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<BaseVehicle> w = new LambdaQueryWrapper<>();
        w.eq(BaseVehicle::getStatus, 1);
        w.and(q -> q.eq(BaseVehicle::getOrganId, line.getStartOrganId())
                .or()
                .eq(BaseVehicle::getOrganId, line.getEndOrganId()));
        if (StringUtils.hasText(keyword)) {
            w.like(BaseVehicle::getLicensePlate, keyword.trim());
        }
        List<BaseVehicle> candidates = vehicleMapper.selectList(w);
        return candidates.stream()
                .filter(v -> !busyVehicleIds.contains(v.getId()))
                .filter(v -> !onThisTrip.contains(v.getId()))
                .limit(200)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addVehicleToTrip(Long tripId, Long vehicleId) {
        ensureTripExists(tripId);
        long cnt = tripVehicleMapper.selectCount(
                new LambdaQueryWrapper<DispatchTripVehicle>().eq(DispatchTripVehicle::getTripId, tripId));
        if (cnt >= MAX_VEHICLES_PER_TRIP) {
            throw new BusinessException("单车次最多安排 " + MAX_VEHICLES_PER_TRIP + " 辆车");
        }
        if (tripVehicleMapper.selectCount(
                new LambdaQueryWrapper<DispatchTripVehicle>()
                        .eq(DispatchTripVehicle::getTripId, tripId)
                        .eq(DispatchTripVehicle::getVehicleId, vehicleId)) > 0) {
            throw new BusinessException("该车辆已在该车次中");
        }
        BaseVehicle v = vehicleMapper.selectById(vehicleId);
        if (v == null) {
            throw new BusinessException("车辆不存在");
        }
        if (!Objects.equals(v.getStatus(), 1)) {
            throw new BusinessException("仅可选择状态为「可用」的车辆");
        }
        DispatchTrip trip = tripMapper.selectById(tripId);
        DispatchLine line = lineMapper.selectById(trip.getLineId());
        boolean organOk = Objects.equals(v.getOrganId(), line.getStartOrganId())
                || Objects.equals(v.getOrganId(), line.getEndOrganId());
        if (!organOk) {
            throw new BusinessException("车辆所属机构须为线路起始地或目的地（支持往返车次）");
        }
        long busy = transportTaskMapper.selectCount(
                new LambdaQueryWrapper<DispatchTransportTask>()
                        .eq(DispatchTransportTask::getVehicleId, vehicleId)
                        .in(DispatchTransportTask::getStatus, TRANSPORT_BUSY_STATUSES));
        if (busy > 0) {
            throw new BusinessException("该车辆运输任务进行中，不可安排");
        }
        DispatchTripVehicle link = new DispatchTripVehicle();
        link.setTripId(tripId);
        link.setVehicleId(vehicleId);
        tripVehicleMapper.insert(link);
    }

    @Transactional
    public void removeVehicleFromTrip(Long tripId, Long vehicleId) {
        tripVehicleMapper.delete(
                new LambdaQueryWrapper<DispatchTripVehicle>()
                        .eq(DispatchTripVehicle::getTripId, tripId)
                        .eq(DispatchTripVehicle::getVehicleId, vehicleId));
    }

    private DispatchTrip ensureTripExists(Long tripId) {
        DispatchTrip trip = tripMapper.selectById(tripId);
        if (trip == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        return trip;
    }
}
