package com.wanshu.common.event;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 机构图同步快照（MySQL base_organ → Neo4j 节点），供 Neo4j 写入使用，避免 common 依赖 model 模块。
 */
public record OrganGraphSnapshot(
        Long id,
        Long parentId,
        String organName,
        Integer organType,
        Long provinceId,
        Long cityId,
        Long countyId,
        String address,
        BigDecimal longitude,
        BigDecimal latitude,
        String managerName,
        String managerPhone,
        String contactPhone,
        Integer status
) implements Serializable {
}
