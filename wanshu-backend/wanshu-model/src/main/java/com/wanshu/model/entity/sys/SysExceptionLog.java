package com.wanshu.model.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 异常日志
 */
@Data
@TableName("sys_exception_log")
public class SysExceptionLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String module;
    private String requestUrl;
    private String requestParams;
    private String exceptionName;
    private String exceptionMsg;
    private String stackTrace;
    private Long operatorId;
    private String operatorName;
    private String operatorIp;
    private LocalDateTime createdTime;
}
