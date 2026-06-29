package com.xhbookstore.api.aspect;

import java.util.Date;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.alibaba.fastjson2.JSON;
import com.xhbookstore.api.domain.ApiLog;
import com.xhbookstore.api.service.IApiLogService;

/**
 * API请求日志AOP
 * 使用 @Around 环绕通知记录所有Controller方法的入参、出参、耗时
 */
@Aspect
@Component
public class ApiLogAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiLogAspect.class);

    @Autowired
    private IApiLogService apiLogService;

    @Pointcut("execution(* com.xhbookstore.api.controller..*(..))")
    public void apiControllerPointcut() {}

    @Around("apiControllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().replace("-", "");

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String uri = request != null ? request.getRequestURI() : "";
        String method = request != null ? request.getMethod() : "";
        String clientIp = request != null ? getClientIp(request) : "";
        String userAgent = request != null ? request.getHeader("User-Agent") : "";
        String userId = request != null ? (String) request.getAttribute("userId") : "";

        // 获取入参
        Object[] args = joinPoint.getArgs();
        String requestParams = "";
        try {
            requestParams = JSON.toJSONString(args);
            if (requestParams != null && requestParams.length() > 5000) {
                requestParams = requestParams.substring(0, 5000) + "...(truncated)";
            }
        } catch (Exception e) {
            requestParams = "[serialize error]";
        }

        log.info("[API请求] requestId={} {} {} params={}", requestId, method, uri,
                requestParams.length() > 500 ? requestParams.substring(0, 500) : requestParams);

        Object result = null;
        int responseCode = 0;
        String responseBody = "";

        try {
            result = joinPoint.proceed();
            responseCode = 0;
            try {
                responseBody = JSON.toJSONString(result);
                if (responseBody != null && responseBody.length() > 5000) {
                    responseBody = responseBody.substring(0, 5000) + "...(truncated)";
                }
            } catch (Exception e) {
                responseBody = "[serialize error]";
            }
        } catch (Exception e) {
            responseCode = 500;
            responseBody = e.getMessage();
            if (responseBody != null && responseBody.length() > 1000) {
                responseBody = responseBody.substring(0, 1000);
            }
            log.error("[API异常] requestId={} {} {}", requestId, uri, e.getMessage());
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            log.info("[API响应] requestId={} {} {} code={} cost={}ms", requestId, method, uri, responseCode, costTime);

            // 异步写入日志表
            try {
                ApiLog apiLog = new ApiLog();
                apiLog.setRequestId(requestId);
                apiLog.setRequestUri(uri);
                apiLog.setHttpMethod(method);
                apiLog.setClientIp(clientIp);
                apiLog.setUserId(userId);
                apiLog.setRequestParams(requestParams);
                apiLog.setResponseBody(responseBody);
                apiLog.setResponseCode(responseCode);
                apiLog.setCostTime(costTime);
                apiLog.setUserAgent(userAgent);
                apiLog.setCreatedAt(new Date());
                apiLogService.save(apiLog);
            } catch (Exception e) {
                log.warn("[日志写入失败] requestId={}", requestId, e);
            }
        }

        return result;
    }

    private String getClientIp(HttpServletRequest request) {
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
    }
}
