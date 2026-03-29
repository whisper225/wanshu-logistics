package com.wanshu.common.exception;

import cn.dev33.satoken.stp.StpUtil;
import com.wanshu.common.event.SysExceptionRecordedEvent;
import com.wanshu.common.result.R;
import com.wanshu.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int STACK_MAX = 12000;

    private final ApplicationEventPublisher eventPublisher;

    public GlobalExceptionHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}]: {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验异常: {}", message);
        return R.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        log.warn("参数绑定异常: {}", message);
        return R.fail(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 未知异常兜底（并异步落库异常日志）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        publishExceptionLog(e, request);
        return R.fail(ResultCode.FAIL);
    }

    private void publishExceptionLog(Exception e, HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            String qs = request.getQueryString();
            String params = qs != null ? qs : "";
            if (params.length() > 2000) {
                params = params.substring(0, 2000) + "...";
            }
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            if (stack.length() > STACK_MAX) {
                stack = stack.substring(0, STACK_MAX) + "\n...(truncated)";
            }
            Long opId = null;
            String opName = null;
            try {
                if (StpUtil.isLogin()) {
                    opId = StpUtil.getLoginIdAsLong();
                    opName = "uid:" + opId;
                }
            } catch (Exception ignored) {
                /* ignore */
            }
            String ip = clientIp(request);
            eventPublisher.publishEvent(new SysExceptionRecordedEvent(
                    moduleFromUri(uri),
                    uri,
                    params,
                    e.getClass().getName(),
                    e.getMessage() != null ? e.getMessage() : "",
                    stack,
                    opId,
                    opName,
                    ip
            ));
        } catch (Exception ex) {
            log.warn("发布异常日志事件失败: {}", ex.getMessage());
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String h = request.getHeader("X-Forwarded-For");
        if (h != null && !h.isEmpty() && !"unknown".equalsIgnoreCase(h)) {
            return h.split(",")[0].trim();
        }
        h = request.getHeader("X-Real-IP");
        if (h != null && !h.isEmpty() && !"unknown".equalsIgnoreCase(h)) {
            return h.trim();
        }
        return request.getRemoteAddr();
    }

    private static String moduleFromUri(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "未知";
        }
        if (uri.startsWith("/api/system/")) {
            return "系统管理";
        }
        if (uri.startsWith("/api/base/")) {
            return "基础数据";
        }
        if (uri.startsWith("/api/business/")) {
            return "业务管理";
        }
        if (uri.startsWith("/api/dispatch/")) {
            return "调度管理";
        }
        if (uri.startsWith("/api/auth/")) {
            return "认证";
        }
        return "接口";
    }
}
