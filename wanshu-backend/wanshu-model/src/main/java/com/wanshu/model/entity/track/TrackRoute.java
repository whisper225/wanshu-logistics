package com.wanshu.model.entity.track;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单轨迹路线（MongoDB）
 * <p>
 * 由 Coze 工作流调用高德路线规划 API 生成，存储 polyline 坐标点列表，
 * 供小程序地图组件渲染物流路径动画。
 */
@Data
@Document(collection = "track_route")
public class TrackRoute {

    @Id
    private String id;

    /** 关联的订单 ID */
    @Indexed(unique = true)
    private Long orderId;

    /** 关联的运单 ID */
    @Indexed
    private Long waybillId;

    /** 轨迹状态：0=进行中，1=已完成 */
    private Integer status;

    /** 当前实时位置（快递员/司机最新上报） */
    private GeoPoint currentLocation;

    /** 最后一次位置上报时间 */
    private LocalDateTime lastLocationTime;

    /** 起点地址（寄件地址，人类可读） */
    private String originAddress;

    /** 终点地址（收件地址，人类可读） */
    private String destAddress;

    /** 途经机构坐标（经度,纬度 字符串列表，来自运单 transportLine） */
    private List<String> waypoints;

    /** 总距离（米） */
    private Long distanceMeters;

    /**
     * 抽稀后的坐标点列表（lng,lat 格式），最多 500 点，
     * 直接映射到小程序 map 组件 polyline.points。
     */
    private List<GeoPoint> polylinePoints;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    @Data
    public static class GeoPoint {
        private double longitude;
        private double latitude;

        public GeoPoint() {}

        public GeoPoint(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }
}
