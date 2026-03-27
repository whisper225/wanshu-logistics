package com.wanshu.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanshu.model.entity.sys.SysExceptionLog;
import com.wanshu.model.entity.sys.SysOperationLog;
import com.wanshu.system.mapper.SysExceptionLogMapper;
import com.wanshu.system.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysLogService {

    private final SysOperationLogMapper operationLogMapper;
    private final SysExceptionLogMapper exceptionLogMapper;

    public IPage<SysOperationLog> pageOperationLogs(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysOperationLog::getModule, keyword)
                    .or().like(SysOperationLog::getOperation, keyword)
                    .or().like(SysOperationLog::getOperatorName, keyword);
        }
        wrapper.orderByDesc(SysOperationLog::getCreatedTime);
        return operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public IPage<SysExceptionLog> pageExceptionLogs(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SysExceptionLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysExceptionLog::getModule, keyword)
                    .or().like(SysExceptionLog::getExceptionName, keyword);
        }
        wrapper.orderByDesc(SysExceptionLog::getCreatedTime);
        return exceptionLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public void saveOperationLog(SysOperationLog log) {
        operationLogMapper.insert(log);
    }

    public void saveExceptionLog(SysExceptionLog log) {
        exceptionLogMapper.insert(log);
    }
}
