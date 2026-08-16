package com.dlbyy.blog.aspect;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.entity.OperationLog;
import com.dlbyy.blog.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 后台操作日志切面
 * <p>
 * 拦截所有标注 {@link Admin} 的后台写接口，记录操作人、请求方法、URI、
 * 请求参数（password 字段脱敏）、客户端 IP、执行结果、异常信息与耗时，
 * 交由 {@link OperationLogService} 异步落库。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    /**
     * 参数最大记录长度，超出截断
     */
    private static final int MAX_PARAMS_LENGTH = 2000;

    /**
     * 异常信息最大记录长度，超出截断
     */
    private static final int MAX_ERROR_LENGTH = 2000;

    /**
     * 匹配 JSON 串中的 password 字段值，不区分大小写，用于脱敏
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("(\"password\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE);

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(admin)")
    public Object around(ProceedingJoinPoint joinPoint, Admin admin) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String params = buildParams(joinPoint.getArgs());
        long start = System.currentTimeMillis();
        OperationLog operationLog = new OperationLog();
        operationLog.setOperation(admin.value());
        operationLog.setMethod(request != null ? request.getMethod() : null);
        operationLog.setUri(request != null ? request.getRequestURI() : null);
        operationLog.setIp(getClientIp(request));
        operationLog.setUsername(getCurrentUsername());
        operationLog.setParams(params);

        Throwable error = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
            operationLog.setStatus(1);
        } catch (Throwable e) {
            error = e;
            operationLog.setStatus(0);
            String errorMsg = e.getClass().getName() + ": " + e.getMessage();
            operationLog.setErrorMsg(errorMsg.length() > MAX_ERROR_LENGTH
                    ? errorMsg.substring(0, MAX_ERROR_LENGTH) : errorMsg);
        }
        operationLog.setCostMs(System.currentTimeMillis() - start);

        try {
            operationLogService.asyncSave(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败 | uri={}", operationLog.getUri(), e);
        }

        if (error != null) {
            throw error;
        }
        return result;
    }

    /**
     * 序列化请求参数：跳过 Servlet 对象与文件类型，password 字段脱敏，超长截断
     */
    private String buildParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile) {
                continue;
            }
            try {
                String json = objectMapper.writeValueAsString(arg);
                json = PASSWORD_PATTERN.matcher(json).replaceAll("$1******$2");
                parts.add(json);
            } catch (Exception e) {
                parts.add(arg.getClass().getName());
            }
        }
        if (parts.isEmpty()) {
            return null;
        }
        String joined = String.join(",", parts);
        return joined.length() > MAX_PARAMS_LENGTH
                ? joined.substring(0, MAX_PARAMS_LENGTH) + "..." : joined;
    }

    /**
     * 获取客户端真实 IP（兼容多级反向代理场景）
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            log.warn("获取客户端 IP 失败", e);
            return null;
        }
    }

    /**
     * 获取当前登录用户名，未认证时返回 anonymous
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
