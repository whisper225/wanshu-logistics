package com.wanshu.dispatch.entity.node;

import com.wanshu.dispatch.entity.line.TransportLine;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * 一级转运中心节点（OLT）
 * 如：西北转运中心、西南转运中心、华北转运中心等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Node("OLT")
public class OltNode extends BaseOrganNode {

    /** 出站线路 */
    @Relationship(type = "OUT_LINE", direction = Relationship.Direction.OUTGOING)
    private List<TransportLine> outLines = new ArrayList<>();

    /** 入站线路 */
    @Relationship(type = "IN_LINE", direction = Relationship.Direction.OUTGOING)
    private List<TransportLine> inLines = new ArrayList<>();
}
