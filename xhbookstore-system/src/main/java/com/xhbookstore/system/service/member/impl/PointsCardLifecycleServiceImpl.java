package com.xhbookstore.system.service.member.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.alibaba.fastjson2.JSON;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberCard;
import com.xhbookstore.system.domain.member.PointsRule;
import com.xhbookstore.system.domain.member.PointsUserIntoBillDetail;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.mapper.member.PointsOrderMapper;
import com.xhbookstore.system.mapper.member.PointsRuleMapper;
import com.xhbookstore.system.mapper.member.PointsUserIntoBillDetailMapper;
import com.xhbookstore.system.service.member.IPointsCardLifecycleService;

@Service
public class PointsCardLifecycleServiceImpl implements IPointsCardLifecycleService {
    @Autowired private PointsOrderMapper pointsOrderMapper;
    @Autowired private PointsRuleMapper pointsRuleMapper;
    @Autowired private MemberMapper memberMapper;
    @Autowired private PointsUserIntoBillDetailMapper intoBillMapper;
    @Autowired private TransactionTemplate transactionTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> grantFrozenCardPoints(Member member, MemberCard card, boolean renewal) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (member == null || card == null || card.getSaleOrderNo() == null) return result;
        String ruleCode = cardRuleCode(card.getCardTypeName(), renewal, false);
        if (ruleCode == null) return result;
        String businessKey = "CARD_POINTS:" + card.getSaleOrderNo();
        Map<String, Object> existing = pointsOrderMapper.selectByBusinessKey(businessKey);
        if (existing != null) return existing;

        PointsRule rule = pointsRuleMapper.selectEnabledByRuleCodeForUpdate(
                ruleCode, card.getDeptId(), card.getCardTypeId());
        if (rule == null || rule.getFixedPoints() == null || rule.getFixedPoints() <= 0) return result;
        if (Integer.valueOf(1).equals(rule.getExcludeBulkPurchase())
                && card.getRemark() != null && !card.getRemark().trim().isEmpty()) {
            result.put("status", "SKIPPED_BULK_PURCHASE");
            return result;
        }

        int points = rule.getFixedPoints();
        int currentPoints = member.getCurrentPoints() == null ? 0 : member.getCurrentPoints();
        Calendar available = Calendar.getInstance();
        available.setTime(card.getPaidAt() == null ? new Date() : card.getPaidAt());
        available.add(Calendar.DAY_OF_MONTH, rule.getFreezeDays() == null ? 0 : rule.getFreezeDays());
        String orderNumber = number("IN");
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("ruleCode", rule.getRuleCode());
        snapshot.put("saleOrderNo", card.getSaleOrderNo());
        snapshot.put("memberCardId", card.getId());
        snapshot.put("freezeDays", rule.getFreezeDays());
        snapshot.put("pointsValidDays", validDays(rule));
        snapshot.put("points", points);

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderNumber", orderNumber);
        order.put("cardNo", member.getCardNo());
        order.put("memberId", member.getId());
        order.put("points", points);
        order.put("currentPoints", currentPoints);
        order.put("description", rule.getRuleName() + "（冻结中）");
        order.put("ruleId", rule.getId());
        order.put("ruleCode", rule.getRuleCode());
        order.put("ruleName", rule.getRuleName());
        order.put("sceneCode", rule.getSceneCode());
        order.put("triggerEvent", rule.getTriggerEvent());
        order.put("availableAt", available.getTime());
        order.put("saleOrderNo", card.getSaleOrderNo());
        order.put("businessKey", businessKey);
        order.put("calculationSnapshot", JSON.toJSONString(snapshot));
        if (pointsOrderMapper.insertFrozenCardPointsOrder(order) != 1) {
            throw new IllegalStateException("购卡冻结积分创建失败");
        }
        if (pointsRuleMapper.incrementUsage(rule.getId(), points) != 1) {
            throw new IllegalStateException("购卡积分规则次数或预算已用完");
        }
        result.put("status", "FROZEN");
        result.put("points", points);
        result.put("availableAt", available.getTime());
        result.put("orderNumber", orderNumber);
        return result;
    }

    @Override
    public Map<String, Integer> processPending(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        List<Long> ids = pointsOrderMapper.selectPendingCardLifecycleIds(safeLimit);
        int unfrozen = 0;
        int cancelled = 0;
        for (Long id : ids) {
            String action = transactionTemplate.execute(status -> processOne(id));
            if ("UNFROZEN".equals(action)) unfrozen++;
            if ("CANCELLED".equals(action)) cancelled++;
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("scanned", ids.size());
        result.put("unfrozen", unfrozen);
        result.put("cancelled", cancelled);
        return result;
    }

    private String processOne(Long id) {
        Map<String, Object> row = pointsOrderMapper.selectCardLifecycleForUpdate(id);
        if (row == null || !"FROZEN".equals(text(row.get("availability_status")))) return "SKIPPED";
        int memberId = integer(row.get("member_id"));
        int points = integer(row.get("amount"));
        Member member = memberMapper.selectMemberByIdForUpdate(memberId);
        if (member == null || points <= 0) throw new IllegalStateException("冻结积分关联会员或积分值无效");
        int currentPoints = member.getCurrentPoints() == null ? 0 : member.getCurrentPoints();

        if (integer(row.get("card_status")) == 3) {
            String refundOrderNo = text(row.get("refund_order_no"));
            String businessKey = "CARD_POINTS_REFUND:" + refundOrderNo;
            if (pointsOrderMapper.selectByBusinessKey(businessKey) == null) {
                String refundRuleCode = cardRuleCode(text(row.get("card_type_name")), false, true);
                PointsRule refundRule = pointsRuleMapper.selectEnabledByRuleCodeForUpdate(
                        refundRuleCode, longValue(row.get("dept_id")), integer(row.get("card_type_id")));
                if (refundRule == null) throw new IllegalStateException("退卡积分冲销规则未启用：" + refundRuleCode);
                Map<String, Object> reversal = new LinkedHashMap<>();
                reversal.put("orderNumber", number("OT"));
                reversal.put("cardNo", row.get("card_no"));
                reversal.put("memberId", memberId);
                reversal.put("points", points);
                reversal.put("currentPoints", currentPoints);
                reversal.put("description", refundRule.getRuleName() + "（冲销冻结积分）");
                reversal.put("ruleId", refundRule.getId());
                reversal.put("ruleCode", refundRule.getRuleCode());
                reversal.put("ruleName", refundRule.getRuleName());
                reversal.put("sceneCode", refundRule.getSceneCode());
                reversal.put("triggerEvent", refundRule.getTriggerEvent());
                reversal.put("refundOrderNo", refundOrderNo);
                reversal.put("businessKey", businessKey);
                reversal.put("originalOrderNo", row.get("order_number"));
                reversal.put("calculationSnapshot", JSON.toJSONString(Map.of(
                        "originalOrderNo", row.get("order_number"), "points", points,
                        "reason", "CARD_REFUNDED_WHILE_FROZEN")));
                pointsOrderMapper.insertFrozenCardReversalOrder(reversal);
                pointsRuleMapper.incrementUsage(refundRule.getId(), points);
            }
            if (pointsOrderMapper.markFrozenCancelled(id) != 1) throw new IllegalStateException("冻结积分冲销状态更新失败");
            return "CANCELLED";
        }

        Member update = new Member();
        update.setId(memberId);
        update.setCurrentPoints(Math.addExact(currentPoints, points));
        update.setLastOperator("SYSTEM");
        if (memberMapper.updateMember(update) != 1) throw new IllegalStateException("积分解冻余额更新失败");
        if (pointsOrderMapper.markFrozenAvailable(id) != 1) throw new IllegalStateException("积分解冻状态更新失败");
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.DAY_OF_MONTH, positiveOrDefault(row.get("points_valid_days"), 360));
        PointsUserIntoBillDetail bill = new PointsUserIntoBillDetail();
        bill.setMemberId(memberId);
        bill.setPoints(points);
        bill.setRemainingPoints(points);
        bill.setDescription("购卡积分解冻");
        bill.setOrderNoSrc(text(row.get("order_number")));
        bill.setOrderNoTarget("");
        bill.setActivityKey(text(row.get("rule_code")));
        bill.setActivityName("购卡积分");
        bill.setEventKey("CARD_POINTS_UNFROZEN");
        bill.setEventName("购卡积分解冻");
        bill.setAccountType(0);
        bill.setBillStatus(0);
        bill.setIsWhiteOrder(0);
        bill.setExpiredTime(expiry.getTime());
        bill.setExpiredTimestamp(expiry.getTimeInMillis());
        bill.setIsDel(0);
        if (intoBillMapper.insertIntoBill(bill) != 1) throw new IllegalStateException("积分解冻入账明细创建失败");
        return "UNFROZEN";
    }

    private String cardRuleCode(String cardTypeName, boolean renewal, boolean refund) {
        if (cardTypeName == null) return null;
        String type = cardTypeName.contains("尊享") ? "PREMIUM"
                : cardTypeName.contains("畅享") ? "ENJOY" : null;
        if (type == null) return null;
        if (refund) return "REFUND_" + type + "_CARD";
        return (renewal ? "RENEW_" : "BUY_") + type + "_CARD";
    }

    private String number(String prefix) {
        return prefix + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
    private String text(Object value) { return value == null ? null : value.toString(); }
    private int integer(Object value) { return value == null ? 0 : Integer.parseInt(value.toString()); }
    private int positiveOrDefault(Object value, int defaultValue) {
        int number = integer(value);
        return number > 0 ? number : defaultValue;
    }
    private int validDays(PointsRule rule) {
        return rule.getPointsValidDays() == null || rule.getPointsValidDays() <= 0
                ? 360 : rule.getPointsValidDays();
    }
    private Long longValue(Object value) { return value == null ? null : Long.valueOf(value.toString()); }
}
