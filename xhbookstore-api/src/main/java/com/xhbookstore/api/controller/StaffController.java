package com.xhbookstore.api.controller;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.constant.ApiErrorCode;
import com.xhbookstore.api.exception.ApiException;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.model.PageResult;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberCardLog;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.domain.book.*;
import com.xhbookstore.system.mapper.SysUserMapper;
import com.xhbookstore.system.mapper.member.MemberCardLogMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.member.ICardTypeService;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.system.service.member.IPointsService;
import com.xhbookstore.system.service.book.IBookBorrowService;

/**
 * 员工端接口 - 文档 §12
 */
@RestController
@RequestMapping("/api/mp/v1/staff")
public class StaffController {

    @Autowired
    private IMemberService memberService;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private IPointsService pointsService;
    @Autowired
    private IBookBorrowService bookBorrowService;
    @Autowired
    private MemberCardLogMapper memberCardLogMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private ICardTypeService cardTypeService;

    /**
     * 查询员工首页 §12.1
     */
    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("storeName", "新华书店总店");
        data.put("todayStoreBorrowCount", 12);
        data.put("todayStaffBorrowCount", 3);
        return ApiResponse.success(data);
    }

    /**
     * 查询会员类型列表 — 数据源: util_card_type
     */
    @GetMapping("/card-types")
    public ApiResponse<Map<String, Object>> cardTypes() {
        List<CardType> list = cardTypeService.selectAll();
        List<Map<String, Object>> items = new ArrayList<>();
        for (CardType ct : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", ct.getId());
            item.put("typeName", ct.getTypeName());
            item.put("price", ct.getPrice());
            item.put("validDays", ct.getValidDays());
            item.put("borrowLimit", ct.getBorrowLimit());
            item.put("discount", ct.getDiscount());
            item.put("isRenewal", ct.getIsRenewal() != null && ct.getIsRenewal() == 1);
            item.put("description", ct.getDescription());
            item.put("sort", ct.getSort());
            item.put("status", ct.getStatus());
            items.add(item);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", items);
        return ApiResponse.success(data);
    }

    /**
     * 开通/续费会员卡 — 参数 cardTypeId，数据源 util_card_type
     */
    @PostMapping("/members/{memberId}/activate-card")
    public ApiResponse<Map<String, Object>> activateCard(
            @PathVariable String memberId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        Integer cardTypeId = body.get("cardTypeId") != null ? Integer.valueOf(body.get("cardTypeId").toString()) : null;
        if (cardTypeId == null) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "请选择会员类型");
        }
        String memberName = (String) body.get("name");
        if (memberName == null || memberName.trim().isEmpty()) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "请填写会员姓名");
        }
        String remark = (String) body.get("remark");

        // 1. 查卡类型
        CardType cardType = cardTypeService.selectById(cardTypeId);
        if (cardType == null || cardType.getStatus() != null && cardType.getStatus() != 0) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "无效的会员类型");
        }
        if (cardType.getIsDel() != null && cardType.getIsDel() == 1) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "该会员类型已删除");
        }

        // 2. 业务参数直接从表字段获取
        String cardTypeName = cardType.getTypeName();
        int validDays = cardType.getValidDays();
        boolean isRenewal = cardType.getIsRenewal() != null && cardType.getIsRenewal() == 1;

        // 3. 悲观锁查会员
        Member member = memberMapper.selectMemberByIdForUpdate(Integer.parseInt(memberId));
        if (member == null) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        }

        // 4. 记录变更前
        String beforeCardType = member.getCardTypeName();
        java.util.Date beforeValidDate = member.getValidDate();

        // 5. 计算到期日（不再依赖 card_type 表）
        java.util.Date newValidDate;
        String operationType;
        java.util.Calendar cal = java.util.Calendar.getInstance();

        if (isRenewal) {
            if (member.getValidDate() == null || member.getValidDate().before(new java.util.Date())) {
                throw new ApiException(ApiErrorCode.PARAM_INVALID,
                        "会员卡已过期（到期日：" + (member.getValidDate() != null ? member.getValidDate().toString() : "无") +
                        "），请选择「年卡会员」或「半年卡会员」开通新卡");
            }
            cal.setTime(member.getValidDate());
            cal.add(java.util.Calendar.DAY_OF_YEAR, validDays);
            operationType = "RENEW";
        } else {
            cal.setTime(new java.util.Date());
            cal.add(java.util.Calendar.DAY_OF_YEAR, validDays);
            operationType = "ACTIVATE";
        }
        newValidDate = cal.getTime();

        // 6. 更新会员（含姓名）
        member.setName(memberName);
        member.setValidDate(newValidDate);
        member.setStatus(0);
        member.setLastOperator(getOperatorFromRequest(request));
        memberMapper.updateMember(member);

        // 7. 查操作人真实姓名
        Object staffIdAttr = request.getAttribute("staffUserId");
        String operatorId = staffIdAttr != null ? String.valueOf(staffIdAttr) : "system";
        String operatorName = operatorId;
        if (staffIdAttr != null) {
            com.xhbookstore.common.core.domain.entity.SysUser staffUser = sysUserMapper.selectUserById(Long.valueOf(operatorId));
            if (staffUser != null) operatorName = staffUser.getNickName();
        }

        // 8. 写日志
        MemberCardLog log = new MemberCardLog();
        log.setMemberId(member.getId());
        log.setMemberNo(member.getCardNo());
        log.setOperationType(operationType);
        log.setBeforeCardType(beforeCardType);
        log.setBeforeValidDate(beforeValidDate instanceof java.sql.Date ? null : beforeValidDate);
        log.setAfterCardType(cardTypeName);
        log.setAfterValidDate(newValidDate);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setDevice("小程序");
        log.setRemark(remark);
        memberCardLogMapper.insert(log);

        // 8. 返回
        long remainingDays = (newValidDate.getTime() - System.currentTimeMillis()) / 86400000L;
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("memberId", member.getId());
        data.put("memberNo", member.getCardNo());
        data.put("cardTypeId", cardTypeId);
        data.put("cardTypeName", cardTypeName);
        data.put("operationType", operationType);
        data.put("validDate", newValidDate);
        data.put("remainingDays", Math.max(0, remainingDays));
        data.put("cardStatus", "active");
        return ApiResponse.success(data);
    }

    private String getOperatorFromRequest(HttpServletRequest request) {
        Object attr = request.getAttribute("staffUserId");
        return attr != null ? String.valueOf(attr) : "system";
    }

    /**
     * 解析会员码 §12.2 — 格式: MEMBER:{cardNo}:TIMESTAMP:{ts}
     */
    @PostMapping("/member-code/scan")
    public ApiResponse<Map<String, Object>> scanMemberCode(@RequestBody Map<String, String> body) {
        String scanResult = body.get("scanResult");
        if (scanResult == null || scanResult.isEmpty()) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "缺少扫码内容");
        }

        // 解析格式: MEMBER:65000000001:TIMESTAMP:1782921744290
        String[] parts = scanResult.split(":");
        if (parts.length < 4 || !"MEMBER".equals(parts[0]) || !"TIMESTAMP".equals(parts[2])) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "无效的会员码格式");
        }

        String memberNo = parts[1];
        long codeTimestamp;
        try {
            codeTimestamp = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "会员码时间戳无效");
        }

        // 验证有效期（30秒）
        long now = System.currentTimeMillis();
        if (now - codeTimestamp > 30_000) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "会员码已过期，请刷新");
        }

        // 根据卡号查会员
        Member member = memberMapper.selectMemberByCardNo(memberNo);
        if (member == null) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("memberId", String.valueOf(member.getId()));
        data.put("memberNo", member.getCardNo());
        return ApiResponse.success(data);
    }

    /**
     * 查询扫码会员概要 §12.3
     */
    @GetMapping("/members/{memberId}/overview")
    public ApiResponse<Map<String, Object>> memberOverview(@PathVariable String memberId) {
        Member member = memberService.selectMemberById(Integer.parseInt(memberId));
        if (member == null) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        }

        // 构建会员概要
        Map<String, Object> memberMap = new HashMap<>();
        memberMap.put("memberId", String.valueOf(member.getId()));
        memberMap.put("memberNo", member.getCardNo());
        memberMap.put("memberName", member.getName());
        memberMap.put("phoneDisplay", maskPhone(member.getPhone()));
        memberMap.put("currentPoints", member.getCurrentPoints());
        memberMap.put("currentBorrowingCount", 0);
        memberMap.put("yearBorrowCount", 0);

        // 构建卡信息
        Map<String, Object> card = new HashMap<>();
        card.put("memberNo", member.getCardNo());
        card.put("memberLevel", member.getLevelId() != null ? member.getLevelId() * 10 : 0);
        card.put("cardName", member.getCardTypeName());
        card.put("cardStatus", "active");
        card.put("effectiveAt", member.getCreatedAt() != null ? member.getCreatedAt().getTime() : null);
        card.put("expiredAt", member.getValidDate() != null ? member.getValidDate().getTime() : null);
        card.put("remainingDays", 30);
        memberMap.put("card", card);

        // 构建操作状态
        Map<String, Object> availability = new HashMap<>();
        availability.put("canBorrow", true);
        availability.put("borrowDisabledReason", null);
        availability.put("canReturn", true);
        availability.put("returnDisabledReason", null);
        availability.put("canAddPoints", true);
        availability.put("addPointsDisabledReason", null);
        availability.put("canDeductPoints", true);
        availability.put("deductPointsDisabledReason", null);
        availability.put("maxAddPoints", 99999);
        availability.put("maxDeductPoints", 99999);

        Map<String, Object> data = new HashMap<>();
        data.put("member", memberMap);
        data.put("availability", availability);
        return ApiResponse.success(data);
    }

    /**
     * 查询全市借阅列表 §12.4
     */
    @GetMapping("/borrows")
    public ApiResponse<Map<String, Object>> borrowsList(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        // TODO: 全市查询需跨member，暂返回空
        Map<String, Object> data = new HashMap<>();
        data.put("page", new PageResult<>(Collections.emptyList(), pageNo, pageSize, 0));
        return ApiResponse.success(data);
    }

    /**
     * 查询员工侧借阅详情 §12.5
     */
    @GetMapping("/borrows/{borrowId}")
    public ApiResponse<Map<String, Object>> borrowDetail(@PathVariable String borrowId) {
        BookBorrowOrder order = bookBorrowService.selectOrderByNo(borrowId);
        if (order == null) {
            throw new ApiException(ApiErrorCode.NOT_FOUND, "借书单不存在");
        }
        List<BookBorrowDetail> details = bookBorrowService.selectDetailsByOrderId(order.getId());
        List<BookReturnDetail> returns = bookBorrowService.selectReturnsByOrderId(order.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("details", details);
        data.put("returns", returns);
        return ApiResponse.success(data);
    }

    /**
     * 办理还书 §12.6
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/borrow-returns")
    public ApiResponse<Map<String, Object>> returnBooks(@RequestBody Map<String, Object> body,
                                                         HttpServletRequest request) {
        String borrowOrderNo = (String) body.get("borrowOrderNo");
        List<Map<String, Object>> returnItems = (List<Map<String, Object>>) body.get("returnItems");
        if (borrowOrderNo == null || borrowOrderNo.isEmpty()) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "缺少借书单号");
        }
        if (returnItems == null || returnItems.isEmpty()) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "请选择要还的图书");
        }

        Object staffUserIdAttr = request.getAttribute("staffUserId");
        String staffId = staffUserIdAttr != null ? String.valueOf(staffUserIdAttr) : "system";
        String staffName = "员工";

        com.xhbookstore.common.core.domain.AjaxResult result = bookBorrowService.returnBook(
                borrowOrderNo, returnItems, staffId, staffName, null);

        if (result.isError()) {
            throw new ApiException(ApiErrorCode.BORROW_RETURN_DENIED, (String) result.get("msg"));
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> respData = (Map<String, Object>) result.get("data");
        return ApiResponse.success(respData);
    }

    /**
     * 查询指定会员借阅记录 §12.7
     */
    @GetMapping("/members/{memberId}/borrows")
    public ApiResponse<Map<String, Object>> memberBorrows(
            @PathVariable String memberId,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        Member member = memberService.selectMemberById(Integer.parseInt(memberId));
        if (member == null) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        }

        List<BookBorrowOrder> orders = bookBorrowService.selectByMemberId(Integer.parseInt(memberId));
        List<Map<String, Object>> records = new ArrayList<>();
        for (BookBorrowOrder o : orders) {
            Map<String, Object> r = new HashMap<>();
            r.put("orderNo", o.getOrderNo());
            r.put("totalBookCount", o.getTotalBookCount());
            r.put("borrowStatus", o.getBorrowStatus());
            r.put("borrowTime", o.getBorrowTime() != null ? o.getBorrowTime().getTime() : null);
            r.put("returnAllTime", o.getReturnAllTime() != null ? o.getReturnAllTime().getTime() : null);
            r.put("remark", o.getRemark());
            List<BookBorrowDetail> details = bookBorrowService.selectDetailsByOrderId(o.getId());
            r.put("details", details);
            records.add(r);
        }

        Map<String, Object> memberMap = new HashMap<>();
        memberMap.put("memberId", String.valueOf(member.getId()));
        memberMap.put("memberNo", member.getCardNo());
        memberMap.put("memberName", member.getName());
        memberMap.put("phoneDisplay", maskPhone(member.getPhone()));
        memberMap.put("currentPoints", member.getCurrentPoints());

        Map<String, Object> data = new HashMap<>();
        data.put("member", memberMap);
        data.put("page", new PageResult<>(records, pageNo, pageSize, records.size()));
        return ApiResponse.success(data);
    }

    /**
     * 办理借阅 §12.8
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/members/{memberId}/borrows")
    public ApiResponse<Map<String, Object>> borrow(
            @PathVariable String memberId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        List<Map<String, Object>> books = (List<Map<String, Object>>) body.get("books");
        if (books == null || books.isEmpty()) {
            throw new ApiException(ApiErrorCode.BORROW_BOOK_REQUIRED);
        }

        Object staffUserIdAttr = request.getAttribute("staffUserId");
        String staffId = staffUserIdAttr != null ? String.valueOf(staffUserIdAttr) : "system";
        String staffName = "员工";
        String remark = (String) body.get("remark");
        Object imageUrlsObj = body.get("imageUrls");
        List<String> imageUrls = null;
        if (imageUrlsObj instanceof List) {
            imageUrls = ((List<?>) imageUrlsObj).stream()
                    .map(Object::toString).collect(java.util.stream.Collectors.toList());
        }

        com.xhbookstore.common.core.domain.AjaxResult result = bookBorrowService.createBorrowOrder(
                Integer.parseInt(memberId), books, remark, staffId, staffName, null, imageUrls);

        if (result.isError()) {
            throw new ApiException(ApiErrorCode.BORROW_DENIED, (String) result.get("msg"));
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> respData2 = (Map<String, Object>) result.get("data");
        return ApiResponse.success(respData2);
    }

    /**
     * 查询积分事项 §12.9
     */
    @GetMapping("/points-reasons")
    public ApiResponse<Map<String, Object>> pointsReasons(
            @RequestParam String direction,
            @RequestParam(required = false) String memberId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>();
        r1.put("reasonId", "1");
        r1.put("reasonName", "活动赠送");
        r1.put("enabled", true);
        r1.put("defaultPoints", 50);
        list.add(r1);
        Map<String, Object> r2 = new HashMap<>();
        r2.put("reasonId", "2");
        r2.put("reasonName", "借阅奖励");
        r2.put("enabled", true);
        r2.put("defaultPoints", 10);
        list.add(r2);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("maxPoints", 99999);
        return ApiResponse.success(data);
    }

    /**
     * 增加积分 §12.10 ★ 含悲观锁+事务
     */
    @PostMapping("/members/{memberId}/points/add")
    public ApiResponse<Map<String, Object>> addPoints(
            @PathVariable String memberId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String reasonId = (String) body.get("reasonId");
        Object pointsObj = body.get("points");
        String remark = (String) body.get("remark");

        if (reasonId == null || reasonId.isEmpty()) {
            throw new ApiException(ApiErrorCode.POINTS_REASON_INVALID);
        }
        if (pointsObj == null) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "缺少积分值");
        }

        int points;
        try {
            points = Integer.parseInt(pointsObj.toString());
        } catch (NumberFormatException e) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "积分值格式错误");
        }
        if (points <= 0) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "积分必须为正整数");
        }

        Object staffUserIdAttr = request.getAttribute("staffUserId");
        String operator = staffUserIdAttr != null ? String.valueOf(staffUserIdAttr) : "system";

        // 调用积分服务（内部有悲观锁+事务保证原子性）
        com.xhbookstore.common.core.domain.AjaxResult result = pointsService.addPoints(
                Integer.parseInt(memberId), points,
                remark != null ? remark : "员工操作",
                operator, "小程序");

        if (result.isError()) {
            throw new ApiException(ApiErrorCode.POINTS_OPERATION_DENIED, (String) result.get("msg"));
        }

        // 查询刚创建的订单获取操作前后积分
        String orderNumber = ((String) result.get("msg")).replaceAll(".*订单号：", "");
        List<PointsOrder> orders = pointsService.selectByMemberId(Integer.parseInt(memberId));
        PointsOrder latest = orders.isEmpty() ? null : orders.get(0);

        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("pointsRecordId", orderNumber);
        data.put("beforePoints", latest != null ? latest.getOrginPoints() : 0);
        data.put("pointsDelta", points);
        data.put("afterPoints", latest != null ? latest.getAfterPoints() : points);
        data.put("operatedAt", System.currentTimeMillis());
        return ApiResponse.success(data);
    }

    /**
     * 消耗积分 §12.11
     */
    @PostMapping("/members/{memberId}/points/deduct")
    public ApiResponse<Map<String, Object>> deductPoints(
            @PathVariable String memberId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String reasonId = (String) body.get("reasonId");
        Object pointsObj = body.get("points");
        String remark = (String) body.get("remark");

        if (reasonId == null || reasonId.isEmpty()) {
            throw new ApiException(ApiErrorCode.POINTS_REASON_INVALID);
        }
        if (pointsObj == null) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "缺少积分值");
        }

        int points;
        try {
            points = Integer.parseInt(pointsObj.toString());
        } catch (NumberFormatException e) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "积分值格式错误");
        }
        if (points <= 0) {
            throw new ApiException(ApiErrorCode.PARAM_INVALID, "积分必须为正整数");
        }

        // 校验积分是否足够
        Member member = memberService.selectMemberById(Integer.parseInt(memberId));
        if (member == null) {
            throw new ApiException(ApiErrorCode.MEMBER_NOT_FOUND);
        }
        if (member.getCurrentPoints() == null || member.getCurrentPoints() < points) {
            throw new ApiException(ApiErrorCode.POINTS_NOT_ENOUGH);
        }

        // TODO: 实现消耗积分（扣减逻辑 + 插入OUT类型订单 + 插入出账单）
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("pointsRecordId", "OT" + System.currentTimeMillis());
        data.put("beforePoints", member.getCurrentPoints());
        data.put("pointsDelta", -points);
        data.put("afterPoints", member.getCurrentPoints() - points);
        data.put("operatedAt", System.currentTimeMillis());
        return ApiResponse.success(data);
    }

    /**
     * 查询全市积分列表 §12.12
     */
    @GetMapping("/points-records")
    public ApiResponse<Map<String, Object>> pointsRecordsList(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String memberId,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        List<PointsOrder> orders;
        if (memberId != null && !memberId.isEmpty()) {
            orders = pointsService.selectByMemberId(Integer.parseInt(memberId));
        } else {
            orders = Collections.emptyList(); // TODO: 全市查询
        }

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
            r.put("staffName", o.getOperationDevice());
            records.add(r);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("page", new PageResult<>(records, pageNo, pageSize, records.size()));
        return ApiResponse.success(data);
    }

    /**
     * 查询积分详情 §12.13
     */
    @GetMapping("/points-records/{pointsRecordId}")
    public ApiResponse<Map<String, Object>> pointsDetail(@PathVariable String pointsRecordId) {
        Map<String, Object> data = new HashMap<>();
        data.put("pointsRecordId", pointsRecordId);
        return ApiResponse.success(data);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
