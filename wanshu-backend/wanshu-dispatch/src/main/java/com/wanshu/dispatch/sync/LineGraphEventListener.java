package com.wanshu.dispatch.sync;

import com.wanshu.common.event.LineGraphDeleteEvent;
import com.wanshu.common.event.LineGraphUpsertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * MySQL 事务提交后再写 Neo4j 线路关系，避免主库未提交时图库已可见；失败只记日志，不回滚主事务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LineGraphEventListener {

    private final LineNeo4jSyncService lineNeo4jSyncService;

    /**
     * fallbackExecution：若当前线程未绑定 JDBC 事务时仍执行同步，避免图库遗漏更新。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLineUpsert(LineGraphUpsertEvent event) {
        try {
            log.debug("Neo4j 线路同步(upsert)开始, lineNumber={}", event.getLine().getLineNumber());
            lineNeo4jSyncService.upsert(event.getLine(), event.getStartOrgan(), event.getEndOrgan());
        } catch (Exception e) {
            log.error("Neo4j 线路关系同步失败(upsert), lineNumber={}", event.getLine().getLineNumber(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLineDelete(LineGraphDeleteEvent event) {
        try {
            log.debug("Neo4j 线路同步(delete)开始, lineNumber={}", event.getLineNumber());
            lineNeo4jSyncService.deleteByLineNumber(event.getLineNumber());
        } catch (Exception e) {
            log.error("Neo4j 线路关系同步失败(delete), lineNumber={}", event.getLineNumber(), e);
        }
    }
}
