package com.xhbookstore.api.interceptor;

import com.alibaba.fastjson2.JSON;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {
    public static final String REQUEST_CACHE_KEY_ATTR = "idempotencyCacheKey";
    public static final String PROCESSING = "PROCESSING";
    public static final String SUCCESS_PREFIX = "SUCCESS:";

    private static final Logger log = LoggerFactory.getLogger(IdempotencyInterceptor.class);
    private static final long PROCESSING_SECONDS = 30;
    private static final long CACHE_MINUTES = 5;

    @Autowired private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.contains("/auth/") || uri.contains("/files/")) {
            return true;
        }

        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return true;
        }

        String cacheKey = buildCacheKey(request, idempotencyKey.trim());
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        String cached = cachedValue != null ? cachedValue.toString() : null;
        if (cached != null) {
            if (cached.startsWith(SUCCESS_PREFIX)) {
                log.info("[Idempotency] hit success cache, key={}", cacheKey);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(cached.substring(SUCCESS_PREFIX.length()));
                return false;
            }
            writeJson(response, HttpServletResponse.SC_CONFLICT, ApiErrorCode.RATE_LIMIT, "请求处理中，请勿重复提交");
            return false;
        }

        Boolean locked = redisTemplate.opsForValue().setIfAbsent(cacheKey, PROCESSING, PROCESSING_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            writeJson(response, HttpServletResponse.SC_CONFLICT, ApiErrorCode.RATE_LIMIT, "请求处理中，请勿重复提交");
            return false;
        }
        request.setAttribute(REQUEST_CACHE_KEY_ATTR, cacheKey);
        return true;
    }

    private String buildCacheKey(HttpServletRequest request, String idempotencyKey) {
        Object userId = request.getAttribute("userId");
        String userPart = userId != null ? userId.toString() : "anonymous";
        return "idempotent:" + userPart + ":" + request.getMethod() + ":" + request.getRequestURI() + ":" + idempotencyKey;
    }

    private void writeJson(HttpServletResponse response, int httpStatus, int code, String message) throws Exception {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(ApiResponse.error(code, message)));
    }

    public static long cacheMinutes() {
        return CACHE_MINUTES;
    }
}
