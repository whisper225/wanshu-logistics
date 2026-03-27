package com.wanshu.dispatch.entity.node;

import com.wanshu.dispatch.entity.line.TransportLine;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * 二级分拣中心节点（TLT）
 * 如：成都分拣中心、西安分拣中心、北京分拣中心等
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Node("TLT")
public class TltNode extends BaseOrganNode {

    /** 出站线路 */
    @Relationship(type = "OUT_LINE", direction = Relationship.Direction.OUTGOING)
    private List<TransportLine> outLines = new ArrayList<>();

    /** 入站线路 */
    @Relationship(type = "IN_LINE", direction = Relationship.Direction.OUTGOING)
    private List<TransportLine> inLines = new ArrayList<>();
}
