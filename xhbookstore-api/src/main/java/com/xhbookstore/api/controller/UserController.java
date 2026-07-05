package com.xhbookstore.api.controller;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.model.PageResult;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.domain.book.*;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.system.service.member.IMemberCardService;
import com.xhbookstore.system.service.member.IMemberCodeTokenService;
import com.xhbookstore.system.service.member.IPointsService;
import com.xhbookstore.system.service.book.IBookBorrowService;

/**
 * 用户端接口 - 文档 §11
 */
@Tag(name = "用户端接口", description = "用户首页、会员码、借阅记录、积分记录")
@RestController
@RequestMapping("/api/mp/v1/user")
public class UserController {

    @Autowired
    private IMemberService memberService;
    @Autowired
    private IMemberCardService memberCardService;
    @Autowired
    private IMemberCodeTokenService memberCodeTokenService;
    @Autowired
    private IPointsService pointsService;
    @Autowired
    private IBookBorrowService bookBorrowService;

    /**
     * 查询用户首页 §11.1 — 从Token获取手机号，查会员真实数据
     */
    @Operation(summary = "查询用户首页", description = "从Token获取手机号和memberId，查member表返回会员卡/积分/借阅概况")
    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home(HttpServletRequest request) {
        // 从JWT Token中获取手机号
        String phone = (String) request.getAttribute("phone");
        if (phone == null) phone = "";
        String phoneDisplay = phone.length() >= 7
                ? phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4)
                : phone;

        // 从JWT Token中获取memberId，查真实会员数据
        Object memberIdObj = request.getAttribute("memberId");
        Integer memberId = memberIdObj != null ? Integer.valueOf(memberIdObj.toString()) : null;

        Map<String, Object> data = new HashMap<>();
        data.put("phoneDisplay", phoneDisplay);

        Map<String, Object> memberMap = new HashMap<>();
        if (memberId != null) {
            Member member = memberService.selectMemberById(memberId);
            if (member != null) {
                // 会员卡信息
                Map<String, Object> card = new HashMap<>();
                card.put("cardTypeId", member.getCardTypeId());
                card.put("cardTypeName", member.getCardTypeName());
                card.put("memberNo", member.getCardNo());
                card.put("cardStatus", member.getStatus() != null && member.getStatus() == 0 ? "active" : "inactive");
                card.put("level", member.getLevelId());
                card.put("remainingDays", member.getValidDate() != null
                        ? Math.max(0, (member.getValidDate().getTime() - System.currentTimeMillis()) / 86400000L)
                        : 0);
                card.put("effectiveAt", member.getCreatedAt() != null ? member.getCreatedAt().getTime() : null);
                card.put("expiredAt", member.getValidDate() != null ? member.getValidDate().getTime() : null);

                memberMap.put("memberId", member.getId());
                memberMap.put("memberNo", member.getCardNo());
                memberMap.put("memberName", member.getName());
                memberMap.put("phoneDisplay", phoneDisplay);
                memberMap.put("card", card);
                memberMap.put("currentPoints", member.getCurrentPoints() != null ? member.getCurrentPoints() : 0);
                memberMap.put("currentBorrowingCount", 0); // TODO: 真实统计
                memberMap.put("yearBorrowCount", 0);        // TODO: 真实统计
            }
        } else {
            memberMap.put("memberId", null);
            memberMap.put("memberNo", "");
            memberMap.put("memberName", "");
            memberMap.put("phoneDisplay", phoneDisplay);
            memberMap.put("card", null);
            memberMap.put("currentPoints", 0);
            memberMap.put("currentBorrowingCount", 0);
            memberMap.put("yearBorrowCount", 0);
        }
        data.put("member", memberMap);
        return ApiResponse.success(data);
    }

    /**
     * 生成动态会员码 §11.2 — 根据当前登录会员生成真实二维码内容
     */
    @Operation(summary = "生成动态会员码", description = "根据当前登录会员的真实card_no生成二维码内容，30秒过期")
    @PostMapping("/member-code")
    public ApiResponse<Map<String, Object>> generateMemberCode(HttpServletRequest request) {
        // 从Token获取memberId
        Object memberIdObj = request.getAttribute("memberId");
        if (memberIdObj == null) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.MEMBER_NOT_FOUND,
                    "仅会员可生成会员码");
        }
        Integer memberId = Integer.valueOf(memberIdObj.toString());
        Member member = memberService.selectMemberById(memberId);
        if (member == null || member.getCardNo() == null) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.MEMBER_NOT_FOUND);
        }

        com.xhbookstore.system.domain.member.MemberCodeTokenInfo tokenInfo =
                memberCodeTokenService.createToken(memberId, "BUY_CARD", 60);
        String memberNo = member.getCardNo();
        Map<String, Object> data = new HashMap<>();
        data.put("memberNo", memberNo);
        data.put("memberCodeToken", tokenInfo.getToken());
        data.put("codeContent", "MCODE:" + tokenInfo.getToken());
        data.put("expiresAt", tokenInfo.getExpiresAt());
        data.put("ttlSeconds", 60);
        return ApiResponse.success(data);
    }

    @Operation(summary = "My member cards")
    @GetMapping("/member-cards")
    public ApiResponse<Map<String, Object>> myMemberCards(HttpServletRequest request) {
        Object memberIdObj = request.getAttribute("memberId");
        if (memberIdObj == null) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.MEMBER_NOT_FOUND);
        }
        Integer memberId = Integer.valueOf(memberIdObj.toString());
        return ApiResponse.success(memberCardService.getMemberCardView(memberId));
    }

    /**
     * 查询本人借阅记录 §11.3
     */
    @Operation(summary = "查询本人借阅记录", description = "分页查询当前会员的借阅订单及明细")
    @GetMapping("/borrows")
    public ApiResponse<Map<String, Object>> myBorrows(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        // TODO: 从Token获取memberId，当前用固定值
        Integer memberId = 1;
        List<BookBorrowOrder> orders = bookBorrowService.selectByMemberId(memberId);
        List<Map<String, Object>> records = new ArrayList<>();
        for (BookBorrowOrder o : orders) {
            Map<String, Object> r = new HashMap<>();
            r.put("orderNo", o.getOrderNo());
            r.put("totalBookCount", o.getTotalBookCount());
            r.put("borrowStatus", o.getBorrowStatus());
            r.put("borrowTime", o.getBorrowTime() != null ? o.getBorrowTime().getTime() : null);
            List<BookBorrowDetail> details = bookBorrowService.selectDetailsByOrderId(o.getId());
            r.put("details", details);
            records.add(r);
        }

        String phone = (String) request.getAttribute("phone");
        Map<String, Object> data = new HashMap<>();
        data.put("memberDisplay", maskPhone(phone));
        data.put("yearBorrowCount", orders.size());
        data.put("currentBorrowingCount", orders.stream().filter(o -> o.getBorrowStatus() != null && o.getBorrowStatus() != 2 && o.getBorrowStatus() != 5).count());
        data.put("page", new PageResult<>(records, pageNo, pageSize, records.size()));
        return ApiResponse.success(data);
    }

    /**
     * 查询借阅详情 §11.4
     */
    @Operation(summary = "查询借阅详情", description = "按借阅单号查询完整订单+明细+还书记录")
    @GetMapping("/borrows/{borrowId}")
    public ApiResponse<Map<String, Object>> borrowDetail(@PathVariable String borrowId) {
        BookBorrowOrder order = bookBorrowService.selectOrderByNo(borrowId);
        if (order == null) return ApiResponse.success(new HashMap<>());
        List<BookBorrowDetail> details = bookBorrowService.selectDetailsByOrderId(order.getId());
        List<BookReturnDetail> returns = bookBorrowService.selectReturnsByOrderId(order.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("details", details);
        data.put("returns", returns);
        return ApiResponse.success(data);
    }

    /**
     * 查询本人积分记录 §11.5
     */
    @Operation(summary = "查询本人积分记录", description = "分页查询当前会员的积分增减记录（方向/变动量/操作前后值/时间）")
    @GetMapping("/points-records")
    public ApiResponse<Map<String, Object>> myPointsRecords(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        // TODO: 关联当前用户memberId
        List<PointsOrder> orders = pointsService.selectByMemberId(1);
        List<Map<String, Object>> records = new ArrayList<>();
        for (PointsOrder o : orders) {
            Map<String, Object> r = new HashMap<>();
            r.put("pointsRecordId", o.getOrderNumber());
            r.put("reasonName", o.getDescription());
            r.put("direction", o.getOrderNumber().startsWith("IN") ? "add" : "deduct");
            r.put("pointsDelta", o.getAmount());
            r.put("beforePoints", o.getOrginPoints());
            r.put("afterPoints", o.getAfterPoints());
            r.put("operatedAt", o.getCreatedAt().getTime());
            records.add(r);
        }
        String phone2 = (String) request.getAttribute("phone");
        Map<String, Object> data = new HashMap<>();
        data.put("memberDisplay", maskPhone(phone2));
        data.put("currentPoints", 100);
        data.put("yearEarnedPoints", 50);
        data.put("page", new PageResult<>(records, pageNo, pageSize, records.size()));
        return ApiResponse.success(data);
    }

    /**
     * 查询积分详情 §11.6
     */
    @Operation(summary = "查询积分详情", description = "按积分记录ID查询单条积分详情")
    @GetMapping("/points-records/{pointsRecordId}")
    public ApiResponse<Map<String, Object>> pointsDetail(@PathVariable String pointsRecordId) {
        Map<String, Object> data = new HashMap<>();
        data.put("pointsRecordId", pointsRecordId);
        return ApiResponse.success(data);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone != null ? phone : "";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
