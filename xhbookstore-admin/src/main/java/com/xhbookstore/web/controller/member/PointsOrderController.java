package com.xhbookstore.web.controller.member;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xhbookstore.common.core.controller.BaseController;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.core.page.TableDataInfo;
import com.xhbookstore.system.domain.member.PointsOrderRecord;
import com.xhbookstore.system.service.member.IPointsOrderAdminService;

@RestController
@RequestMapping("/member/points/order")
public class PointsOrderController extends BaseController {
    @Autowired private IPointsOrderAdminService pointsOrderAdminService;

    @PreAuthorize("@ss.hasPermi('member:points:list')")
    @GetMapping("/list")
    public TableDataInfo list(PointsOrderRecord query) {
        startPage();
        return getDataTable(pointsOrderAdminService.selectList(query));
    }

    @PreAuthorize("@ss.hasPermi('member:points:query')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Integer id) {
        PointsOrderRecord query = new PointsOrderRecord();
        query.setId(id);
        Map<String, Object> detail = pointsOrderAdminService.selectDetail(query);
        return detail == null ? AjaxResult.error("积分流水不存在或无权查看") : AjaxResult.success(detail);
    }

    @PreAuthorize("@ss.hasPermi('member:points:list')")
    @GetMapping("/rules/options")
    public AjaxResult ruleOptions() {
        return AjaxResult.success(pointsOrderAdminService.selectRuleOptions());
    }
}
