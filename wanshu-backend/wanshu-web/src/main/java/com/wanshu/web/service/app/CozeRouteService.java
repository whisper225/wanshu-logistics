package com.wanshu.web.service.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanshu.common.config.CozeConfig;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.model.entity.track.TrackRoute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Coze 路径规划工作流服务
 * <p>
 * 调用 Coze 工作流（内部使用高德驾车路线规划 API），
 * 工作流输入：origin、destination、waypoints（逗号分隔）
 * 工作流输出 JSON：{ "distance": 12345, "polyline": "lng1,lat1;lng2,lat2;..." }
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CozeRouteService {

    private final CozeConfig cozeConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 通过 Coze 工作流规划驾车路线，返回 polyline 坐标点列表。
     *
     * @param origin      起点坐标 "lng,lat"
     * @param destination 终点坐标 "lng,lat"
     * @param waypoints   途经点坐标列表（"lng,lat" 格式），可为空
     * @return 包含距离和坐标点的结果
     */
    public RouteResult planRoute(String origin, String destination, List<String> waypoints) {
        if (!StringUtils.hasText(cozeConfig.getRouteWorkflowId())) {
            log.warn("Coze route-workflow-id 未配置，返回空路线");
            return RouteResult.empty();
        }
        if (!StringUtils.hasText(cozeConfig.getAccessToken())) {
            log.warn("Coze access-token 未配置，返回空路线");
            return RouteResult.empty();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cozeConfig.getAccessToken());

            Map<String, Object> params = new HashMap<>();
            params.put("origin", origin);
            params.put("destination", destination);
            params.put("waypoints", waypoints == null ? "" : String.join("|", waypoints));

            Map<String, Object> body = new HashMap<>();
            body.put("workflow_id", cozeConfig.getRouteWorkflowId());
            body.put("parameters", params);

            String apiUrl = cozeConfig.getApiBaseUrl() + "/v1/workflow/run";
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            // Coze workflow/run 成功 code=0，data 字段为工作流输出 JSON 字符串
            if (root.path("code").asInt(-1) != 0) {
                log.warn("Coze 工作流返回异常: {}", response.getBody());
                return RouteResult.empty();
            }

            // data 可能是字符串（JSON）或直接对象
            JsonNode dataNode = root.path("data");
            JsonNode payload = dataNode.isTextual()
                    ? objectMapper.readTree(dataNode.asText())
                    : dataNode;

            long distance = payload.path("distance").asLong(0);
            String polylineStr = payload.path("polyline").asText("");

            List<TrackRoute.GeoPoint> points = parsePolyline(polylineStr);
            return new RouteResult(distance, points);

        } catch (Exception e) {
            log.error("Coze 路径规划调用失败: {}", e.getMessage(), e);
            return RouteResult.empty();
        }
    }

    /**
     * 解析高德 polyline 字符串为坐标点列表。
     * 格式：lng1,lat1;lng2,lat2;...
     */
    private List<TrackRoute.GeoPoint> parsePolyline(String polyline) {
        List<TrackRoute.GeoPoint> points = new ArrayList<>();
        if (!StringUtils.hasText(polyline)) return points;
        for (String pair : polyline.split(";")) {
            String[] parts = pair.trim().split(",");
            if (parts.length == 2) {
                try {
                    double lng = Double.parseDouble(parts[0].trim());
                    double lat = Double.parseDouble(parts[1].trim());
                    points.add(new TrackRoute.GeoPoint(lng, lat));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return points;
    }

    public record RouteResult(long distanceMeters, List<TrackRoute.GeoPoint> points) {
        static RouteResult empty() {
            return new RouteResult(0L, Collections.emptyList());
        }
    }
}
