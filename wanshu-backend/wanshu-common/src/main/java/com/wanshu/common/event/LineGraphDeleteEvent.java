package com.wanshu.common.event;

/**
 * 线路删除后，同步删除 Neo4j 中对应的 OUT_LINE / IN_LINE 关系。
 * 在 MySQL 事务提交后由 LineGraphEventListener 处理。
 */
public class LineGraphDeleteEvent {

    private final String lineNumber;
    private final Long startOrganId;
    private final Long endOrganId;

    public LineGraphDeleteEvent(String lineNumber, Long startOrganId, Long endOrganId) {
        this.lineNumber = lineNumber;
        this.startOrganId = startOrganId;
        this.endOrganId = endOrganId;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public Long getStartOrganId() {
        return startOrganId;
    }

    public Long getEndOrganId() {
        return endOrganId;
    }
}
