package com.xhbookstore.api.aspect;

import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.xhbookstore.api.config.SecurityProperties;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;

/**
 * 接口限流切面
 * - 登录接口：按IP限流，防止暴力破解
 * - 写操作（借书/还书/积分）：按用户限流，防止刷接口
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    @Autowired private RedisTemplate<Object, Object> redisTemplate;
    @Autowired private SecurityProperties securityProperties;

    /** 登录接口限流 — 按IP */
    @Around("execution(* com.xhbookstore.api.controller.AuthController.wechatPhoneLogin(..))")
    public Object loginRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        String ip = getClientIp();
        String key = "rate_limit:login:" + ip;
        int limit = securityProperties.getRateLimit().getLoginPerMinute();

        if (!checkRate(key, limit, 60)) {
            log.warn("[限流-登录] IP={} 超过限制{}次/分钟", ip, limit);
            throw new ApiException(ApiErrorCode.RATE_LIMIT, "操作过于频繁，请稍后再试");
        }
        return joinPoint.proceed();
    }

    /** 积分操作限流 — 按用户 */
    @Around("execution(* com.xhbookstore.api.controller.StaffController.addPoints(..)) || " +
            "execution(* com.xhbookstore.api.controller.StaffController.deductPoints(..))")
    public Object pointsRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        String userId = getUserId();
        String key = "rate_limit:points:" + userId;
        int limit = securityProperties.getRateLimit().getPointsPerMinute();

        if (!checkRate(key, limit, 60)) {
            log.warn("[限流-积分] userId={} 超过限制{}次/分钟", userId, limit);
            throw new ApiException(ApiErrorCode.RATE_LIMIT, "积分操作过于频繁，请稍后再试");
        }
        return joinPoint.proceed();
    }

    /** 借书/还书限流 — 按用户 */
    @Around("execution(* com.xhbookstore.api.controller.StaffController.borrow(..)) || " +
            "execution(* com.xhbookstore.api.controller.StaffController.returnBooks(..))")
    public Object writeRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        String userId = getUserId();
        String key = "rate_limit:write:" + userId;
        int limit = securityProperties.getRateLimit().getWritePerMinute();

        if (!checkRate(key, limit, 60)) {
            log.warn("[限流-写操作] userId={} 超过限制{}次/分钟", userId, limit);
            throw new ApiException(ApiErrorCode.RATE_LIMIT, "操作过于频繁，请稍后再试");
        }
        return joinPoint.proceed();
    }

    private boolean checkRate(String key, int limit, int windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count == null || count <= limit;
    }

    private String getClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }

    private String getUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        Object userId = attrs.getRequest().getAttribute("userId");
        return userId != null ? userId.toString() : "anonymous";
    }
}
