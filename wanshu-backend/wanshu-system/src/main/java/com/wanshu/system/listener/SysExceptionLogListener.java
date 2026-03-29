package com.wanshu.system.listener;

import com.wanshu.common.event.SysExceptionRecordedEvent;
import com.wanshu.model.entity.sys.SysExceptionLog;
import com.wanshu.system.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SysExceptionLogListener {

    private final SysLogService logService;

    @Async
    @EventListener
    public void onSysExceptionRecorded(SysExceptionRecordedEvent e) {
        SysExceptionLog row = new SysExceptionLog();
        row.setModule(e.module());
        row.setRequestUrl(e.requestUrl());
        row.setRequestParams(e.requestParams());
        row.setExceptionName(e.exceptionName());
        row.setExceptionMsg(e.exceptionMsg());
        row.setStackTrace(e.stackTrace());
        row.setOperatorId(e.operatorId());
        row.setOperatorName(e.operatorName());
        row.setOperatorIp(e.operatorIp());
        row.setCreatedTime(LocalDateTime.now());
        logService.saveExceptionLog(row);
    }
}
