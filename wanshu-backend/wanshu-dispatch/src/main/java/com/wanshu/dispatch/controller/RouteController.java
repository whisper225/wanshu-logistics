package com.wanshu.dispatch.controller;

import com.wanshu.common.result.R;
import com.wanshu.dispatch.entity.node.AgencyNode;
import com.wanshu.dispatch.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 路径计算 Controller
 */
@Tag(name = "路径计算", description = "物流网络路径计算相关接口")
@RestController
@RequestMapping("/api/dispatch/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @Operation(summary = "最少转运次数路径")
    @GetMapping("/shortest")
    public R<Map<String, Object>> shortest(
            @Parameter(description = "起始营业部业务ID") @RequestParam Long startBid,
            @Parameter(description = "目标营业部业务ID") @RequestParam Long endBid) {
        return R.ok(routeService.findShortestRoute(startBid, endBid));
    }

    @Operation(summary = "最低成本路径")
    @GetMapping("/cheapest")
    public R<Map<String, Object>> cheapest(
            @Parameter(description = "起始营业部业务ID") @RequestParam Long startBid,
            @Parameter(description = "目标营业部业务ID") @RequestParam Long endBid) {
        return R.ok(routeService.findCheapestRoute(startBid, endBid));
    }

    @Operation(summary = "综合路径推荐（同时返回最短和最低成本方案）")
    @GetMapping("/recommend")
    public R<Map<String, Object>> recommend(
            @Parameter(description = "起始营业部业务ID") @RequestParam Long startBid,
            @Parameter(description = "目标营业部业务ID") @RequestParam Long endBid) {
        return R.ok(routeService.recommendRoutes(startBid, endBid));
    }

    @Operation(summary = "所有可达路径")
    @GetMapping("/all")
    public R<List<Map<String, Object>>> allRoutes(
            @Parameter(description = "起始机构业务ID") @RequestParam Long startBid,
            @Parameter(description = "目标机构业务ID") @RequestParam Long endBid,
            @Parameter(description = "最大跳数") @RequestParam(defaultValue = "10") int maxHops) {
        return R.ok(routeService.findAllRoutes(startBid, endBid, maxHops));
    }

    @Operation(summary = "查找附近营业部")
    @GetMapping("/nearby")
    public R<List<AgencyNode>> nearby(
            @Parameter(description = "经度") @RequestParam double lng,
            @Parameter(description = "纬度") @RequestParam double lat,
            @Parameter(description = "搜索半径(米)") @RequestParam(defaultValue = "5000") double radius,
            @Parameter(description = "最大返回数量") @RequestParam(defaultValue = "10") int limit) {
        return R.ok(routeService.findNearbyAgencies(lng, lat, radius, limit));
    }
}
