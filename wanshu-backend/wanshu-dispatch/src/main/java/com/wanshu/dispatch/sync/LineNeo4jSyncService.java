package com.wanshu.dispatch.sync;

import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.dispatch.DispatchLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 将 MySQL 线路同步为 Neo4j OUT_LINE / IN_LINE 关系。
 * <p>
 * 使用 {@link Session#executeWrite} 在单一 Neo4j 写事务内完成「删旧关系 + 建新关系」（upsert 语义）；
 * 不套用 Spring {@code @Transactional}，避免与 JDBC 事务管理器混用产生歧义。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineNeo4jSyncService {

    private final Driver neo4jDriver;

    /**
     * 创建或更新线路的 Neo4j 关系（OUT_LINE：startOrgan→endOrgan，IN_LINE：endOrgan→startOrgan）。
     * 先按 lineNumber 删除旧关系，再重新创建，实现 upsert 语义。
     */
    public void upsert(DispatchLine line, BaseOrgan startOrgan, BaseOrgan endOrgan) {
        String lineNumber = line.getLineNumber();
        Long startBid = startOrgan.getId();
        Long endBid = endOrgan.getId();

        Map<String, Object> props = buildProps(line, startOrgan, endOrgan);

        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                // 先删除旧关系（按 lineNumber 去重，避免创建重复关系）
                tx.run("MATCH ()-[r:OUT_LINE {number: $num}]-() DELETE r",
                        Values.parameters("num", lineNumber));
                tx.run("MATCH ()-[r:IN_LINE {number: $num}]-() DELETE r",
                        Values.parameters("num", lineNumber));

                // 创建 OUT_LINE: start → end，IN_LINE: end → start
                tx.run("MATCH (s {bid: $startBid}), (e {bid: $endBid}) " +
                               "CREATE (s)-[:OUT_LINE $props]->(e), (e)-[:IN_LINE $props]->(s)",
                        Values.parameters("startBid", startBid, "endBid", endBid, "props", props));

                return null;
            });
            log.info("Neo4j 线路关系已写入, lineNumber={}, start={}, end={}", lineNumber, startBid, endBid);
        }
    }

    /**
     * 按线路编号删除 Neo4j 中的 OUT_LINE / IN_LINE 关系。
     */
    public void deleteByLineNumber(String lineNumber) {
        if (lineNumber == null) {
            return;
        }
        try (Session session = neo4jDriver.session()) {
            session.executeWrite(tx -> {
                tx.run("MATCH ()-[r:OUT_LINE {number: $num}]-() DELETE r",
                        Values.parameters("num", lineNumber));
                tx.run("MATCH ()-[r:IN_LINE {number: $num}]-() DELETE r",
                        Values.parameters("num", lineNumber));
                return null;
            });
            log.info("Neo4j 线路关系已删除, lineNumber={}", lineNumber);
        }
    }

    private Map<String, Object> buildProps(DispatchLine line, BaseOrgan startOrgan, BaseOrgan endOrgan) {
        Map<String, Object> props = new HashMap<>();
        props.put("number", line.getLineNumber());
        props.put("name", line.getLineName() != null ? line.getLineName() : "");
        props.put("type", line.getLineType() != null ? line.getLineType() : 0);
        props.put("cost", toDouble(line.getCost()));
        props.put("distance", toDouble(line.getDistance()));
        props.put("time", line.getEstimatedTime() != null ? line.getEstimatedTime() : 0L);
        props.put("startOrganId", startOrgan.getId());
        props.put("endOrganId", endOrgan.getId());
        long now = System.currentTimeMillis();
        props.put("created", now);
        props.put("updated", now);
        return props;
    }

    private double toDouble(BigDecimal v) {
        return v != null ? v.doubleValue() : 0.0;
    }
}
