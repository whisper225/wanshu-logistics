package com.wanshu.dispatch.repository;

import com.wanshu.dispatch.entity.node.AgencyNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 营业部 Repository
 */
public interface AgencyRepository extends Neo4jRepository<AgencyNode, Long> {

    Optional<AgencyNode> findByBid(Long bid);

    Optional<AgencyNode> findByName(String name);

    List<AgencyNode> findByStatus(Boolean status);

    /**
     * 查询坐标附近的营业部（启用状态），按距离排序
     *
     * @param lng          经度
     * @param lat          纬度
     * @param radiusMeters 搜索半径（米）
     * @param limit        最大返回数量
     * @return 附近营业部列表
     */
    @Query("MATCH (a:AGENCY {status: true}) " +
           "WHERE point.distance(a.location, point({longitude: $lng, latitude: $lat, crs: 'wgs-84'})) <= $radiusMeters " +
           "RETURN a " +
           "ORDER BY point.distance(a.location, point({longitude: $lng, latitude: $lat, crs: 'wgs-84'})) " +
           "LIMIT $limit")
    List<AgencyNode> findNearby(@Param("lng") double lng,
                                @Param("lat") double lat,
                                @Param("radiusMeters") double radiusMeters,
                                @Param("limit") int limit);
}

