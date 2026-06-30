package com.xhbookstore.api.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.config.SecurityProperties;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.service.IWechatService;
import com.xhbookstore.common.utils.StringUtils;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.mapper.member.MemberMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 认证接口
 * 安全机制：
 * - accessToken 2小时 + refreshToken 30天，双Token机制
 * - 登录接口按IP限流（默认5次/分钟）
 * - JWT密钥从配置文件读取
 */
@RestController
@RequestMapping("/api/mp/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired private IWechatService wechatService;
    @Autowired private MemberMapper memberMapper;
    @Autowired private SecurityProperties securityProperties;

    /** 微信手机号登录 — 返回双Token */
    @PostMapping("/wechat-phone-login")
    public ApiResponse<Map<String, Object>> wechatPhoneLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (StringUtils.isEmpty(code)) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "缺少微信授权码");
        }

        // 获取手机号（微信API未配置时使用Mock）
        String phone = wechatService.getPhoneNumber(code);
        if (phone == null || phone.isEmpty()) {
            throw new ApiException(ApiErrorCode.AUTH_CODE_INVALID);
        }

        Member member = memberMapper.selectMemberByPhone(phone);
        boolean isStaff = member != null;
        String userId = member != null ? String.valueOf(member.getId()) : UUID.randomUUID().toString();

        String secret = securityProperties.getJwt().getSecret();
        long accessExpire = securityProperties.getJwt().getAccessTokenExpire();
        long refreshExpire = securityProperties.getJwt().getRefreshTokenExpire();

        // 生成 accessToken（短期，2小时）
        String accessToken = Jwts.builder()
                .setSubject(userId)
                .claim("isStaff", isStaff)
                .claim("phone", phone)
                .claim("memberId", member != null ? member.getId() : null)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire * 1000))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        // 生成 refreshToken（长期，30天）
        String refreshToken = Jwts.builder()
                .setSubject(userId)
                .claim("type", "refresh")
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpire * 1000))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();

        log.info("[登录] phone={}, isStaff={}", maskPhone(phone), isStaff);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("expiresIn", accessExpire);
        data.put("isStaff", isStaff);
        data.put("userId", userId);
        data.put("memberId", member != null ? member.getId() : null);
        return ApiResponse.success(data);
    }

    /** 刷新Token — 用refreshToken换新accessToken */
    @PostMapping("/refresh-token")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (StringUtils.isEmpty(refreshToken)) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "缺少refreshToken");
        }

        String secret = securityProperties.getJwt().getSecret();
        long accessExpire = securityProperties.getJwt().getAccessTokenExpire();

        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(refreshToken).getBody();
            if (!"refresh".equals(claims.get("type"))) {
                throw new ApiException(ApiErrorCode.AUTH_TOKEN_INVALID, "无效的refreshToken");
            }

            String userId = claims.getSubject();
            String newAccessToken = Jwts.builder()
                    .setSubject(userId)
                    .claim("isStaff", claims.get("isStaff"))
                    .claim("phone", claims.get("phone"))
                    .claim("memberId", claims.get("memberId"))
                    .claim("type", "access")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + accessExpire * 1000))
                    .signWith(SignatureAlgorithm.HS256, secret)
                    .compact();

            log.info("[刷新Token] userId={}", userId);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            data.put("expiresIn", accessExpire);
            return ApiResponse.success(data);
        } catch (ExpiredJwtException e) {
            throw new ApiException(ApiErrorCode.AUTH_TOKEN_EXPIRED, "refreshToken已过期，请重新登录");
        } catch (Exception e) {
            throw new ApiException(ApiErrorCode.AUTH_TOKEN_INVALID, "无效的refreshToken");
        }
    }

    /** 校验登录态 */
    @GetMapping("/session")
    public ApiResponse<Map<String, Object>> checkSession(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Map<String, Object> data = new HashMap<>();
        data.put("valid", userId != null);
        if (userId != null) {
            data.put("isStaff", request.getAttribute("isStaff"));
            data.put("userId", userId);
        }
        return ApiResponse.success(data);
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout() {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        return ApiResponse.success(data);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
