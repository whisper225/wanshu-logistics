package com.wanshu.model.entity.track;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 物流跟踪节点（MongoDB）
 * <p>
 * 记录运单在各关键节点的事件（揽收、发出、到达、派送中、签收、拒收），
 * 供用户端查看结构化物流时间线。
 */
@Data
@Document(collection = "track_node")
public class TrackNode {

    @Id
    private String id;

    /** 关联运单 ID */
    @Indexed
    private Long waybillId;

    /**
     * 事件状态码（与运单 status 对应）：
     * 1=已揽收, 2=运输中/已发出, 3=已到达中转, 4=派送中, 5=已签收, 6=已拒收
     */
    private Integer status;

    /** 事件标题，如"快件已揽收"、"快件已到达【XX转运中心】" */
    private String title;

    /** 事件详情，如"快递员：张三，电话：133xxxx" */
    private String content;

    /** 当前所在机构名称（可为 null） */
    private String organName;

    /** 事件发生时间 */
    private LocalDateTime time;
}
