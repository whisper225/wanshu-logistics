package com.wanshu.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.common.exception.BusinessException;
import com.wanshu.common.result.ResultCode;
import com.wanshu.model.entity.sys.SysExceptionLog;
import com.wanshu.model.entity.sys.SysOperationLog;
import com.wanshu.system.mapper.SysExceptionLogMapper;
import com.wanshu.system.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SysLogService {

    private final SysOperationLogMapper operationLogMapper;
    private final SysExceptionLogMapper exceptionLogMapper;

    public IPage<SysOperationLog> pageOperationLogs(
            int pageNum,
            int pageSize,
            String keyword,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysOperationLog::getModule, kw)
                    .or().like(SysOperationLog::getOperation, kw)
                    .or().like(SysOperationLog::getOperatorName, kw)
                    .or().like(SysOperationLog::getRequestUrl, kw));
        }
        if (startTime != null) {
            wrapper.ge(SysOperationLog::getCreatedTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysOperationLog::getCreatedTime, endTime);
        }
        wrapper.orderByDesc(SysOperationLog::getCreatedTime);
        return operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public IPage<SysExceptionLog> pageExceptionLogs(
            int pageNum,
            int pageSize,
            String keyword,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        LambdaQueryWrapper<SysExceptionLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysExceptionLog::getModule, kw)
                    .or().like(SysExceptionLog::getExceptionName, kw)
                    .or().like(SysExceptionLog::getExceptionMsg, kw)
                    .or().like(SysExceptionLog::getRequestUrl, kw));
        }
        if (startTime != null) {
            wrapper.ge(SysExceptionLog::getCreatedTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysExceptionLog::getCreatedTime, endTime);
        }
        wrapper.orderByDesc(SysExceptionLog::getCreatedTime);
        return exceptionLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public SysOperationLog getOperationLog(Long id) {
        SysOperationLog row = operationLogMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "操作日志不存在");
        }
        return row;
    }

    public SysExceptionLog getExceptionLog(Long id) {
        SysExceptionLog row = exceptionLogMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "异常日志不存在");
        }
        return row;
    }

    public void saveOperationLog(SysOperationLog log) {
        operationLogMapper.insert(log);
    }

    @Async
    public void saveOperationLogAsync(SysOperationLog log) {
        try {
            operationLogMapper.insert(log);
        } catch (Exception ignored) {
            /* 避免日志失败影响主流程 */
        }
    }

    public void saveExceptionLog(SysExceptionLog log) {
        exceptionLogMapper.insert(log);
    }
}
