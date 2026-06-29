package com.xhbookstore.api.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.common.utils.StringUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 认证接口 - 文档 §9
 */
@RestController
@RequestMapping("/api/mp/v1/auth")
public class AuthController {

    @Autowired(required = false)
    private HttpServletRequest request;

    private static final String TOKEN_SECRET = "abcdefghijklmnopqrstuvwxyz";
    private static final long TOKEN_EXPIRE_MS = 30 * 24 * 60 * 60 * 1000L; // 30天

    /**
     * 微信手机号登录 §9.1
     */
    @PostMapping("/wechat-phone-login")
    public ApiResponse<Map<String, Object>> wechatPhoneLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (StringUtils.isEmpty(code)) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "缺少微信授权码");
        }

        // TODO: 调用微信 getPhoneNumber 接口换取手机号
        // 当前模拟：直接用code作为手机号标识
        String phone = "138****0001"; // Mock数据

        // 生成Token
        String userId = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .setSubject(userId)
                .claim("isStaff", false)
                .claim("phone", phone)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_MS))
                .signWith(SignatureAlgorithm.HS256, TOKEN_SECRET)
                .compact();

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", token);
        data.put("expiresIn", TOKEN_EXPIRE_MS / 1000);
        data.put("isStaff", false);
        data.put("userId", userId);
        data.put("memberId", null);
        return ApiResponse.success(data);
    }

    /**
     * 校验登录态 §9.2
     */
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

    /**
     * 退出登录 §9.3
     */
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout() {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        return ApiResponse.success(data);
    }
}
