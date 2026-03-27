package com.wanshu.dispatch.repository;

import com.wanshu.dispatch.entity.node.BaseOrganNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * 路径计算专用 Repository
 * 使用自定义 Cypher 查询实现最优路径算法
 */
public interface RouteRepository extends Neo4jRepository<BaseOrganNode, Long> {

    /**
     * 最少转运次数路径（最短路径）
     * 从起始营业部到目标营业部，经过分拣中心和转运中心的最短跳数路径
     *
     * @param startBid 起始营业部业务 ID
     * @param endBid   目标营业部业务 ID
     * @return 路径上的节点和关系信息
     */
    @Query("MATCH (start {bid: $startBid}), (end {bid: $endBid}), " +
           "path = shortestPath((start)-[:OUT_LINE|IN_LINE*]-(end)) " +
           "RETURN [node IN nodes(path) | {bid: node.bid, name: node.name, labels: labels(node)}] AS nodes, " +
           "[rel IN relationships(path) | {number: rel.number, name: rel.name, type: rel.type, " +
           "cost: rel.cost, distance: rel.distance, time: rel.time}] AS lines, " +
           "length(path) AS hops")
    List<Map<String, Object>> findShortestPath(@Param("startBid") Long startBid, @Param("endBid") Long endBid);

    /**
     * 最低成本路径
     * 使用加权最短路径算法，以 cost 为权重计算成本最低的运输路径
     *
     * @param startBid 起始营业部业务 ID
     * @param endBid   目标营业部业务 ID
     * @return 路径上的节点和关系信息
     */
    @Query("MATCH (start {bid: $startBid}), (end {bid: $endBid}) " +
           "CALL apoc.algo.dijkstra(start, end, 'OUT_LINE|IN_LINE', 'cost') " +
           "YIELD path, weight " +
           "RETURN [node IN nodes(path) | {bid: node.bid, name: node.name, labels: labels(node)}] AS nodes, " +
           "[rel IN relationships(path) | {number: rel.number, name: rel.name, type: rel.type, " +
           "cost: rel.cost, distance: rel.distance, time: rel.time}] AS lines, " +
           "weight AS totalCost")
    List<Map<String, Object>> findCheapestPath(@Param("startBid") Long startBid, @Param("endBid") Long endBid);

    /**
     * 查询两个机构之间的所有可达路径（限制深度避免爆炸）
     *
     * @param startBid 起始机构业务 ID
     * @param endBid   目标机构业务 ID
     * @param maxHops  最大跳数
     * @return 所有可达路径
     */
    @Query("MATCH (start {bid: $startBid}), (end {bid: $endBid}), " +
           "path = (start)-[:OUT_LINE*1..$maxHops]->(end) " +
           "RETURN [node IN nodes(path) | {bid: node.bid, name: node.name, labels: labels(node)}] AS nodes, " +
           "[rel IN relationships(path) | {number: rel.number, name: rel.name, type: rel.type, " +
           "cost: rel.cost, distance: rel.distance, time: rel.time}] AS lines, " +
           "reduce(totalCost = 0.0, rel IN relationships(path) | totalCost + rel.cost) AS totalCost, " +
           "reduce(totalDist = 0.0, rel IN relationships(path) | totalDist + rel.distance) AS totalDistance, " +
           "reduce(totalTime = 0, rel IN relationships(path) | totalTime + rel.time) AS totalTime " +
           "ORDER BY length(path) LIMIT 5")
    List<Map<String, Object>> findAllPaths(@Param("startBid") Long startBid,
                                           @Param("endBid") Long endBid,
                                           @Param("maxHops") int maxHops);
}
