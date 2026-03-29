package com.wanshu.system.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanshu.model.entity.sys.SysOperationLog;
import com.wanshu.model.entity.sys.SysUser;
import com.wanshu.system.mapper.SysUserMapper;
import com.wanshu.system.service.SysLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 记录管理端 API 操作日志（异步写入，失败不影响业务）。
 */
@Slf4j
@Aspect
@Component
@Order(100)
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysLogService logService;
    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Around("execution(public * com.wanshu..controller..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
        if (request == null || shouldSkip(request.getRequestURI())) {
            return pjp.proceed();
        }

        long start = System.currentTimeMillis();
        int status = 1;
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable ex) {
            status = 0;
            throw ex;
        } finally {
            try {
                long cost = System.currentTimeMillis() - start;
                SysOperationLog op = buildLog(pjp, request, status, cost);
                logService.saveOperationLogAsync(op);
            } catch (Exception e) {
                log.debug("操作日志写入跳过: {}", e.getMessage());
            }
        }
        return result;
    }

    private boolean shouldSkip(String uri) {
        if (uri == null) {
            return true;
        }
        if (uri.startsWith("/api/auth/")) {
            return true;
        }
        if (uri.startsWith("/api/system/log/")) {
            return true;
        }
        if (uri.startsWith("/doc") || uri.startsWith("/webjars") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger")) {
            return true;
        }
        if (uri.contains("error")) {
            return true;
        }
        return false;
    }

    private SysOperationLog buildLog(ProceedingJoinPoint pjp, HttpServletRequest request, int status, long cost) {
        Object target = pjp.getTarget();
        Class<?> userClass = ClassUtils.getUserClass(target);
        Tag tag = userClass.getAnnotation(Tag.class);
        String module = tag != null && StringUtils.hasText(tag.name()) ? tag.name() : userClass.getSimpleName();

        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        io.swagger.v3.oas.annotations.Operation apiOp = method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
        String operation = apiOp != null && StringUtils.hasText(apiOp.summary()) ? apiOp.summary() : method.getName();

        String methodStr = userClass.getSimpleName() + "." + method.getName();

        StringBuilder url = new StringBuilder(request.getRequestURI());
        String qs = request.getQueryString();
        if (StringUtils.hasText(qs)) {
            url.append('?').append(qs);
        }
        String urlStr = url.length() > 500 ? url.substring(0, 500) : url.toString();

        SysOperationLog row = new SysOperationLog();
        row.setModule(module.length() > 50 ? module.substring(0, 50) : module);
        row.setOperation(operation.length() > 100 ? operation.substring(0, 100) : operation);
        row.setMethod(methodStr.length() > 200 ? methodStr.substring(0, 200) : methodStr);
        row.setRequestUrl(urlStr);
        row.setRequestParams(safeArgs(pjp.getArgs()));
        row.setResponseData(null);
        row.setOperatorId(resolveOperatorId());
        row.setOperatorName(resolveOperatorName(row.getOperatorId()));
        row.setOperatorIp(clientIp(request));
        row.setCostTime(cost);
        row.setStatus(status);
        row.setCreatedTime(LocalDateTime.now());
        return row;
    }

    private Long resolveOperatorId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {
            /* */
        }
        return null;
    }

    private String resolveOperatorName(Long userId) {
        if (userId == null) {
            return "匿名";
        }
        try {
            SysUser u = userMapper.selectById(userId);
            if (u != null) {
                if (StringUtils.hasText(u.getRealName())) {
                    return u.getRealName();
                }
                if (StringUtils.hasText(u.getUsername())) {
                    return u.getUsername();
                }
            }
        } catch (Exception ignored) {
            /* */
        }
        return "uid:" + userId;
    }

    private String clientIp(HttpServletRequest request) {
        String h = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(h) && !"unknown".equalsIgnoreCase(h)) {
            return h.split(",")[0].trim();
        }
        h = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(h) && !"unknown".equalsIgnoreCase(h)) {
            return h.trim();
        }
        return request.getRemoteAddr();
    }

    private String safeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            List<Object> safe = new ArrayList<>();
            for (Object a : args) {
                if (a == null) {
                    continue;
                }
                if (a instanceof ServletRequest || a instanceof ServletResponse) {
                    continue;
                }
                if (a instanceof MultipartFile || a instanceof MultipartFile[]) {
                    continue;
                }
                safe.add(a);
            }
            if (safe.isEmpty()) {
                return "";
            }
            String s = objectMapper.writeValueAsString(safe);
            return s.length() > 2000 ? s.substring(0, 2000) + "..." : s;
        } catch (Exception e) {
            return "[参数序列化失败]";
        }
    }
}
