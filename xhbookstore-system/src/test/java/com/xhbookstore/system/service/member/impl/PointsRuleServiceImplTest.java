package com.xhbookstore.system.service.member.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.PointsRule;
import com.xhbookstore.system.mapper.member.PointsRuleMapper;

@ExtendWith(MockitoExtension.class)
class PointsRuleServiceImplTest {
    @Mock private PointsRuleMapper pointsRuleMapper;
    @InjectMocks private PointsRuleServiceImpl service;

    @Test
    void updateFixedPointsKeepsUnitValueAndWritesOperator() {
        PointsRule rule = rule(1L, "FIXED");
        rule.setFixedPoints(100);
        when(pointsRuleMapper.selectRuleById(1L)).thenReturn(rule);
        when(pointsRuleMapper.updateRulePoints(1L, 120, null, 8L, "admin")).thenReturn(1);

        AjaxResult result = service.updateRulePoints(1L, 120, null, 8L, "admin");

        assertThat(result.get("code")).isEqualTo(200);
        verify(pointsRuleMapper).updateRulePoints(1L, 120, null, 8L, "admin");
    }

    @Test
    void updatePerUnitPointsKeepsFixedValue() {
        PointsRule rule = rule(2L, "PER_YUAN");
        rule.setPointsPerUnit(1);
        when(pointsRuleMapper.selectRuleById(2L)).thenReturn(rule);
        when(pointsRuleMapper.updateRulePoints(2L, null, 2, 8L, "admin"))
                .thenReturn(1);

        AjaxResult result = service.updateRulePoints(2L, null, 2, 8L, "admin");

        assertThat(result.get("code")).isEqualTo(200);
        verify(pointsRuleMapper).updateRulePoints(2L, null, 2, 8L, "admin");
    }

    @Test
    void originalOrderRuleCannotBeChangedToArbitraryPoints() {
        PointsRule rule = rule(3L, "ORIGINAL_ORDER");
        when(pointsRuleMapper.selectRuleById(3L)).thenReturn(rule);

        AjaxResult result = service.updateRulePoints(3L, 50, null, 8L, "admin");

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg")).isEqualTo("该规则按原订单实际积分计算，不能配置固定积分");
        verify(pointsRuleMapper, never()).updateRulePoints(3L, 50, null, 8L, "admin");
    }

    @Test
    void createRuleNormalizesCodeAndWritesAuditOperator() {
        PointsRule rule = editableRule();
        rule.setRuleCode("custom_reward");
        rule.setMemberLimit(0);
        rule.setTotalLimit(0);
        rule.setMaxPointsPerOrder(0);
        rule.setBudgetPoints(0L);
        when(pointsRuleMapper.countRuleCode("CUSTOM_REWARD", null)).thenReturn(0);
        when(pointsRuleMapper.insertRule(rule)).thenReturn(1);

        AjaxResult result = service.createRule(rule, 8L, "admin");

        assertThat(result.get("code")).isEqualTo(200);
        ArgumentCaptor<PointsRule> captor = ArgumentCaptor.forClass(PointsRule.class);
        verify(pointsRuleMapper).insertRule(captor.capture());
        assertThat(captor.getValue().getRuleCode()).isEqualTo("CUSTOM_REWARD");
        assertThat(captor.getValue().getOperatorUserId()).isEqualTo(8L);
        assertThat(captor.getValue().getOperatorName()).isEqualTo("admin");
        assertThat(captor.getValue().getMemberLimit()).isNull();
        assertThat(captor.getValue().getTotalLimit()).isNull();
        assertThat(captor.getValue().getMaxPointsPerOrder()).isNull();
        assertThat(captor.getValue().getBudgetPoints()).isNull();
        assertThat(captor.getValue().getPointsValidDays()).isEqualTo(360);
    }

    @Test
    void updateRuleKeepsExistingImmutableCode() {
        PointsRule existing = editableRule();
        existing.setId(4L);
        existing.setRuleCode("SYSTEM_CODE");
        PointsRule update = editableRule();
        update.setId(4L);
        update.setRuleCode("CHANGED_CODE");
        when(pointsRuleMapper.selectRuleById(4L)).thenReturn(existing);
        when(pointsRuleMapper.updateRule(update)).thenReturn(1);

        AjaxResult result = service.updateRule(update, 8L, "admin");

        assertThat(result.get("code")).isEqualTo(200);
        assertThat(update.getRuleCode()).isEqualTo("SYSTEM_CODE");
        verify(pointsRuleMapper).updateRule(update);
    }

    @Test
    void deleteRuleWithHistoryIsRejected() {
        PointsRule rule = editableRule();
        rule.setId(5L);
        rule.setRuleName("已使用规则");
        rule.setUsedCount(1);
        when(pointsRuleMapper.selectRuleById(5L)).thenReturn(rule);

        AjaxResult result = service.deleteRules(new Long[]{5L}, 8L, "admin");

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg")).asString().contains("已有积分流水");
        verify(pointsRuleMapper, never()).logicalDeleteRule(5L, 8L, "admin");
    }

    @Test
    void unusedRuleIsLogicallyDeleted() {
        PointsRule rule = editableRule();
        rule.setId(6L);
        when(pointsRuleMapper.selectRuleById(6L)).thenReturn(rule);
        when(pointsRuleMapper.countPointsOrdersByRuleId(6L)).thenReturn(0);
        when(pointsRuleMapper.logicalDeleteRule(6L, 8L, "admin")).thenReturn(1);

        AjaxResult result = service.deleteRules(new Long[]{6L}, 8L, "admin");

        assertThat(result.get("code")).isEqualTo(200);
        verify(pointsRuleMapper).logicalDeleteRule(6L, 8L, "admin");
    }

    private PointsRule rule(Long id, String calculationMode) {
        PointsRule rule = new PointsRule();
        rule.setId(id);
        rule.setCalculationMode(calculationMode);
        return rule;
    }

    private PointsRule editableRule() {
        PointsRule rule = new PointsRule();
        rule.setRuleCode("CUSTOM_RULE");
        rule.setRuleName("自定义规则");
        rule.setSceneCode("CUSTOM_SCENE");
        rule.setRuleSource("CUSTOM");
        rule.setImplementationStatus("NOT_STARTED");
        rule.setDirection("ADD");
        rule.setTriggerMode("MANUAL");
        rule.setTriggerEvent("MANUAL_INPUT");
        rule.setCalculationMode("FIXED");
        rule.setFixedPoints(10);
        rule.setStatus("DRAFT");
        return rule;
    }
}
