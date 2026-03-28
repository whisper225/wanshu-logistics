package com.wanshu.common.event;

/**
 * 机构从 MySQL 删除后，移除 Neo4j 中 bid 对应节点（在 MySQL 事务提交后由监听器处理）。
 */
public class OrganGraphDeleteEvent {

    private final Long organId;

    public OrganGraphDeleteEvent(Long organId) {
        this.organId = organId;
    }

    public Long getOrganId() {
        return organId;
    }
}
