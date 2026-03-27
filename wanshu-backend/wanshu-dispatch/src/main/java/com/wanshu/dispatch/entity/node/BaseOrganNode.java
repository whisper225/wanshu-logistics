package com.wanshu.dispatch.entity.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.types.GeographicPoint2d;

import java.io.Serializable;

/**
 * Neo4j 机构节点基类
 * 所有机构（转运中心/分拣中心/营业部）共享的属性
 */
@Data
public abstract class BaseOrganNode implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    /** 业务 ID（对应 MySQL 中的机构 ID） */
    private Long bid;

    /** 机构名称 */
    private String name;

    /** 联系电话 */
    private String phone;

    /** 负责人姓名 */
    private String managerName;

    /** 地址 JSON（包含省市区及详细地址） */
    private String address;

    /** 地理坐标（WGS-84） */
    private GeographicPoint2d location;

    /** 上级机构业务 ID（0 表示顶级） */
    private Long parentId;

    /** 状态：true=启用 */
    private Boolean status;
}
