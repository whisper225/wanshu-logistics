package com.wanshu.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wanshu.common.result.R;
import com.wanshu.model.entity.sys.SysExceptionLog;
import com.wanshu.model.entity.sys.SysOperationLog;
import com.wanshu.system.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "日志管理")
@RestController
@RequestMapping("/api/system/log")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService logService;

    @Operation(summary = "操作日志分页列表")
    @GetMapping("/operation/page")
    public R<Map<String, Object>> operationLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        IPage<SysOperationLog> page = logService.pageOperationLogs(pageNum, pageSize, keyword, startTime, endTime);
        return R.ok(pageToMap(page));
    }

    @Operation(summary = "操作日志详情")
    @GetMapping("/operation/{id}")
    public R<SysOperationLog> operationDetail(@PathVariable Long id) {
        return R.ok(logService.getOperationLog(id));
    }

    @Operation(summary = "异常日志分页列表")
    @GetMapping("/exception/page")
    public R<Map<String, Object>> exceptionLogs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        IPage<SysExceptionLog> page = logService.pageExceptionLogs(pageNum, pageSize, keyword, startTime, endTime);
        return R.ok(pageToMap(page));
    }

    @Operation(summary = "异常日志详情")
    @GetMapping("/exception/{id}")
    public R<SysExceptionLog> exceptionDetail(@PathVariable Long id) {
        return R.ok(logService.getExceptionLog(id));
    }

    private static Map<String, Object> pageToMap(IPage<?> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        return result;
    }
}
