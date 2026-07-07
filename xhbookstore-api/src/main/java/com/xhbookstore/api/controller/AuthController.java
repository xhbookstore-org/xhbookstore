package com.xhbookstore.api.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xhbookstore.api.config.SecurityProperties;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.service.IWechatService;
import com.xhbookstore.common.core.domain.entity.SysDept;
import com.xhbookstore.common.core.domain.entity.SysUser;
import com.xhbookstore.common.utils.StringUtils;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.mapper.SysDeptMapper;
import com.xhbookstore.system.mapper.SysUserMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.member.IMemberService;

@Tag(name = "Auth API", description = "Wechat phone login, token refresh, session check and logout")
@RestController
@RequestMapping("/api/mp/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired private IWechatService wechatService;
    @Autowired private MemberMapper memberMapper;
    @Autowired private SysDeptMapper sysDeptMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private IMemberService memberService;
    @Autowired private SecurityProperties securityProperties;

    @Operation(summary = "Wechat phone login")
    @PostMapping("/wechat-phone-login")
    public ApiResponse<Map<String, Object>> wechatPhoneLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        if (StringUtils.isEmpty(code)) throw new ApiException(ApiErrorCode.PARAM_INVALID, "code is required");
        Long deptId = parseDeptId(body.get("deptId"));
        SysDept dept = sysDeptMapper.selectDeptById(deptId);
        if (dept == null || !"0".equals(dept.getStatus())) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "Store not found or disabled");
        }

        String phone = wechatService.getPhoneNumber(code);
        if (phone == null || phone.isEmpty()) throw new ApiException(ApiErrorCode.AUTH_CODE_INVALID);

        Member member = memberMapper.selectMemberByPhoneAnyStatus(phone);
        SysUser staff = sysUserMapper.selectUserByPhonenumber(phone);
        if (member != null && member.getStatus() != null && member.getStatus() == 1) {
            log.warn("[login rejected] phone={}, memberId={}, status={}", maskPhone(phone), member.getId(), member.getStatus());
        } else if (member != null) {
            Member updateMember = new Member();
            updateMember.setId(member.getId());
            updateMember.setStatus(0);
            if (member.getDeptId() == null || member.getDeptId() == 0) updateMember.setDeptId(deptId);
            updateMember.setLastOperator("wechat-login");
            memberMapper.updateMember(updateMember);
            member = memberMapper.selectMemberByPhoneAnyStatus(phone);
        } else {
            try {
                member = new Member();
                member.setPhone(phone);
                member.setName("");
                member.setDeptId(deptId);
                member.setStatus(0);
                member.setSource("wechat");
                member.setCurrentPoints(0);
                member.setBorrowCountValid(0);
                member.setSyncErp(0);
                member.setLastOperator("system");
                member.setCardNo(memberService.generateCardNo(deptId));
                memberMapper.insertMember(member);
                member = memberMapper.selectMemberByPhoneAnyStatus(phone);
                log.info("[auto register] phone={}, memberId={}", maskPhone(phone), member != null ? member.getId() : null);
            } catch (Exception e) {
                log.error("[auto register failed] {}", e.getMessage());
                member = null;
            }
        }

        boolean isMember = member != null && (member.getStatus() == null || member.getStatus() != 1);
        boolean isStaff = staff != null;
        Member activeMember = isMember ? member : null;
        String userId = activeMember != null ? "M" + activeMember.getId() : staff != null ? "S" + staff.getUserId() : UUID.randomUUID().toString();
        String secret = securityProperties.getJwt().getSecret();
        long ae = securityProperties.getJwt().getAccessTokenExpire();
        long re = securityProperties.getJwt().getRefreshTokenExpire();
        String at = Jwts.builder().setSubject(userId).claim("isMember", isMember).claim("isStaff", isStaff)
            .claim("phone", phone).claim("memberId", activeMember != null ? activeMember.getId() : null)
            .claim("deptId", activeMember != null ? activeMember.getDeptId() : null)
            .claim("staffUserId", staff != null ? staff.getUserId() : null).claim("type", "access")
            .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + ae * 1000))
            .signWith(SignatureAlgorithm.HS256, secret).compact();
        String rt = Jwts.builder().setSubject(userId).claim("isMember", isMember).claim("isStaff", isStaff)
            .claim("phone", phone).claim("memberId", activeMember != null ? activeMember.getId() : null)
            .claim("deptId", activeMember != null ? activeMember.getDeptId() : null)
            .claim("staffUserId", staff != null ? staff.getUserId() : null).claim("type", "refresh")
            .setId(UUID.randomUUID().toString()).setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + re * 1000))
            .signWith(SignatureAlgorithm.HS256, secret).compact();
        Map<String, Object> d = new HashMap<>();
        d.put("accessToken", at);
        d.put("refreshToken", rt);
        d.put("expiresIn", ae);
        d.put("isStaff", isStaff);
        d.put("staffUserId", staff != null ? staff.getUserId() : null);
        d.put("isMember", isMember);
        d.put("userId", userId);
        d.put("memberId", activeMember != null ? activeMember.getId() : null);
        d.put("deptId", activeMember != null ? activeMember.getDeptId() : null);
        d.put("needBindStore", activeMember == null || activeMember.getDeptId() == null || activeMember.getDeptId() == 0);
        return ApiResponse.success(d);
    }

    @Operation(summary = "Refresh token")
    @PostMapping("/refresh-token")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestBody Map<String, String> body) {
        String rt = body.get("refreshToken");
        if (StringUtils.isEmpty(rt)) throw new ApiException(ApiErrorCode.PARAM_INVALID, "refreshToken is required");
        String secret = securityProperties.getJwt().getSecret();
        long ae = securityProperties.getJwt().getAccessTokenExpire();
        try {
            Claims c = Jwts.parser().setSigningKey(secret).parseClaimsJws(rt).getBody();
            if (!"refresh".equals(c.get("type"))) throw new ApiException(ApiErrorCode.AUTH_TOKEN_INVALID, "Invalid refreshToken");
            String at = Jwts.builder().setSubject(c.getSubject()).claim("isMember", c.get("isMember"))
                .claim("isStaff", c.get("isStaff")).claim("phone", c.get("phone"))
                .claim("memberId", c.get("memberId")).claim("staffUserId", c.get("staffUserId"))
                .claim("deptId", c.get("deptId"))
                .claim("type", "access").setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ae * 1000))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
            Map<String, Object> d = new HashMap<>();
            d.put("accessToken", at);
            d.put("expiresIn", ae);
            return ApiResponse.success(d);
        } catch (ExpiredJwtException e) {
            throw new ApiException(ApiErrorCode.AUTH_TOKEN_EXPIRED, "refreshToken expired");
        } catch (Exception e) {
            throw new ApiException(ApiErrorCode.AUTH_TOKEN_INVALID, "Invalid refreshToken");
        }
    }

    @Operation(summary = "Check session")
    @GetMapping("/session")
    public ApiResponse<Map<String, Object>> checkSession(HttpServletRequest request) {
        String uid = (String) request.getAttribute("userId");
        Map<String, Object> d = new HashMap<>();
        d.put("valid", uid != null);
        if (uid != null) {
            d.put("isStaff", request.getAttribute("isStaff"));
            d.put("isMember", request.getAttribute("isMember"));
            d.put("staffUserId", request.getAttribute("staffUserId"));
            d.put("memberId", request.getAttribute("memberId"));
            d.put("deptId", request.getAttribute("deptId"));
            d.put("userId", uid);
        }
        return ApiResponse.success(d);
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout() {
        Map<String, Object> d = new HashMap<>();
        d.put("success", true);
        return ApiResponse.success(d);
    }

    private Long parseDeptId(String value) {
        if (StringUtils.isEmpty(value)) throw new ApiException(ApiErrorCode.PARAM_INVALID, "deptId is required");
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "deptId must be a number");
        }
    }

    private String maskPhone(String p) {
        return p != null && p.length() > 6 ? p.substring(0, 3) + "****" + p.substring(p.length() - 4) : p;
    }
}
