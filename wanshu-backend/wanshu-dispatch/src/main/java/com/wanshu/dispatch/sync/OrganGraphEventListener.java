package com.wanshu.dispatch.sync;

import com.wanshu.common.event.OrganGraphDeleteEvent;
import com.wanshu.common.event.OrganGraphUpsertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * MySQL 事务提交后再写 Neo4j，避免主库未提交时图库已可见；失败只记日志。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrganGraphEventListener {

    private final OrganNeo4jSyncService organNeo4jSyncService;

    /**
     * fallbackExecution：若当前线程未绑定 JDBC 事务（监听器会被跳过），仍执行同步，避免 MySQL 已写入但图库不更新。
     * 有事务时仍以 AFTER_COMMIT 为准。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrganUpsert(OrganGraphUpsertEvent event) {
        try {
            log.debug("Neo4j 机构同步(upsert)开始, bid={}", event.getSnapshot().id());
            organNeo4jSyncService.upsert(event.getSnapshot());
        } catch (Exception e) {
            log.error("Neo4j 机构节点同步失败(upsert), bid={}", event.getSnapshot().id(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOrganDelete(OrganGraphDeleteEvent event) {
        try {
            log.debug("Neo4j 机构同步(delete)开始, bid={}", event.getOrganId());
            organNeo4jSyncService.deleteByBid(event.getOrganId());
        } catch (Exception e) {
            log.error("Neo4j 机构节点同步失败(delete), bid={}", event.getOrganId(), e);
        }
    }
}
