package com.wanshu.common.event;

/**
 * 全局异常捕获后异步写入 sys_exception_log 的事件载荷。
 */
public record SysExceptionRecordedEvent(
        String module,
        String requestUrl,
        String requestParams,
        String exceptionName,
        String exceptionMsg,
        String stackTrace,
        Long operatorId,
        String operatorName,
        String operatorIp
) {
}
