package com.wanshu.dispatch.event;

import com.wanshu.model.entity.base.BaseOrgan;
import com.wanshu.model.entity.dispatch.DispatchLine;

/**
 * 线路新增或更新后，同步 Neo4j 关系（OUT_LINE / IN_LINE）。
 * 在 MySQL 事务提交后由 LineGraphEventListener 处理。
 */
public class LineGraphUpsertEvent {

    private final DispatchLine line;
    private final BaseOrgan startOrgan;
    private final BaseOrgan endOrgan;

    public LineGraphUpsertEvent(DispatchLine line, BaseOrgan startOrgan, BaseOrgan endOrgan) {
        this.line = line;
        this.startOrgan = startOrgan;
        this.endOrgan = endOrgan;
    }

    public DispatchLine getLine() {
        return line;
    }

    public BaseOrgan getStartOrgan() {
        return startOrgan;
    }

    public BaseOrgan getEndOrgan() {
        return endOrgan;
    }
}
