package com.wanshu.dispatch.service;

import com.wanshu.common.exception.BusinessException;
import com.wanshu.model.entity.base.BaseOrgan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 调用高德地图「Web服务」REST API 计算两点间驾车距离。
 * <p>
 * 需要在高德开放平台创建「Web服务」类型 Key，与前端 JS Key 不同。
 * 通过环境变量 AMAP_REST_KEY 或 application.yml 中 amap.rest.key 注入。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmapRestService {

    private static final String DRIVING_URL = "https://restapi.amap.com/v3/direction/driving";

    @Value("${amap.rest.key:}")
    private String amapKey;

    private final RestTemplate restTemplate;

    /**
     * 计算两个机构间的驾车距离（米）。
     *
     * @param origin      起始机构（需有 longitude/latitude 坐标）
     * @param destination 目的机构（需有 longitude/latitude 坐标）
     * @return 驾车距离（米）
     * @throws BusinessException 坐标为空、Key 未配置或 API 调用失败时
     */
    public double getDrivingDistanceMeters(BaseOrgan origin, BaseOrgan destination) {
        if (!StringUtils.hasText(amapKey)) {
            throw new BusinessException("高德 Web 服务 Key 未配置（amap.rest.key），无法自动计算距离");
        }
        BigDecimal oLng = origin.getLongitude();
        BigDecimal oLat = origin.getLatitude();
        BigDecimal dLng = destination.getLongitude();
        BigDecimal dLat = destination.getLatitude();
        if (oLng == null || oLat == null) {
            throw new BusinessException("起始机构「" + origin.getOrganName() + "」未设置经纬度坐标，无法计算距离");
        }
        if (dLng == null || dLat == null) {
            throw new BusinessException("目的机构「" + destination.getOrganName() + "」未设置经纬度坐标，无法计算距离");
        }

        String originStr = oLng.toPlainString() + "," + oLat.toPlainString();
        String destStr = dLng.toPlainString() + "," + dLat.toPlainString();

        String url = UriComponentsBuilder.fromHttpUrl(DRIVING_URL)
                .queryParam("origin", originStr)
                .queryParam("destination", destStr)
                .queryParam("key", amapKey)
                .queryParam("extensions", "base")
                .build(false)
                .toUriString();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            if (resp == null) {
                throw new BusinessException("高德 API 返回为空");
            }
            String status = (String) resp.get("status");
            if (!"1".equals(status)) {
                String info = (String) resp.get("info");
                throw new BusinessException("高德 API 返回错误：" + info);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> route = (Map<String, Object>) resp.get("route");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> paths = (List<Map<String, Object>>) route.get("paths");
            if (paths == null || paths.isEmpty()) {
                throw new BusinessException("高德 API 未返回路径信息");
            }
            Object distanceObj = paths.get(0).get("distance");
            if (distanceObj == null) {
                throw new BusinessException("高德 API 路径距离字段为空");
            }
            double meters = Double.parseDouble(distanceObj.toString());
            log.info("高德驾车距离计算成功: {} → {}, 距离={}m", originStr, destStr, meters);
            return meters;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("高德 API 调用异常: origin={}, dest={}", originStr, destStr, e);
            throw new BusinessException("高德地图距离查询失败：" + e.getMessage());
        }
    }
}
