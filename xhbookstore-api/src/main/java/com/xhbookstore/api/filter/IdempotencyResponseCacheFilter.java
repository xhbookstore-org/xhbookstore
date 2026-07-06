package com.xhbookstore.api.filter;

import com.xhbookstore.api.interceptor.IdempotencyInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class IdempotencyResponseCacheFilter extends OncePerRequestFilter {
    private final RedisTemplate<Object, Object> redisTemplate;

    public IdempotencyResponseCacheFilter(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapper);
        } finally {
            cacheSuccessfulResponse(request, wrapper);
            wrapper.copyBodyToResponse();
        }
    }

    private void cacheSuccessfulResponse(HttpServletRequest request, ContentCachingResponseWrapper response) {
        Object cacheKey = request.getAttribute(IdempotencyInterceptor.REQUEST_CACHE_KEY_ATTR);
        if (cacheKey == null || response.getStatus() < 200 || response.getStatus() >= 300) {
            return;
        }
        byte[] body = response.getContentAsByteArray();
        if (body.length == 0) {
            return;
        }
        String contentType = response.getContentType();
        if (contentType != null && !contentType.contains("application/json")) {
            return;
        }
        String payload = new String(body, StandardCharsets.UTF_8);
        redisTemplate.opsForValue().set(
                cacheKey.toString(),
                IdempotencyInterceptor.SUCCESS_PREFIX + payload,
                IdempotencyInterceptor.cacheMinutes(),
                TimeUnit.MINUTES);
    }
}
