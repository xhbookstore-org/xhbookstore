package com.xhbookstore.api.interceptor;

import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.alibaba.fastjson2.JSON;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.model.ApiResponse;

/**
 * 幂等性拦截器
 * 写接口需携带 Idempotency-Key 头，5分钟内相同key重复请求直接返回缓存结果
 */
@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyInterceptor.class);
    private static final long CACHE_MINUTES = 5;

    @Autowired private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 仅处理 POST/PUT 写请求
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            return true;
        }
        // 白名单（登录等无需幂等key）
        String uri = request.getRequestURI();
        if (uri.contains("/auth/") || uri.contains("/files/")) {
            return true;
        }

        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            return true; // 不强制要求
        }

        String cacheKey = "idempotent:" + idempotencyKey;
        String cached = (String) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            // 重复请求，直接返回缓存结果
            log.info("[幂等] key={} 命中缓存，直接返回", idempotencyKey);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(cached);
            return false; // 拦截，不执行Controller
        }

        // 标记该key正在处理（5分钟过期）
        redisTemplate.opsForValue().set(cacheKey, "processing", CACHE_MINUTES, TimeUnit.MINUTES);
        return true;
    }
}
