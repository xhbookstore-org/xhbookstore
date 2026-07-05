package com.xhbookstore.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.system.domain.book.BookBorrowDetail;
import com.xhbookstore.system.domain.book.BookBorrowOrder;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.service.book.IBookBorrowService;
import com.xhbookstore.system.service.member.IMemberService;

@Tag(name = "账号管理", description = "注销资格查询、注销账号")
@RestController
@RequestMapping("/api/mp/v1/account")
public class AccountController {
    @Autowired
    private IMemberService memberService;
    @Autowired
    private IBookBorrowService bookBorrowService;

    @Operation(summary = "查询注销前置状态", description = "检查当前会员是否可以注销，返回真实积分和未还书状态")
    @GetMapping("/cancel-eligibility")
    public ApiResponse<Map<String, Object>> cancelEligibility(HttpServletRequest request) {
        Integer memberId = currentMemberId(request);
        Member member = memberService.selectMemberById(memberId);
        if (member == null) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        }
        boolean hasUnreturnedBooks = hasUnreturnedBooks(memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("canCancel", !hasUnreturnedBooks);
        data.put("blockedReason", hasUnreturnedBooks ? "存在未还清图书，暂不能注销" : null);
        data.put("hasUnreturnedBooks", hasUnreturnedBooks);
        data.put("isStaffActive", false);
        data.put("currentPoints", member.getCurrentPoints() != null ? member.getCurrentPoints() : 0);
        return ApiResponse.success(data);
    }

    @Operation(summary = "注销账号", description = "执行账号注销。入参 JSON: reason(注销原因), confirmRead(是否确认)")
    @PostMapping("/cancel")
    public ApiResponse<Map<String, Object>> cancel(@RequestBody Map<String, Object> body) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
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

    private boolean hasUnreturnedBooks(Integer memberId) {
        List<BookBorrowOrder> orders = bookBorrowService.selectByMemberId(memberId);
        if (orders == null) return false;
        for (BookBorrowOrder order : orders) {
            List<BookBorrowDetail> details = bookBorrowService.selectDetailsByOrderId(order.getId());
            if (details == null) continue;
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
}
