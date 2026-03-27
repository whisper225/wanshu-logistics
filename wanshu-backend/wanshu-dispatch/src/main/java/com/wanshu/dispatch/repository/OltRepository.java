package com.wanshu.dispatch.repository;

import com.wanshu.dispatch.entity.node.OltNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 一级转运中心 Repository
 */
public interface OltRepository extends Neo4jRepository<OltNode, Long> {

    Optional<OltNode> findByBid(Long bid);

    Optional<OltNode> findByName(String name);

    List<OltNode> findByStatus(Boolean status);

    /** 查询所有启用的转运中心（不加载关系，轻量查询） */
    @Query("MATCH (n:OLT {status: true}) RETURN n")
    List<OltNode> findAllActive();
}
