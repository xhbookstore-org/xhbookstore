package com.xhbookstore.web.controller.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xhbookstore.common.annotation.Log;
import com.xhbookstore.common.core.controller.BaseController;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.enums.BusinessType;
import com.xhbookstore.system.service.dashboard.IMemberDashboardService;

@RestController
@RequestMapping("/dashboard/member")
public class MemberDashboardController extends BaseController {
    @Autowired
    private IMemberDashboardService memberDashboardService;

    @GetMapping("/overview")
    public AjaxResult overview() {
        return success(memberDashboardService.getOverview());
    }

    @Log(title = "会员首页统计", businessType = BusinessType.UPDATE)
    @PostMapping("/refresh")
    public AjaxResult refresh() {
        boolean refreshed = memberDashboardService.refreshStatsWithLock();
        return refreshed ? success("刷新成功") : AjaxResult.warn("已有其他节点正在刷新，请稍后查看");
    }
}
