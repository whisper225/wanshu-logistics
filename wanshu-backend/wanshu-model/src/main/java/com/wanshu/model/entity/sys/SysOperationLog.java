package com.wanshu.model.entity.sys;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestParams;
    private String responseData;
    private Long operatorId;
    private String operatorName;
    private String operatorIp;
    private Long costTime;
    private Integer status;
    private LocalDateTime createdTime;
}
