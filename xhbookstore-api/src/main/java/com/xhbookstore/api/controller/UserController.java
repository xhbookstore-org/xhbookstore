package com.xhbookstore.api.controller;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.model.PageResult;
import com.xhbookstore.system.domain.member.MemberCard;
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
    private static final int MEMBER_CODE_TOKEN_TTL_SECONDS = 3600;

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
                memberMap.put("memberId", member.getId());
                memberMap.put("memberNo", member.getCardNo());
                memberMap.put("memberName", member.getName());
                memberMap.put("phoneDisplay", phoneDisplay);
                memberMap.put("card", buildActiveMemberCard(member));
                memberMap.put("currentPoints", member.getCurrentPoints() != null ? member.getCurrentPoints() : 0);
                Map<String, Integer> borrowStats = borrowStats(member.getId());
                memberMap.put("currentBorrowingCount", borrowStats.get("currentBorrowingCount"));
                memberMap.put("yearBorrowCount", borrowStats.get("yearBorrowCount"));
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
                memberCodeTokenService.createToken(memberId, "BUY_CARD", MEMBER_CODE_TOKEN_TTL_SECONDS);
        String memberNo = member.getCardNo();
        Map<String, Object> data = new HashMap<>();
        data.put("memberNo", memberNo);
        data.put("memberCodeToken", tokenInfo.getToken());
        data.put("codeContent", "MCODE:" + tokenInfo.getToken());
        data.put("expiresAt", tokenInfo.getExpiresAt());
        data.put("ttlSeconds", MEMBER_CODE_TOKEN_TTL_SECONDS);
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
        return ApiResponse.success(filterVisibleMemberCards(memberCardService.getMemberCardView(memberId)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> filterVisibleMemberCards(Map<String, Object> cardView) {
        if (cardView == null) {
            return Collections.emptyMap();
        }
        Object cardsObj = cardView.get("cards");
        if (!(cardsObj instanceof List<?> cards)) {
            return cardView;
        }
        List<MemberCard> visibleCards = new ArrayList<>();
        for (Object item : cards) {
            if (!(item instanceof MemberCard card)) {
                continue;
            }
            Integer status = card.getStatus();
            if (status != null && (status == 0 || status == 1)) {
                visibleCards.add(card);
            }
        }
        Map<String, Object> filtered = new HashMap<>(cardView);
        filtered.put("cards", visibleCards);
        MemberCard activeCard = null;
        for (MemberCard card : visibleCards) {
            if (card.getStatus() != null && card.getStatus() == 1) {
                activeCard = card;
                break;
            }
        }
        filtered.put("activeCard", activeCard);
        filtered.put("hasActiveCard", activeCard != null);
        return filtered;
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
        validatePage(pageNo, pageSize);
        Integer memberId = currentMemberId(request);
        int offset = (pageNo - 1) * pageSize;
        List<Map<String, Object>> records = bookBorrowService.selectBorrowDetailPage(
                null, null, memberId, false, offset, pageSize);
        long total = bookBorrowService.countBorrowDetailPage(null, null, memberId, false);
        int currentBorrowingCount = bookBorrowService.sumRemainingByMemberId(memberId);
        int borrowOrderCount = bookBorrowService.countBorrowOrdersByMemberId(memberId);

        String phone = (String) request.getAttribute("phone");
        Map<String, Object> data = new HashMap<>();
        data.put("memberDisplay", maskPhone(phone));
        data.put("yearBorrowCount", borrowOrderCount);
        data.put("currentBorrowingCount", currentBorrowingCount);
        data.put("page", new PageResult<>(records, pageNo, pageSize, total));
        return ApiResponse.success(data);
    }

    /**
     * 查询借阅详情 §11.4
     */
    @Operation(summary = "查询借阅详情", description = "按借阅单号查询完整订单+明细+还书记录")
    @GetMapping("/borrows/{detailId}")
    public ApiResponse<Map<String, Object>> borrowDetail(@PathVariable String detailId, HttpServletRequest request) {
        Integer memberId = currentMemberId(request);
        BookBorrowDetail detail = bookBorrowService.selectDetailById(parseLong(detailId, "detailId"));
        if (detail == null) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.NOT_FOUND,
                    "Borrow detail not found");
        }
        if (detail.getMemberId() == null || !detail.getMemberId().equals(memberId)) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.FORBIDDEN,
                    "No permission to view this borrow detail");
        }
        BookBorrowOrder order = bookBorrowService.selectOrderByNo(detail.getBorrowOrderNo());
        List<BookReturnDetail> returns = order != null ? bookBorrowService.selectReturnsByOrderId(order.getId()) : Collections.emptyList();
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("detail", detail);
        data.put("item", buildBorrowItem(order, detail));
        data.put("returns", filterReturns(returns, detail.getId()));
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
        validatePage(pageNo, pageSize);
        Integer memberId = currentMemberId(request);
        Member member = memberService.selectMemberById(memberId);
        int offset = (pageNo - 1) * pageSize;
        List<PointsOrder> orders = pointsService.selectPage(null, memberId, null, offset, pageSize);
        long total = pointsService.countPage(null, memberId, null);
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
        data.put("currentPoints", member != null && member.getCurrentPoints() != null ? member.getCurrentPoints() : 0);
        data.put("yearEarnedPoints", pointsService.sumYearEarned(memberId));
        data.put("page", new PageResult<>(records, pageNo, pageSize, total));
        return ApiResponse.success(data);
    }

    /**
     * 查询积分详情 §11.6
     */
    @Operation(summary = "查询积分详情", description = "按积分记录ID查询单条积分详情")
    @GetMapping("/points-records/{pointsRecordId}")
    public ApiResponse<Map<String, Object>> pointsDetail(@PathVariable String pointsRecordId, HttpServletRequest request) {
        Integer memberId = currentMemberId(request);
        PointsOrder order = pointsService.selectByOrderNumber(pointsRecordId);
        if (order == null || order.getIsDel() != null && order.getIsDel() != 0) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.NOT_FOUND,
                    "Points record not found");
        }
        if (order.getMemberId() == null || !order.getMemberId().equals(memberId)) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.FORBIDDEN,
                    "No permission to view this points record");
        }
        return ApiResponse.success(buildPointsItem(order));
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone != null ? phone : "";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private Integer currentMemberId(HttpServletRequest request) {
        Object memberIdObj = request.getAttribute("memberId");
        if (memberIdObj == null) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.MEMBER_NOT_FOUND,
                    "仅会员可访问");
        }
        return Integer.valueOf(memberIdObj.toString());
    }

    private void validatePage(int pageNo, int pageSize) {
        if (pageNo < 1) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.PARAM_INVALID, "pageNo must be greater than 0");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.PARAM_INVALID, "pageSize must be between 1 and 100");
        }
    }

    private Map<String, Object> buildBorrowItem(BookBorrowOrder o, BookBorrowDetail d) {
        Map<String, Object> item = new HashMap<>();
        item.put("detailId", d.getId());
        item.put("borrowDetailId", d.getId());
        item.put("orderNo", d.getBorrowOrderNo());
        item.put("memberId", d.getMemberId());
        item.put("bookId", d.getBookId());
        item.put("bookName", d.getBookName());
        item.put("borrowStatus", d.getBorrowStatus() != null ? d.getBorrowStatus() : 0);
        item.put("borrowQty", d.getBorrowQty() != null ? d.getBorrowQty() : 0);
        item.put("returnedQty", d.getReturnedQty() != null ? d.getReturnedQty() : 0);
        item.put("purchaseQty", d.getPurchaseQty() != null ? d.getPurchaseQty() : 0);
        item.put("remainingQty", remainingQty(d));
        item.put("purchaseOrderNo", d.getPurchaseOrderNo());
        item.put("borrowTime", timeMillis(d.getBorrowTime()));
        item.put("returnAllTime", d.getReturnAllTime() != null ? timeMillis(d.getReturnAllTime())
                : (o != null ? timeMillis(o.getReturnAllTime()) : null));
        item.put("expectedReturnTime", o != null ? timeMillis(o.getExpectedReturnTime()) : null);
        item.put("remark", d.getRemark() != null ? d.getRemark() : (o != null ? o.getRemark() : null));
        return item;
    }

    private List<BookReturnDetail> filterReturns(List<BookReturnDetail> returns, Long detailId) {
        List<BookReturnDetail> list = new ArrayList<>();
        if (returns == null) return list;
        for (BookReturnDetail r : returns) {
            if (r.getBorrowDetailId() != null && r.getBorrowDetailId().equals(detailId)) {
                list.add(r);
            }
        }
        return list;
    }

    private Map<String, Integer> borrowStats(Integer memberId) {
        Map<String, Integer> stats = new HashMap<>();
        int currentBorrowingCount = 0;
        int yearBorrowCount = 0;
        List<BookBorrowOrder> orders = bookBorrowService.selectByMemberId(memberId);
        Calendar now = Calendar.getInstance();
        if (orders != null) {
            for (BookBorrowOrder order : orders) {
                List<BookBorrowDetail> details = bookBorrowService.selectDetailsByOrderId(order.getId());
                if (details == null) continue;
                for (BookBorrowDetail detail : details) {
                    currentBorrowingCount += Math.max(0, remainingQty(detail));
                    if (detail.getBorrowTime() != null) {
                        Calendar created = Calendar.getInstance();
                        created.setTime(detail.getBorrowTime());
                        if (created.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                            yearBorrowCount += detail.getBorrowQty() != null ? detail.getBorrowQty() : 0;
                        }
                    }
                }
            }
        }
        stats.put("currentBorrowingCount", currentBorrowingCount);
        stats.put("yearBorrowCount", yearBorrowCount);
        return stats;
    }

    private int remainingQty(BookBorrowDetail d) {
        int b = d.getBorrowQty() != null ? d.getBorrowQty() : 0;
        int r = d.getReturnedQty() != null ? d.getReturnedQty() : 0;
        int p = d.getPurchaseQty() != null ? d.getPurchaseQty() : 0;
        return b - r - p;
    }

    private Map<String, Object> buildActiveMemberCard(Member member) {
        Map<String, Object> view = memberCardService.getMemberCardView(member.getId());
        Object active = view.get("activeCard");
        if (!(active instanceof MemberCard card)) {
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("cardTypeId", card.getCardTypeId());
        data.put("cardTypeName", card.getCardTypeName());
        data.put("memberNo", member.getCardNo());
        data.put("cardStatus", "active");
        data.put("level", member.getLevelId());
        data.put("remainingDays", card.getExpiredAt() != null
                ? Math.max(0, (card.getExpiredAt().getTime() - System.currentTimeMillis()) / 86400000L)
                : 0);
        data.put("effectiveAt", card.getEffectiveAt() != null ? card.getEffectiveAt().getTime() : null);
        data.put("expiredAt", card.getExpiredAt() != null ? card.getExpiredAt().getTime() : null);
        return data;
    }

    private Long parseLong(String value, String fieldName) {
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            throw new com.xhbookstore.api.exception.ApiException(
                    com.xhbookstore.api.constant.ApiErrorCode.PARAM_INVALID,
                    fieldName + " must be a number");
        }
    }

    private Map<String, Object> buildPointsItem(PointsOrder o) {
        Map<String, Object> r = new HashMap<>();
        r.put("pointsRecordId", o.getOrderNumber());
        r.put("memberId", o.getMemberId());
        r.put("reasonName", o.getDescription());
        r.put("direction", o.getOrderNumber() != null && o.getOrderNumber().startsWith("IN") ? "add" : "deduct");
        r.put("pointsDelta", o.getAmount());
        r.put("beforePoints", o.getOrginPoints());
        r.put("afterPoints", o.getAfterPoints());
        r.put("operatedAt", o.getCreatedAt() != null ? o.getCreatedAt().getTime() : null);
        r.put("operationDevice", o.getOperationDevice());
        return r;
    }

    private Long timeMillis(Date date) {
        return date != null ? Long.valueOf(date.getTime()) : null;
    }
}
