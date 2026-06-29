package com.xhbookstore.api.controller;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.model.ApiResponse;
import com.xhbookstore.api.model.PageResult;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.system.service.member.IPointsService;

/**
 * 用户端接口 - 文档 §11
 */
@RestController
@RequestMapping("/api/mp/v1/user")
public class UserController {

    @Autowired
    private IMemberService memberService;
    @Autowired
    private IPointsService pointsService;

    /**
     * 查询用户首页 §11.1
     */
    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home(HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("phoneDisplay", "138****0001");
        // 构建会员概要
        Map<String, Object> member = new HashMap<>();
        member.put("memberId", null);
        member.put("memberNo", "");
        member.put("memberName", "");
        member.put("phoneDisplay", "138****0001");
        member.put("card", null);
        member.put("currentPoints", 0);
        member.put("currentBorrowingCount", 0);
        member.put("yearBorrowCount", 0);
        data.put("member", member);
        return ApiResponse.success(data);
    }

    /**
     * 生成动态会员码 §11.2
     */
    @PostMapping("/member-code")
    public ApiResponse<Map<String, Object>> generateMemberCode() {
        // TODO: 根据当前用户生成动态二维码
        Map<String, Object> data = new HashMap<>();
        data.put("memberNo", "65000000001");
        data.put("codeId", UUID.randomUUID().toString());
        data.put("codeContent", "MEMBER:65000000001:TIMESTAMP:" + System.currentTimeMillis());
        data.put("expiresAt", System.currentTimeMillis() + 30000);
        data.put("ttlSeconds", 30);
        return ApiResponse.success(data);
    }

    /**
     * 查询本人借阅记录 §11.3
     */
    @GetMapping("/borrows")
    public ApiResponse<Map<String, Object>> myBorrows(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        // TODO: 实现借阅查询
        Map<String, Object> data = new HashMap<>();
        data.put("memberDisplay", "138****0001");
        data.put("yearBorrowCount", 0);
        data.put("currentBorrowingCount", 0);
        data.put("page", new PageResult<>(Collections.emptyList(), pageNo, pageSize, 0));
        return ApiResponse.success(data);
    }

    /**
     * 查询借阅详情 §11.4
     */
    @GetMapping("/borrows/{borrowId}")
    public ApiResponse<Map<String, Object>> borrowDetail(@PathVariable String borrowId) {
        // TODO: 实现借阅详情查询
        return ApiResponse.success(new HashMap<>());
    }

    /**
     * 查询本人积分记录 §11.5
     */
    @GetMapping("/points-records")
    public ApiResponse<Map<String, Object>> myPointsRecords(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
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
        Map<String, Object> data = new HashMap<>();
        data.put("memberDisplay", "138****0001");
        data.put("currentPoints", 100);
        data.put("yearEarnedPoints", 50);
        data.put("page", new PageResult<>(records, pageNo, pageSize, records.size()));
        return ApiResponse.success(data);
    }

    /**
     * 查询积分详情 §11.6
     */
    @GetMapping("/points-records/{pointsRecordId}")
    public ApiResponse<Map<String, Object>> pointsDetail(@PathVariable String pointsRecordId) {
        Map<String, Object> data = new HashMap<>();
        data.put("pointsRecordId", pointsRecordId);
        return ApiResponse.success(data);
    }
}
