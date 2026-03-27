package com.wanshu.model.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统消息
 */
@Data
@TableName("sys_message")
public class SysMessage implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private String content;
    private Integer msgType;
    private Long targetId;
    private Integer isRead;
    private LocalDateTime createdTime;
}
