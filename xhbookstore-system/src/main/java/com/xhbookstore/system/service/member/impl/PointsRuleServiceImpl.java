package com.xhbookstore.system.service.member.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.PointsRule;
import com.xhbookstore.system.mapper.member.PointsRuleMapper;
import com.xhbookstore.system.service.member.IPointsRuleService;

@Service
public class PointsRuleServiceImpl implements IPointsRuleService {
    @Autowired private PointsRuleMapper pointsRuleMapper;

    @Override
    public List<PointsRule> selectRuleList(String ruleName, String direction, String implementationStatus) {
        String normalizedDirection = direction == null ? null : direction.trim().toUpperCase();
        String normalizedStatus = implementationStatus == null ? null : implementationStatus.trim().toUpperCase();
        return pointsRuleMapper.selectRuleList(ruleName == null ? null : ruleName.trim(),
                normalizedDirection, normalizedStatus);
    }

    @Override
    public PointsRule selectRuleById(Long id) {
        return pointsRuleMapper.selectRuleById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult createRule(PointsRule rule, Long operatorUserId, String operatorName) {
        String error = normalizeAndValidate(rule, true);
        if (error != null) return AjaxResult.error(error);
        if (pointsRuleMapper.countRuleCode(rule.getRuleCode(), null) > 0) {
            return AjaxResult.error("规则编码已存在");
        }
        rule.setOperatorUserId(operatorUserId);
        rule.setOperatorName(operatorName);
        if (pointsRuleMapper.insertRule(rule) != 1) throw new IllegalStateException("积分规则新增失败");
        return AjaxResult.success("积分规则新增成功", pointsRuleMapper.selectRuleById(rule.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateRule(PointsRule rule, Long operatorUserId, String operatorName) {
        if (rule == null || rule.getId() == null) return AjaxResult.error("规则ID不能为空");
        PointsRule existing = pointsRuleMapper.selectRuleById(rule.getId());
        if (existing == null) return AjaxResult.error("积分规则不存在");
        rule.setRuleCode(existing.getRuleCode());
        String error = normalizeAndValidate(rule, false);
        if (error != null) return AjaxResult.error(error);
        rule.setOperatorUserId(operatorUserId);
        rule.setOperatorName(operatorName);
        if (pointsRuleMapper.updateRule(rule) != 1) throw new IllegalStateException("积分规则修改失败");
        return AjaxResult.success("积分规则修改成功", pointsRuleMapper.selectRuleById(rule.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteRules(Long[] ids, Long operatorUserId, String operatorName) {
        if (ids == null || ids.length == 0) return AjaxResult.error("请选择要删除的积分规则");
        List<Long> distinctIds = Arrays.stream(ids).filter(id -> id != null).distinct().collect(Collectors.toList());
        if (distinctIds.isEmpty()) return AjaxResult.error("请选择要删除的积分规则");
        for (Long id : distinctIds) {
            PointsRule rule = pointsRuleMapper.selectRuleById(id);
            if (rule == null) return AjaxResult.error("积分规则不存在或已删除，ID：" + id);
            if ((rule.getUsedCount() != null && rule.getUsedCount() > 0)
                    || pointsRuleMapper.countPointsOrdersByRuleId(id) > 0) {
                return AjaxResult.error("规则“" + rule.getRuleName() + "”已有积分流水，不能删除；可改为停用");
            }
        }
        for (Long id : distinctIds) {
            if (pointsRuleMapper.logicalDeleteRule(id, operatorUserId, operatorName) != 1) {
                throw new IllegalStateException("积分规则删除失败，ID：" + id);
            }
        }
        return AjaxResult.success("积分规则删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult updateRulePoints(Long id, Integer fixedPoints, Integer pointsPerUnit,
                                       Long operatorUserId, String operatorName) {
        PointsRule rule = pointsRuleMapper.selectRuleById(id);
        if (rule == null) return AjaxResult.error("积分规则不存在");

        String mode = rule.getCalculationMode();
        Integer nextFixedPoints = rule.getFixedPoints();
        Integer nextPointsPerUnit = rule.getPointsPerUnit();
        if ("FIXED".equals(mode)) {
            if (fixedPoints == null || fixedPoints <= 0) return AjaxResult.error("固定积分必须大于0");
            nextFixedPoints = fixedPoints;
        } else if ("PER_ITEM".equals(mode) || "PER_YUAN".equals(mode)) {
            if (pointsPerUnit == null || pointsPerUnit <= 0) {
                return AjaxResult.error("单位积分必须大于0");
            }
            nextPointsPerUnit = pointsPerUnit;
        } else if ("ORIGINAL_ORDER".equals(mode)) {
            return AjaxResult.error("该规则按原订单实际积分计算，不能配置固定积分");
        } else {
            return AjaxResult.error("当前计算方式暂不支持修改积分数值");
        }

        int rows = pointsRuleMapper.updateRulePoints(id, nextFixedPoints, nextPointsPerUnit,
                operatorUserId, operatorName);
        if (rows != 1) throw new IllegalStateException("积分规则更新失败");
        return AjaxResult.success("积分规则已更新", pointsRuleMapper.selectRuleById(id));
    }

    private String normalizeAndValidate(PointsRule rule, boolean creating) {
        if (rule == null) return "积分规则不能为空";
        if (creating) {
            String code = upper(rule.getRuleCode());
            if (code == null || !code.matches("[A-Z][A-Z0-9_]{1,63}")) {
                return "规则编码须为2-64位大写字母、数字或下划线，且以字母开头";
            }
            rule.setRuleCode(code);
        }
        rule.setRuleName(trim(rule.getRuleName()));
        rule.setSceneCode(upper(rule.getSceneCode()));
        rule.setRuleSource(defaultUpper(rule.getRuleSource(), "CUSTOM"));
        rule.setImplementationStatus(defaultUpper(rule.getImplementationStatus(), "NOT_STARTED"));
        rule.setDirection(upper(rule.getDirection()));
        rule.setTriggerMode(upper(rule.getTriggerMode()));
        rule.setTriggerEvent(upper(rule.getTriggerEvent()));
        rule.setCalculationMode(upper(rule.getCalculationMode()));
        rule.setUnitType(upper(rule.getUnitType()));
        rule.setStatus(defaultUpper(rule.getStatus(), "DRAFT"));
        rule.setRemark(trim(rule.getRemark()));

        if (blank(rule.getRuleName())) return "规则名称不能为空";
        if (blank(rule.getSceneCode())) return "业务场景编码不能为空";
        if (!oneOf(rule.getRuleSource(), "SYSTEM", "CUSTOM")) return "规则来源不正确";
        if (!oneOf(rule.getImplementationStatus(), "EXISTING", "IN_PROGRESS", "NOT_STARTED")) return "开发状态不正确";
        if (!oneOf(rule.getDirection(), "ADD", "DEDUCT")) return "积分方向不正确";
        if (!oneOf(rule.getTriggerMode(), "AUTO", "MANUAL")) return "触发方式不正确";
        if (blank(rule.getTriggerEvent())) return "触发事件不能为空";
        if (!oneOf(rule.getCalculationMode(), "FIXED", "PER_ITEM", "PER_YUAN", "MANUAL", "ORIGINAL_ORDER")) {
            return "计算方式不正确";
        }
        if (!oneOf(rule.getStatus(), "DRAFT", "ENABLED", "DISABLED", "ENDED")) return "规则状态不正确";

        if ("FIXED".equals(rule.getCalculationMode())) {
            if (rule.getFixedPoints() != null && rule.getFixedPoints() <= 0) return "固定积分必须大于0";
            if ("ENABLED".equals(rule.getStatus()) && rule.getFixedPoints() == null) return "启用固定积分规则前必须配置固定积分";
            rule.setPointsPerUnit(null);
            rule.setUnitType(null);
        } else if ("PER_ITEM".equals(rule.getCalculationMode()) || "PER_YUAN".equals(rule.getCalculationMode())) {
            if (rule.getPointsPerUnit() != null && rule.getPointsPerUnit() <= 0) {
                return "单位积分必须大于0";
            }
            if ("ENABLED".equals(rule.getStatus()) && rule.getPointsPerUnit() == null) return "启用单位积分规则前必须配置单位积分";
            rule.setFixedPoints(null);
            rule.setUnitType("PER_ITEM".equals(rule.getCalculationMode()) ? "ITEM" : "YUAN");
        } else {
            rule.setFixedPoints(null);
            rule.setPointsPerUnit(null);
            rule.setUnitType(null);
        }

        rule.setManualPointsEditable(flag(rule.getManualPointsEditable()));
        if ("MANUAL".equals(rule.getCalculationMode())) rule.setManualPointsEditable(1);
        rule.setMemberDayEnabled(flag(rule.getMemberDayEnabled()));
        rule.setRequireBizOrder(defaultFlag(rule.getRequireBizOrder(), 1));
        rule.setRequireEvidence(flag(rule.getRequireEvidence()));
        rule.setExcludeBulkPurchase(flag(rule.getExcludeBulkPurchase()));
        rule.setFreezeDays(defaultNonNegative(rule.getFreezeDays()));
        rule.setSortOrder(defaultNonNegative(rule.getSortOrder()));
        if (rule.getFreezeDays() > 0 && !"ADD".equals(rule.getDirection())) return "只有获取积分规则可以设置冻结天数";
        if (rule.getEffectiveFrom() != null && rule.getEffectiveTo() != null
                && rule.getEffectiveFrom().after(rule.getEffectiveTo())) return "生效时间不能晚于结束时间";
        if (negative(rule.getMemberLimit()) || negative(rule.getTotalLimit()) || negative(rule.getMaxPointsPerOrder())) {
            return "次数或单笔积分上限不能小于0";
        }
        if (rule.getBudgetPoints() != null && rule.getBudgetPoints() < 0) return "积分预算不能小于0";
        rule.setMemberLimit(positiveOrNull(rule.getMemberLimit()));
        rule.setTotalLimit(positiveOrNull(rule.getTotalLimit()));
        rule.setMaxPointsPerOrder(positiveOrNull(rule.getMaxPointsPerOrder()));
        rule.setBudgetPoints(rule.getBudgetPoints() == null || rule.getBudgetPoints() <= 0 ? null : rule.getBudgetPoints());

        if (rule.getMemberDayEnabled() == 1) {
            if (rule.getMemberDayMultiplier() == null || rule.getMemberDayMultiplier().compareTo(BigDecimal.ZERO) <= 0) {
                return "启用会员日时倍数必须大于0";
            }
            try {
                List<Integer> days = JSON.parseArray(rule.getMemberDayDays(), Integer.class);
                if (days == null || days.isEmpty() || days.stream().anyMatch(day -> day == null || day < 1 || day > 31)) {
                    return "会员日日期必须是1-31的非空数组";
                }
                rule.setMemberDayDays(JSON.toJSONString(days.stream().distinct().sorted().collect(Collectors.toList())));
            } catch (Exception e) {
                return "会员日日期格式不正确，应类似[6,16,26]";
            }
        } else {
            rule.setMemberDayDays(null);
            rule.setMemberDayMultiplier(null);
        }
        return null;
    }

    private boolean oneOf(String value, String... values) { return value != null && Arrays.asList(values).contains(value); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String trim(String value) { return value == null ? null : value.trim(); }
    private String upper(String value) { return blank(value) ? null : value.trim().toUpperCase(Locale.ROOT); }
    private String defaultUpper(String value, String defaultValue) { String normalized = upper(value); return normalized == null ? defaultValue : normalized; }
    private Integer flag(Integer value) { return value != null && value == 1 ? 1 : 0; }
    private Integer defaultFlag(Integer value, int defaultValue) { return value == null ? defaultValue : flag(value); }
    private Integer defaultNonNegative(Integer value) { return value == null ? 0 : Math.max(value, 0); }
    private boolean negative(Integer value) { return value != null && value < 0; }
    private Integer positiveOrNull(Integer value) { return value == null || value <= 0 ? null : value; }
}
