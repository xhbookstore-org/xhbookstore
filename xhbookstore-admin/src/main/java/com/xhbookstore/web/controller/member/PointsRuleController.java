package com.xhbookstore.web.controller.member;

import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.common.annotation.Log;
import com.xhbookstore.common.core.controller.BaseController;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.core.page.TableDataInfo;
import com.xhbookstore.common.enums.BusinessType;
import com.xhbookstore.system.domain.member.PointsRule;
import com.xhbookstore.system.service.member.IPointsRuleService;

@RestController
@RequestMapping("/system/points-rule")
public class PointsRuleController extends BaseController {
    @Autowired private IPointsRuleService pointsRuleService;

    @PreAuthorize("@ss.hasPermi('system:pointsRule:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String ruleName,
                              @RequestParam(required = false) String direction,
                              @RequestParam(required = false) String implementationStatus,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginEffectiveFrom,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endEffectiveFrom) {
        startPage();
        List<PointsRule> list = pointsRuleService.selectRuleList(ruleName, direction, implementationStatus,
                beginEffectiveFrom, endEffectiveFrom);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:pointsRule:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        PointsRule rule = pointsRuleService.selectRuleById(id);
        return rule == null ? error("积分规则不存在") : success(rule);
    }

    @PreAuthorize("@ss.hasPermi('system:pointsRule:add')")
    @Log(title = "积分规则管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PointsRule rule) {
        return pointsRuleService.createRule(rule, getUserId(), getUsername());
    }

    @PreAuthorize("@ss.hasPermi('system:pointsRule:edit')")
    @Log(title = "积分规则管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PointsRule rule) {
        return pointsRuleService.updateRule(rule, getUserId(), getUsername());
    }

    @PreAuthorize("@ss.hasPermi('system:pointsRule:remove')")
    @Log(title = "积分规则管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return pointsRuleService.deleteRules(ids, getUserId(), getUsername());
    }

    @PreAuthorize("@ss.hasPermi('system:pointsRule:edit')")
    @Log(title = "积分规则", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/points")
    public AjaxResult updatePoints(@PathVariable Long id, @RequestBody PointsRuleUpdateRequest request) {
        return pointsRuleService.updateRulePoints(id, request.getFixedPoints(), request.getPointsPerUnit(),
                getUserId(), getUsername());
    }
}
