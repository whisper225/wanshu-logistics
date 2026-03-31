package com.wanshu.web.service.mq;

import com.wanshu.base.mapper.BaseOrganMapper;
import com.wanshu.business.mapper.BizWaybillMapper;
import com.wanshu.dispatch.repository.RouteRepository;
import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.biz.BizWaybill;
import com.wanshu.model.entity.track.TrackRoute;
import com.wanshu.web.config.RabbitMqConfig;
import com.wanshu.web.repository.TrackRouteRepository;
import com.wanshu.web.service.app.CozeRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 轨迹 MQ 监听器
 *
 * <pre>
 * track.create  — 揽收后触发：查运单起终点 + 查 Neo4j 途经转运中心作为 waypoints
 *                             → 调用 Coze 工作流规划路线 → 存 MongoDB track_route
 * track.complete — 签收后触发：标记 TrackRoute.status = 1（已完成）
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackMqListener {

    private static final int MAX_POLYLINE_POINTS = 500;

    private final BizWaybillMapper waybillMapper;
    private final BaseOrganMapper organMapper;
    private final TrackRouteRepository trackRouteRepository;
    private final CozeRouteService cozeRouteService;
    private final RouteRepository routeRepository;

    // =====================================================================
    // 轨迹创建
    // =====================================================================

    /**
     * 收到轨迹创建消息后：
     * 1. 幂等检查（已存在直接跳过）
     * 2. 读取运单寄/收件机构坐标作为起终点
     * 3. 通过 Neo4j 最短路径查询途经转运中心，提取中间节点坐标作为 waypoints
     * 4. 调用 Coze 工作流（高德驾车 API）规划路线
     * 5. 抽稀坐标点后写入 MongoDB
     */
    @RabbitListener(queues = RabbitMqConfig.QUEUE_TRACK_CREATE)
    public void onTrackCreate(Long waybillId) {
        log.info("[轨迹MQ] 收到轨迹创建消息，waybillId={}", waybillId);

        if (trackRouteRepository.findByWaybillId(waybillId).isPresent()) {
            log.info("[轨迹MQ] 轨迹已存在（按waybillId），跳过，waybillId={}", waybillId);
            return;
        }

        BizWaybill waybill = waybillMapper.selectById(waybillId);
        if (waybill == null) {
            log.warn("[轨迹MQ] 运单不存在，waybillId={}", waybillId);
            return;
        }

        // AppTrackService 可能已按 orderId 创建了文档（用户提前查询轨迹），
        // 此时需更新补充 waybillId/status/waypoints 等字段，避免 orderId 唯一索引冲突
        TrackRoute existing = waybill.getOrderId() != null
                ? trackRouteRepository.findByOrderId(waybill.getOrderId()).orElse(null)
                : null;

        BaseOrgan sendOrgan    = waybill.getSendOrganId()    != null ? organMapper.selectById(waybill.getSendOrganId())    : null;
        BaseOrgan receiveOrgan = waybill.getReceiveOrganId() != null ? organMapper.selectById(waybill.getReceiveOrganId()) : null;

        String origin      = buildCoord(sendOrgan);
        String destination = buildCoord(receiveOrgan);

        List<String> waypointCoords = resolveWaypoints(waybill);

        log.info("[轨迹MQ] 调用路线规划，waybillId={}, origin={}, dest={}, waypoints={}",
                waybillId, origin, destination, waypointCoords);
        CozeRouteService.RouteResult result = cozeRouteService.planRoute(origin, destination, waypointCoords);

        List<TrackRoute.GeoPoint> points = downsample(result.points(), MAX_POLYLINE_POINTS);

        TrackRoute route = existing != null ? existing : new TrackRoute();
        route.setWaybillId(waybillId);
        route.setOrderId(waybill.getOrderId());
        route.setStatus(0);
        route.setOriginAddress(waybill.getSenderAddress());
        route.setDestAddress(waybill.getReceiverAddress());
        route.setWaypoints(waypointCoords);
        route.setDistanceMeters(result.distanceMeters());
        route.setPolylinePoints(points);
        if (existing == null) {
            route.setCreatedAt(LocalDateTime.now());
        }
        route.setUpdatedAt(LocalDateTime.now());

        trackRouteRepository.save(route);
        log.info("[轨迹MQ] 轨迹已{}，waybillId={}, 坐标点={}，途经中转站={}",
                existing != null ? "更新" : "保存", waybillId, points.size(), waypointCoords.size());
    }

    // =====================================================================
    // 轨迹完成
    // =====================================================================

    /**
     * 收到轨迹完成消息后，将 TrackRoute.status 标记为 1（已完成），之后不再更新实时位置。
     */
    @RabbitListener(queues = RabbitMqConfig.QUEUE_TRACK_COMPLETE)
    public void onTrackComplete(Long waybillId) {
        log.info("[轨迹MQ] 收到轨迹完成消息，waybillId={}", waybillId);
        trackRouteRepository.findByWaybillId(waybillId).ifPresentOrElse(
                route -> {
                    route.setStatus(1);
                    route.setUpdatedAt(LocalDateTime.now());
                    trackRouteRepository.save(route);
                    log.info("[轨迹MQ] 轨迹已标记完成，waybillId={}", waybillId);
                },
                () -> log.warn("[轨迹MQ] 未找到轨迹，waybillId={}", waybillId)
        );
    }

    // =====================================================================
    // 私有辅助
    // =====================================================================

    /**
     * 通过 Neo4j 最短路径查询途经转运中心，提取中间节点（排除起终点）坐标。
     * 若 Neo4j 不可用或无法找到路径，静默返回空列表。
     */
    private List<String> resolveWaypoints(BizWaybill waybill) {
        Long sendId    = waybill.getSendOrganId();
        Long receiveId = waybill.getReceiveOrganId();

        if (sendId == null || receiveId == null || sendId.equals(receiveId)) {
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> paths = routeRepository.findShortestPath(sendId, receiveId);
            if (paths.isEmpty()) {
                return Collections.emptyList();
            }

            Object nodesObj = paths.get(0).get("nodes");
            if (!(nodesObj instanceof List<?> nodeList) || nodeList.size() <= 2) {
                return Collections.emptyList();
            }

            List<String> coords = new ArrayList<>();
            // 中间节点：排除第 0（起点）和最后一个（终点）
            for (int i = 1; i < nodeList.size() - 1; i++) {
                if (!(nodeList.get(i) instanceof Map<?, ?> nodeMap)) continue;
                Object bid = nodeMap.get("bid");
                Long bidLong = bid instanceof Long l ? l
                        : bid instanceof Integer iv ? iv.longValue()
                        : null;
                if (bidLong == null) continue;

                BaseOrgan organ = organMapper.selectById(bidLong);
                if (organ != null && organ.getLongitude() != null && organ.getLatitude() != null) {
                    coords.add(organ.getLongitude().toPlainString() + "," + organ.getLatitude().toPlainString());
                }
            }
            return coords;

        } catch (Exception e) {
            log.warn("[轨迹MQ] Neo4j 路径查询失败，使用空 waypoints，waybillId={}: {}", waybill.getId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /** 将机构坐标格式化为 "lng,lat"；机构不存在或无坐标时返回成都默认坐标。 */
    private String buildCoord(BaseOrgan organ) {
        if (organ != null && organ.getLongitude() != null && organ.getLatitude() != null) {
            return organ.getLongitude().toPlainString() + "," + organ.getLatitude().toPlainString();
        }
        return "104.0657,30.6595";
    }

    /** 坐标等比抽稀：保留首尾点，均匀保留至多 maxPoints 个点。 */
    private List<TrackRoute.GeoPoint> downsample(List<TrackRoute.GeoPoint> points, int maxPoints) {
        if (points == null || points.size() <= maxPoints) {
            return points == null ? Collections.emptyList() : points;
        }
        List<TrackRoute.GeoPoint> result = new ArrayList<>(maxPoints);
        double step = (double) (points.size() - 1) / (maxPoints - 1);
        for (int i = 0; i < maxPoints; i++) {
            result.add(points.get((int) Math.round(i * step)));
        }
        return result;
    }
}
