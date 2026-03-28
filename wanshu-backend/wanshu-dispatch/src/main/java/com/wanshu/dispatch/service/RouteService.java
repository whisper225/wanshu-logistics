package com.wanshu.dispatch.service;

import com.wanshu.dispatch.entity.node.AgencyNode;
import com.wanshu.dispatch.repository.AgencyRepository;
import com.wanshu.dispatch.repository.RouteRepository;
import com.wanshu.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路径计算服务
 * 提供最短路径、最低成本路径、附近营业部查询等核心调度能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final AgencyRepository agencyRepository;

    /**
     * 最少转运次数路径
     *
     * @param startBid 起始营业部业务 ID
     * @param endBid   目标营业部业务 ID
     * @return 路径详情
     */
    public Map<String, Object> findShortestRoute(Long startBid, Long endBid) {
        List<Map<String, Object>> results = routeRepository.findShortestPath(startBid, endBid);
        if (results.isEmpty()) {
            throw new BusinessException("未找到从 " + startBid + " 到 " + endBid + " 的运输路径");
        }
        return results.get(0);
    }

    /**
     * 最低成本路径（需要 Neo4j APOC 插件）
     *
     * @param startBid 起始营业部业务 ID
     * @param endBid   目标营业部业务 ID
     * @return 路径详情（含总成本）
     */
    public Map<String, Object> findCheapestRoute(Long startBid, Long endBid) {
        List<Map<String, Object>> results = routeRepository.findCheapestPath(startBid, endBid);
        if (results.isEmpty()) {
            throw new BusinessException("未找到从 " + startBid + " 到 " + endBid + " 的运输路径");
        }
        return results.get(0);
    }

    /**
     * 查询所有可达路径（按跳数排序，最多返回 5 条）
     *
     * @param startBid 起始机构业务 ID
     * @param endBid   目标机构业务 ID
     * @param maxHops  最大跳数（默认 10）
     * @return 路径列表
     */
    public List<Map<String, Object>> findAllRoutes(Long startBid, Long endBid, int maxHops) {
        return routeRepository.findAllPaths(startBid, endBid, maxHops);
    }

    /**
     * 综合路径推荐：同时返回最短和最低成本两种方案
     *
     * @param startBid 起始营业部业务 ID
     * @param endBid   目标营业部业务 ID
     * @return 包含 shortest 和 cheapest 两种方案
     */
    public Map<String, Object> recommendRoutes(Long startBid, Long endBid) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("shortest", findShortestRoute(startBid, endBid));
        } catch (Exception e) {
            log.warn("最短路径计算失败: {}", e.getMessage());
            result.put("shortest", null);
        }
        try {
            result.put("cheapest", findCheapestRoute(startBid, endBid));
        } catch (Exception e) {
            log.warn("最低成本路径计算失败: {}", e.getMessage());
            result.put("cheapest", null);
        }
        return result;
    }

    /**
     * 查找坐标附近的营业部
     *
     * @param lng          经度
     * @param lat          纬度
     * @param radiusMeters 搜索半径（米）
     * @param limit        最大返回数量
     * @return 附近营业部列表
     */
    public List<AgencyNode> findNearbyAgencies(double lng, double lat, double radiusMeters, int limit) {
        return agencyRepository.findNearby(lng, lat, radiusMeters, limit);
    }
}
