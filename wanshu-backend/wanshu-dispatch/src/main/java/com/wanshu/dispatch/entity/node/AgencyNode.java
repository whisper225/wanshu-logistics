package com.wanshu.dispatch.entity.node;

import com.wanshu.dispatch.entity.line.TransportLine;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * 营业部节点（AGENCY）
 * 如：金牛区营业部、青羊区营业部、碑林区营业部等
 * 物流网络的叶子节点，快递员取/派件的实际作业网点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Node("AGENCY")
public class AgencyNode extends BaseOrganNode {

    /** 出站线路（营业部→分拣中心） */
    @Relationship(type = "OUT_LINE", direction = Relationship.Direction.OUTGOING)
    private List<TransportLine> outLines = new ArrayList<>();

    /** 入站线路（分拣中心→营业部） */
    @Relationship(type = "IN_LINE", direction = Relationship.Direction.OUTGOING)
    private List<TransportLine> inLines = new ArrayList<>();
}
