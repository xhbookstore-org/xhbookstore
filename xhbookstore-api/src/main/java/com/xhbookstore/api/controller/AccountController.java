package com.xhbookstore.api.controller;

import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.common.core.domain.entity.SysUser;
import com.xhbookstore.system.domain.book.BookBorrowDetail;
import com.xhbookstore.system.domain.book.BookBorrowOrder;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.mapper.SysUserMapper;
import com.xhbookstore.system.service.book.IBookBorrowService;
import com.xhbookstore.system.service.member.IMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "账号管理", description = "注销资格查询、注销账号")
@RestController
@RequestMapping("/api/mp/v1/account")
public class AccountController {
    @Autowired
    private IMemberService memberService;
    @Autowired
    private IBookBorrowService bookBorrowService;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Operation(summary = "查询注销前置状态", description = "检查当前会员是否可以注销，返回真实积分、未还书和员工身份状态")
    @GetMapping("/cancel-eligibility")
    public ApiResponse<Map<String, Object>> cancelEligibility(HttpServletRequest request) {
        Integer memberId = currentMemberId(request);
        Member member = requireActiveMember(memberId);
        boolean hasUnreturnedBooks = hasUnreturnedBooks(memberId);
        boolean isStaffActive = isStaffActive(member.getPhone());

        Map<String, Object> data = new HashMap<>();
        data.put("canCancel", !hasUnreturnedBooks && !isStaffActive);
        data.put("blockedReason", blockedReason(hasUnreturnedBooks, isStaffActive));
        data.put("hasUnreturnedBooks", hasUnreturnedBooks);
        data.put("isStaffActive", isStaffActive);
        data.put("currentPoints", member.getCurrentPoints() != null ? member.getCurrentPoints() : 0);
        return ApiResponse.success(data);
    }

    @Operation(summary = "注销账号", description = "执行账号注销。入参 JSON: reason(注销原因), confirmRead(是否确认)")
    @PostMapping("/cancel")
    public ApiResponse<Map<String, Object>> cancel(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Integer memberId = currentMemberId(request);
        Member member = requireActiveMember(memberId);
        if (!confirmed(body.get("confirmRead"))) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "请先确认注销须知");
        }
        if (hasUnreturnedBooks(memberId)) {
            throw new ApiException(ApiErrorCode.ACCOUNT_HAS_UNRETURNED, "存在未还清图书，无法注销");
        }
        if (isStaffActive(member.getPhone())) {
            throw new ApiException(ApiErrorCode.ACCOUNT_STAFF_ACTIVE, "员工身份仍有效，无法注销");
        }

        memberService.deleteMember(memberId);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("memberId", memberId);
        data.put("cancelledAt", System.currentTimeMillis());
        return ApiResponse.success(data);
    }

    private Integer currentMemberId(HttpServletRequest request) {
        Object memberIdObj = request.getAttribute("memberId");
        if (memberIdObj == null) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND, "仅会员可访问");
        }
        return Integer.valueOf(memberIdObj.toString());
    }

    private Member requireActiveMember(Integer memberId) {
        Member member = memberService.selectMemberById(memberId);
        if (member == null || member.getStatus() == null || member.getStatus() != 0) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND, "会员账号已注销或不存在");
        }
        return member;
    }

    private boolean hasUnreturnedBooks(Integer memberId) {
        List<BookBorrowOrder> orders = bookBorrowService.selectByMemberId(memberId);
        if (orders == null) {
            return false;
        }
        for (BookBorrowOrder order : orders) {
            List<BookBorrowDetail> details = bookBorrowService.selectDetailsByOrderId(order.getId());
            if (details == null) {
                continue;
            }
            for (BookBorrowDetail detail : details) {
                int borrowQty = detail.getBorrowQty() != null ? detail.getBorrowQty() : 0;
                int returnedQty = detail.getReturnedQty() != null ? detail.getReturnedQty() : 0;
                int purchaseQty = detail.getPurchaseQty() != null ? detail.getPurchaseQty() : 0;
                if (borrowQty - returnedQty - purchaseQty > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean confirmed(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && "true".equalsIgnoreCase(value.toString());
    }

    private boolean isStaffActive(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        SysUser staff = sysUserMapper.selectUserByPhonenumber(phone);
        return staff != null;
    }

    private String blockedReason(boolean hasUnreturnedBooks, boolean isStaffActive) {
        if (hasUnreturnedBooks) {
            return "存在未还清图书，暂不能注销";
        }
        if (isStaffActive) {
            return "员工身份仍有效，无法注销";
        }
        return null;
    }
}
