package com.wanshu.common.event;

/**
 * 机构新增或更新后，同步 Neo4j 节点（在 MySQL 事务提交后由监听器处理）。
 */
public class OrganGraphUpsertEvent {

    private final OrganGraphSnapshot snapshot;

    public OrganGraphUpsertEvent(OrganGraphSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public OrganGraphSnapshot getSnapshot() {
        return snapshot;
    }
}
