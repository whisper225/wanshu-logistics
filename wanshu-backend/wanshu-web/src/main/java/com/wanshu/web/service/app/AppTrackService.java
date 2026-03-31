package com.wanshu.web.service.app;

import com.wanshu.model.entity.biz.BizOrder;
import com.wanshu.model.entity.track.TrackRoute;
import com.wanshu.web.repository.TrackRouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单路线轨迹服务
 * <p>
 * 流程：
 *   1. 查 MongoDB 是否已存在 → 已存在直接返回（幂等）
 *   2. 查订单信息 → 获取收发件地址（作为起终点）
 *   3. 调用 CozeRouteService.planRoute（Coze工作流 → 高德API → polyline）
 *   4. 坐标点抽稀（最多 500 点）
 *   5. 存入 MongoDB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppTrackService {

    /** 抽稀后最大坐标点数，超出则等比抽取 */
    private static final int MAX_POINTS = 500;

    /**
     * 起/终点坐标：无真实地理编码时使用城市默认中心点（成都/北京）作为演示。
     * 生产环境应对接高德地理编码 API 将地址转换为经纬度。
     */
    private static final String DEFAULT_ORIGIN = "104.0657,30.6595";       // 成都
    private static final String DEFAULT_DESTINATION = "116.3975,39.9087";   // 北京

    private final AppOrderService appOrderService;
    private final CozeRouteService cozeRouteService;
    private final TrackRouteRepository trackRouteRepository;

    /**
     * 生成并缓存订单路线轨迹（幂等：已存在则直接返回）。
     *
     * @param orderId 订单 ID
     * @return TrackRoute 文档（含 polylinePoints）
     */
    public TrackRoute create(Long orderId) {
        // 幂等：已存在直接返回
        Optional<TrackRoute> existing = trackRouteRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            log.debug("轨迹已存在，直接返回: orderId={}", orderId);
            return existing.get();
        }

        // 查询订单
        BizOrder order = appOrderService.getOrderDetailInternal(orderId);

        // 构造起终点坐标（优先使用真实地址，当前版本用城市默认坐标）
        String origin = buildCoord(order.getSenderAddress(), DEFAULT_ORIGIN);
        String destination = buildCoord(order.getReceiverAddress(), DEFAULT_DESTINATION);

        // 调用 Coze 工作流规划路线（内部使用高德 API）
        log.info("调用 Coze 路径规划: orderId={}, origin={}, dest={}", orderId, origin, destination);
        CozeRouteService.RouteResult result = cozeRouteService.planRoute(origin, destination, Collections.emptyList());

        // 抽稀坐标点
        List<TrackRoute.GeoPoint> points = downsample(result.points(), MAX_POINTS);

        // 构建并保存文档
        TrackRoute route = new TrackRoute();
        route.setOrderId(orderId);
        route.setOriginAddress(order.getSenderAddress());
        route.setDestAddress(order.getReceiverAddress());
        route.setWaypoints(Collections.emptyList());
        route.setDistanceMeters(result.distanceMeters());
        route.setPolylinePoints(points);
        route.setCreatedAt(LocalDateTime.now());
        route.setUpdatedAt(LocalDateTime.now());

        return trackRouteRepository.save(route);
    }

    /**
     * 查询订单路线轨迹，不存在时自动创建。
     *
     * @param orderId 订单 ID
     */
    public TrackRoute getOrCreate(Long orderId) {
        return trackRouteRepository.findByOrderId(orderId)
                .orElseGet(() -> create(orderId));
    }

    /**
     * 仅查询，不自动创建，不存在返回 null。
     */
    public TrackRoute getByOrderId(Long orderId) {
        return trackRouteRepository.findByOrderId(orderId).orElse(null);
    }

    /**
     * 删除并重新生成路线轨迹（用于地址变更后刷新）。
     */
    public TrackRoute refresh(Long orderId) {
        trackRouteRepository.deleteByOrderId(orderId);
        return create(orderId);
    }

    // ─────────────────────────── 私有工具 ───────────────────────────

    /**
     * 坐标等比抽稀：保留首尾点，均匀保留至多 maxPoints 个点。
     */
    private List<TrackRoute.GeoPoint> downsample(List<TrackRoute.GeoPoint> points, int maxPoints) {
        if (points == null || points.size() <= maxPoints) return points == null ? Collections.emptyList() : points;
        List<TrackRoute.GeoPoint> result = new ArrayList<>(maxPoints);
        double step = (double) (points.size() - 1) / (maxPoints - 1);
        for (int i = 0; i < maxPoints; i++) {
            result.add(points.get((int) Math.round(i * step)));
        }
        return result;
    }

    /**
     * 构建坐标字符串。若 address 本身是 "lng,lat" 格式则直接使用，否则返回 fallback 默认坐标。
     * TODO: 生产环境应接入高德地理编码 /v3/geocode/geo 将中文地址转为 lng,lat
     */
    private String buildCoord(String address, String fallback) {
        if (StringUtils.hasText(address) && address.matches("^\\d+\\.\\d+,\\d+\\.\\d+$")) {
            return address;
        }
        return fallback;
    }
}
