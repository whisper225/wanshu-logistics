package com.wanshu.dispatch.entity.line;

import com.wanshu.dispatch.entity.node.BaseOrganNode;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.io.Serializable;

/**
 * 运输线路关系实体
 * 对应 Neo4j 中的 OUT_LINE / IN_LINE 关系
 *
 * type: 1=干线（OLT↔OLT） 2=支线（TLT↔OLT） 3=接驳线（AGENCY↔TLT）
 */
@Data
@RelationshipProperties
public class TransportLine implements Serializable {

    @RelationshipId
    @GeneratedValue
    private Long id;

    /** 线路编号（如 XL100001） */
    private String number;

    /** 线路名称（如 "西北到西南"） */
    private String name;

    /** 线路类型：1=干线 2=支线 3=接驳线 */
    private Integer type;

    /** 运输成本（元） */
    private Double cost;

    /** 运输距离（米） */
    private Double distance;

    /** 预计耗时（秒） */
    private Long time;

    /** 起始机构业务 ID */
    private Long startOrganId;

    /** 终点机构业务 ID */
    private Long endOrganId;

    /** 创建时间戳 */
    private Long created;

    /** 更新时间戳 */
    private Long updated;

    /** 目标节点 */
    @TargetNode
    private BaseOrganNode targetNode;
}
