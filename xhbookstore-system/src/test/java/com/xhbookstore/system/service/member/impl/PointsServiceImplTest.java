package com.xhbookstore.system.service.member.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.PointsRule;
import com.xhbookstore.system.domain.member.PointsUserIntoBillDetail;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.mapper.member.PointsOrderMapper;
import com.xhbookstore.system.mapper.member.PointsRuleMapper;
import com.xhbookstore.system.mapper.member.PointsUserIntoBillDetailMapper;
import com.xhbookstore.system.mapper.member.PointsUserOutBillDetailMapper;

@ExtendWith(MockitoExtension.class)
class PointsServiceImplTest {
    @Mock private PointsOrderMapper pointsOrderMapper;
    @Mock private PointsRuleMapper pointsRuleMapper;
    @Mock private PointsUserIntoBillDetailMapper intoBillMapper;
    @Mock private PointsUserOutBillDetailMapper outBillMapper;
    @Mock private MemberMapper memberMapper;
    @InjectMocks private PointsServiceImpl service;

    @Test
    void grantBorrowPointsUsesPerItemRuleAndWritesBusinessKey() {
        Member member = member(1, 30);
        when(pointsOrderMapper.selectByBusinessKey("BORROW_BOOK:DY001"))
                .thenReturn(null)
                .thenReturn(null);
        when(memberMapper.selectMemberById(1)).thenReturn(member);
        when(memberMapper.selectMemberByIdForUpdate(1)).thenReturn(member);
        when(pointsRuleMapper.selectActiveForUpdate("BORROW_BOOK", "BORROW_COMPLETED", 3L, null))
                .thenReturn(borrowRule());
        when(pointsOrderMapper.insertRulePointsOrder(any())).thenReturn(1);
        when(intoBillMapper.insertIntoBill(any())).thenReturn(1);
        when(memberMapper.updateMember(any())).thenReturn(1);
        when(pointsRuleMapper.incrementUsage(7L, 20)).thenReturn(1);

        Map<String, Object> result = service.grantBorrowPoints(1, "DY001", 2, 3L);

        assertThat(result).containsEntry("status", "SUCCESS").containsEntry("points", 20);
        ArgumentCaptor<Map<String, Object>> order = ArgumentCaptor.forClass(Map.class);
        verify(pointsOrderMapper).insertRulePointsOrder(order.capture());
        assertThat(order.getValue())
                .containsEntry("businessKey", "BORROW_BOOK:DY001")
                .containsEntry("baseQuantity", BigDecimal.valueOf(2))
                .containsEntry("amount", 20)
                .containsEntry("beforePoints", 30)
                .containsEntry("afterPoints", 50);
        verify(pointsRuleMapper).incrementUsage(7L, 20);
        ArgumentCaptor<PointsUserIntoBillDetail> bill = ArgumentCaptor.forClass(PointsUserIntoBillDetail.class);
        verify(intoBillMapper).insertIntoBill(bill.capture());
        long validForDays = (bill.getValue().getExpiredTime().getTime() - new Date().getTime()) / 86_400_000L;
        assertThat(validForDays).isBetween(359L, 360L);
    }

    @Test
    void grantBorrowPointsReturnsExistingOrderWithoutGrantingAgain() {
        when(pointsOrderMapper.selectByBusinessKey("BORROW_BOOK:DY001"))
                .thenReturn(Map.of("amount", 20, "order_number", "IN001"));

        Map<String, Object> result = service.grantBorrowPoints(1, "DY001", 2, 3L);

        assertThat(result).containsEntry("status", "IDEMPOTENT").containsEntry("points", 20);
        verify(pointsRuleMapper, never()).selectActiveForUpdate(any(), any(), any(), any());
        verify(memberMapper, never()).updateMember(any());
    }

    @Test
    void adjustPointsByRuleAddsConfiguredFixedPointsAndAuditSnapshot() {
        Member member = member(1, 30);
        member.setDeptId(3L);
        PointsRule rule = manualRule(9L, "MANUAL_ADD_100", "人工奖励100分", "ADD", 100);
        when(memberMapper.selectMemberById(1)).thenReturn(member);
        when(pointsRuleMapper.selectManualFixedRuleForUpdate(9L, 3L, null)).thenReturn(rule);
        when(memberMapper.selectMemberByIdForUpdate(1)).thenReturn(member);
        when(pointsOrderMapper.insertManualRulePointsOrder(any())).thenReturn(1);
        when(intoBillMapper.insertIntoBill(any())).thenReturn(1);
        when(memberMapper.updateMember(any())).thenReturn(1);
        when(pointsRuleMapper.incrementUsage(9L, 100)).thenReturn(1);

        AjaxResult result = service.adjustPointsByRule(1, 9L, null, "服务补偿", 88L, "admin", "PC");

        assertThat(result.get("code")).isEqualTo(200);
        ArgumentCaptor<Map<String, Object>> order = ArgumentCaptor.forClass(Map.class);
        verify(pointsOrderMapper).insertManualRulePointsOrder(order.capture());
        assertThat(order.getValue())
                .containsEntry("amount", 100)
                .containsEntry("beforePoints", 30)
                .containsEntry("afterPoints", 130)
                .containsEntry("operatorUserId", 88L)
                .containsEntry("operatorName", "admin")
                .containsEntry("direction", "ADD");
        verify(intoBillMapper).insertIntoBill(any());
        verify(outBillMapper, never()).insertOutBill(any());
    }

    @Test
    void editablePurchaseRuleAppliesMemberDayMultiplierToEmployeeInput() {
        Member member = member(1, 30);
        PointsRule rule = manualRule(11L, "PURCHASE_BOOK", "购买书籍", "ADD", 1);
        rule.setManualPointsEditable(1);
        rule.setMemberDayEnabled(1);
        rule.setMemberDayDays("[" + LocalDate.now(ZoneId.of("Asia/Shanghai")).getDayOfMonth() + "]");
        rule.setMemberDayMultiplier(BigDecimal.valueOf(2));
        when(memberMapper.selectMemberById(1)).thenReturn(member);
        when(pointsRuleMapper.selectManualFixedRuleForUpdate(11L, null, null)).thenReturn(rule);
        when(memberMapper.selectMemberByIdForUpdate(1)).thenReturn(member);
        when(pointsOrderMapper.insertManualRulePointsOrder(any())).thenReturn(1);
        when(intoBillMapper.insertIntoBill(any())).thenReturn(1);
        when(memberMapper.updateMember(any())).thenReturn(1);
        when(pointsRuleMapper.incrementUsage(11L, 200)).thenReturn(1);

        AjaxResult result = service.adjustPointsByRule(1, 11L, 100, "购书消费", 88L, "staff", "PC");

        assertThat(result.get("code")).isEqualTo(200);
        ArgumentCaptor<Map<String, Object>> order = ArgumentCaptor.forClass(Map.class);
        verify(pointsOrderMapper).insertManualRulePointsOrder(order.capture());
        assertThat(order.getValue()).containsEntry("amount", 200).containsEntry("afterPoints", 230);
        verify(pointsRuleMapper).incrementUsage(11L, 200);
    }

    @Test
    void adjustPointsByRuleRejectsDeductionWhenBalanceIsInsufficient() {
        Member member = member(1, 30);
        PointsRule rule = manualRule(10L, "MANUAL_DEDUCT_50", "人工扣减50分", "DEDUCT", 50);
        when(memberMapper.selectMemberById(1)).thenReturn(member);
        when(pointsRuleMapper.selectManualFixedRuleForUpdate(10L, null, null)).thenReturn(rule);
        when(memberMapper.selectMemberByIdForUpdate(1)).thenReturn(member);

        AjaxResult result = service.adjustPointsByRule(1, 10L, null, "兑换礼品", 88L, "admin", "PC");

        assertThat(result.get("code")).isEqualTo(500);
        assertThat(result.get("msg")).isEqualTo("积分不足，当前余额：30");
        verify(pointsOrderMapper, never()).insertManualRulePointsOrder(any());
        verify(memberMapper, never()).updateMember(any());
    }

    private Member member(int id, int points) {
        Member member = new Member();
        member.setId(id);
        member.setCardNo("CARD");
        member.setCurrentPoints(points);
        return member;
    }

    private PointsRule borrowRule() {
        PointsRule rule = new PointsRule();
        rule.setId(7L);
        rule.setRuleCode("BORROW_BOOK");
        rule.setRuleName("借阅图书积分");
        rule.setSceneCode("BORROW_BOOK");
        rule.setTriggerEvent("BORROW_COMPLETED");
        rule.setCalculationMode("PER_ITEM");
        rule.setPointsPerUnit(10);
        rule.setPointsValidDays(360);
        return rule;
    }

    private PointsRule manualRule(long id, String code, String name, String direction, int points) {
        PointsRule rule = new PointsRule();
        rule.setId(id);
        rule.setRuleCode(code);
        rule.setRuleName(name);
        rule.setSceneCode("MANUAL_POINTS");
        rule.setDirection(direction);
        rule.setTriggerMode("MANUAL");
        rule.setTriggerEvent(code);
        rule.setCalculationMode("FIXED");
        rule.setFixedPoints(points);
        return rule;
    }
}
