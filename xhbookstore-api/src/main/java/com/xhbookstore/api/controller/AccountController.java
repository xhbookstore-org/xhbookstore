package com.xhbookstore.api.controller;

import java.util.HashMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.api.model.ApiResponse;

/**
 * 账号注销接口 - 文档 §9.4-9.5
 */
@Tag(name = "账号管理", description = "注销资格查询、注销账号")
@RestController
@RequestMapping("/api/mp/v1/account")
public class AccountController {

    /**
     * 查询注销前置状态 §9.4
     */
    @Operation(summary = "查询注销前置状态", description = "检查当前用户是否可以注销（有无未还书/是否在职员工）")
    @GetMapping("/cancel-eligibility")
    public ApiResponse<Map<String, Object>> cancelEligibility() {
        // TODO: 实际查询用户借阅状态和员工身份
        Map<String, Object> data = new HashMap<>();
        data.put("canCancel", true);
        data.put("blockedReason", null);
        data.put("hasUnreturnedBooks", false);
        data.put("isStaffActive", false);
        data.put("currentPoints", 100);
        return ApiResponse.success(data);
    }

    /**
     * 注销账号 §9.5
     */
    @Operation(summary = "注销账号", description = "执行账号注销。入参JSON: reason(注销原因), confirmRead(是否确认)")
    @PostMapping("/cancel")
    public ApiResponse<Map<String, Object>> cancel(@RequestBody Map<String, Object> body) {
        // TODO: 实际执行注销逻辑
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("cancelledAt", System.currentTimeMillis());
        return ApiResponse.success(data);
    }
}
