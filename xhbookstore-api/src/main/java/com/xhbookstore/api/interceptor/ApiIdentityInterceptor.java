package com.xhbookstore.api.interceptor;

import com.alibaba.fastjson2.JSON;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.common.constant.Constants;
import com.xhbookstore.common.core.domain.entity.SysDept;
import com.xhbookstore.common.core.domain.entity.SysRole;
import com.xhbookstore.common.core.domain.entity.SysUser;
import com.xhbookstore.system.domain.book.BookBorrowDetail;
import com.xhbookstore.system.domain.book.BookBorrowOrder;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.mapper.SysDeptMapper;
import com.xhbookstore.system.mapper.SysUserMapper;
import com.xhbookstore.system.service.book.IBookBorrowService;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.system.service.member.IPointsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ApiIdentityInterceptor implements HandlerInterceptor {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Autowired private IMemberService memberService;
    @Autowired private IBookBorrowService bookBorrowService;
    @Autowired private IPointsService pointsService;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SysDeptMapper sysDeptMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (PATH_MATCHER.match("/api/mp/v1/staff/**", uri)) {
            return requireStaff(request, response);
        }
        if (PATH_MATCHER.match("/api/mp/v1/user/**", uri) || PATH_MATCHER.match("/api/mp/v1/account/**", uri)) {
            return requireMember(request, response);
        }
        return true;
    }

    private boolean requireStaff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long staffUserId = longAttr(request, "staffUserId");
        if (!boolAttr(request, "isStaff") || staffUserId == null) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, ApiErrorCode.FORBIDDEN, "无员工权限");
            return false;
        }
        SysUser staff = sysUserMapper.selectUserById(staffUserId);
        if (staff == null || !"0".equals(staff.getStatus()) || !"0".equals(staff.getDelFlag())) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, ApiErrorCode.FORBIDDEN, "员工账号已停用或不存在");
            return false;
        }
        Long resourceDeptId = resolveStaffResourceDeptId(request);
        if (resourceDeptId != null && !visibleDeptIds(staff).contains(resourceDeptId)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, ApiErrorCode.FORBIDDEN, "无权访问该门店数据");
            return false;
        }
        return true;
    }

    private boolean requireMember(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer memberId = intAttr(request, "memberId");
        if (!boolAttr(request, "isMember") || memberId == null) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, "仅会员可访问");
            return false;
        }
        Member member = memberService.selectMemberById(memberId);
        if (member == null || member.getStatus() == null || member.getStatus() != 0) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, "会员账号已注销或不存在");
            return false;
        }
        Integer resourceMemberId = resolveUserResourceMemberId(request);
        if (resourceMemberId != null && !memberId.equals(resourceMemberId)) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, ApiErrorCode.FORBIDDEN, "无权访问该会员数据");
            return false;
        }
        return true;
    }

    private Integer resolveUserResourceMemberId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (PATH_MATCHER.match("/api/mp/v1/user/borrows/{detailId}", uri)) {
            BookBorrowDetail detail = bookBorrowService.selectDetailById(longPathVar(request, "detailId"));
            return detail != null ? detail.getMemberId() : null;
        }
        if (PATH_MATCHER.match("/api/mp/v1/user/points-records/{pointsRecordId}", uri)) {
            PointsOrder order = pointsService.selectByOrderNumber(pathVar(request, "pointsRecordId"));
            return order != null ? order.getMemberId() : null;
        }
        return null;
    }

    private Long resolveStaffResourceDeptId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (PATH_MATCHER.match("/api/mp/v1/staff/borrows/{detailId}", uri)) {
            return borrowDetailDept(longPathVar(request, "detailId"));
        }
        if (PATH_MATCHER.match("/api/mp/v1/staff/points-records/{pointsRecordId}", uri)) {
            PointsOrder order = pointsService.selectByOrderNumber(pathVar(request, "pointsRecordId"));
            return order != null ? memberDept(order.getMemberId()) : null;
        }
        Integer memberId = intParam(request, "memberId");
        return memberId != null ? memberDept(memberId) : null;
    }

    private Long borrowDetailDept(Long detailId) {
        if (detailId == null) {
            return null;
        }
        BookBorrowDetail detail = bookBorrowService.selectDetailById(detailId);
        if (detail == null) {
            return null;
        }
        BookBorrowOrder order = bookBorrowService.selectOrderByNo(detail.getBorrowOrderNo());
        if (order != null && order.getDeptId() != null) {
            return order.getDeptId();
        }
        return memberDept(detail.getMemberId());
    }

    private Long memberDept(Integer memberId) {
        if (memberId == null) {
            return null;
        }
        Member member = memberService.selectMemberById(memberId);
        return member != null ? member.getDeptId() : null;
    }

    private Set<Long> visibleDeptIds(SysUser staff) {
        Set<Long> result = new LinkedHashSet<>();
        if (staff.isAdmin()) {
            return allNormalDeptIds();
        }
        List<SysRole> roles = staff.getRoles();
        if (roles != null) {
            for (SysRole role : roles) {
                if (role == null || !"0".equals(role.getStatus())) {
                    continue;
                }
                String scope = role.getDataScope();
                if (Constants.Dept.DATA_SCOPE_ALL.equals(scope)) {
                    return allNormalDeptIds();
                }
                if (Constants.Dept.DATA_SCOPE_CUSTOM.equals(scope)) {
                    result.addAll(sysDeptMapper.selectDeptListByRoleId(role.getRoleId(), role.isDeptCheckStrictly()));
                } else if (Constants.Dept.DATA_SCOPE_DEPT.equals(scope) || Constants.Dept.DATA_SCOPE_SELF.equals(scope)) {
                    addDept(result, staff.getDeptId());
                } else if (Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD.equals(scope)) {
                    addDeptAndChildren(result, staff.getDeptId());
                }
            }
        }
        if (result.isEmpty()) {
            addDept(result, staff.getDeptId());
        }
        return result;
    }

    private Set<Long> allNormalDeptIds() {
        SysDept query = new SysDept();
        query.setStatus("0");
        List<SysDept> depts = sysDeptMapper.selectDeptList(query);
        Set<Long> result = new LinkedHashSet<>();
        for (SysDept dept : depts) {
            addDept(result, dept.getDeptId());
        }
        return result;
    }

    private void addDeptAndChildren(Set<Long> result, Long deptId) {
        addDept(result, deptId);
        if (deptId == null) {
            return;
        }
        List<SysDept> children = sysDeptMapper.selectChildrenDeptById(deptId);
        for (SysDept child : children) {
            if ("0".equals(child.getStatus())) {
                addDept(result, child.getDeptId());
            }
        }
    }

    private void addDept(Collection<Long> result, Long deptId) {
        if (deptId != null && deptId > 0) {
            result.add(deptId);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> pathVariables(HttpServletRequest request) {
        Object value = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return value instanceof Map ? (Map<String, String>) value : Map.of();
    }

    private String pathVar(HttpServletRequest request, String name) {
        return pathVariables(request).get(name);
    }

    private Integer intPathVar(HttpServletRequest request, String name) {
        return intValue(pathVar(request, name));
    }

    private Long longPathVar(HttpServletRequest request, String name) {
        return longValue(pathVar(request, name));
    }

    private Integer intParam(HttpServletRequest request, String name) {
        return intValue(request.getParameter(name));
    }

    private boolean boolAttr(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    private Integer intAttr(HttpServletRequest request, String name) {
        return intValue(request.getAttribute(name));
    }

    private Long longAttr(HttpServletRequest request, String name) {
        return longValue(request.getAttribute(name));
    }

    private Integer intValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse response, int httpStatus, int code, String message) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(ApiResponse.error(code, message)));
    }
}
