package com.xhbookstore.system.service.member.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.domain.member.PointsRule;
import com.xhbookstore.system.domain.member.PointsUserIntoBillDetail;
import com.xhbookstore.system.domain.member.PointsUserOutBillDetail;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.mapper.member.PointsOrderMapper;
import com.xhbookstore.system.mapper.member.PointsRuleMapper;
import com.xhbookstore.system.mapper.member.PointsUserIntoBillDetailMapper;
import com.xhbookstore.system.mapper.member.PointsUserOutBillDetailMapper;
import com.xhbookstore.system.service.member.IPointsService;

/**
 * 积分/书城币管理服务实现
 *
 * 添加积分流程（悲观锁 + 事务保证原子性）：
 * 1. SELECT ... FOR UPDATE 锁定会员行
 * 2. 记录操作前积分
 * 3. 插入订单表 xhbs_points_order
 * 4. 插入入账单表 xhbs_points_user_into_bill_detail
 * 5. 更新会员积分余额
 * 6. 事务提交，释放行锁
 *
 * 并发安全：同一会员并发加积分时，后者等待前者事务提交后才能读取最新积分
 */
@Service
public class PointsServiceImpl implements IPointsService {

    @Autowired private PointsOrderMapper pointsOrderMapper;
    @Autowired private PointsRuleMapper pointsRuleMapper;
    @Autowired private PointsUserIntoBillDetailMapper intoBillMapper;
    @Autowired private PointsUserOutBillDetailMapper outBillMapper;
    @Autowired private MemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> grantBorrowPoints(Integer memberId, String borrowOrderNo,
                                                  int detailCount, Long deptId) {
        if (memberId == null || borrowOrderNo == null || borrowOrderNo.isBlank() || detailCount <= 0) {
            throw new IllegalArgumentException("Invalid borrow points parameters");
        }
        String businessKey = "BORROW_BOOK:" + borrowOrderNo;
        Map<String, Object> existing = pointsOrderMapper.selectByBusinessKey(businessKey);
        if (existing != null) return pointsResult("IDEMPOTENT", number(existing.get("amount")), existing.get("order_number"));

        Member memberView = memberMapper.selectMemberById(memberId);
        if (memberView == null) throw new IllegalStateException("Member not found while granting borrow points");
        PointsRule rule = pointsRuleMapper.selectActiveForUpdate(
                "BORROW_BOOK", "BORROW_COMPLETED", deptId, memberView.getCardTypeId());
        if (rule == null) return pointsResult("SKIPPED_NO_RULE", 0, null);

        // The rule row lock serializes usage-limit checks. Recheck idempotency after acquiring it.
        existing = pointsOrderMapper.selectByBusinessKey(businessKey);
        if (existing != null) return pointsResult("IDEMPOTENT", number(existing.get("amount")), existing.get("order_number"));
        if (rule.getMemberLimit() != null
                && pointsOrderMapper.countSuccessByRuleAndMember(rule.getId(), memberId) >= rule.getMemberLimit()) {
            return pointsResult("SKIPPED_MEMBER_LIMIT", 0, null);
        }

        int points = calculatePoints(rule, detailCount);
        if (rule.getMaxPointsPerOrder() != null) points = Math.min(points, rule.getMaxPointsPerOrder());
        if (points <= 0) throw new IllegalStateException("BORROW_BOOK rule calculated non-positive points");

        Member member = memberMapper.selectMemberByIdForUpdate(memberId);
        if (member == null) throw new IllegalStateException("Member not found while granting borrow points");
        int beforePoints = member.getCurrentPoints() == null ? 0 : member.getCurrentPoints();
        int afterPoints = Math.addExact(beforePoints, points);
        String orderNumber = generateRuleOrderNumber();

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("ruleId", rule.getId());
        snapshot.put("ruleCode", rule.getRuleCode());
        snapshot.put("calculationMode", rule.getCalculationMode());
        snapshot.put("pointsPerUnit", rule.getPointsPerUnit());
        snapshot.put("baseQuantity", detailCount);
        snapshot.put("points", points);

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderNumber", orderNumber);
        order.put("openId", member.getCardNo() == null ? "" : member.getCardNo());
        order.put("cardNo", member.getCardNo() == null ? "" : member.getCardNo());
        order.put("memberId", memberId);
        order.put("amount", points);
        order.put("description", rule.getRuleName());
        order.put("beforePoints", beforePoints);
        order.put("afterPoints", afterPoints);
        order.put("ruleId", rule.getId());
        order.put("ruleCode", rule.getRuleCode());
        order.put("ruleName", rule.getRuleName());
        order.put("sceneCode", rule.getSceneCode());
        order.put("triggerEvent", rule.getTriggerEvent());
        order.put("calculationMode", rule.getCalculationMode());
        order.put("baseQuantity", BigDecimal.valueOf(detailCount));
        order.put("basePoints", points);
        order.put("businessType", "BOOK_BORROW");
        order.put("businessOrderNo", borrowOrderNo);
        order.put("businessKey", businessKey);
        order.put("idempotencyKey", businessKey);
        order.put("calculationSnapshot", JSON.toJSONString(snapshot));
        if (pointsOrderMapper.insertRulePointsOrder(order) != 1) {
            throw new IllegalStateException("Failed to create borrow points order");
        }

        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.YEAR, 1);
        PointsUserIntoBillDetail intoBill = new PointsUserIntoBillDetail();
        intoBill.setMemberId(memberId);
        intoBill.setPoints(points);
        intoBill.setRemainingPoints(points);
        intoBill.setDescription(rule.getRuleName());
        intoBill.setOrderNoSrc(orderNumber);
        intoBill.setOrderNoTarget(borrowOrderNo);
        intoBill.setActivityKey(rule.getRuleCode());
        intoBill.setActivityName(rule.getRuleName());
        intoBill.setEventKey(rule.getTriggerEvent());
        intoBill.setEventName("借阅完成");
        intoBill.setAccountType(0);
        intoBill.setBillStatus(0);
        intoBill.setIsWhiteOrder(0);
        intoBill.setExpiredTime(expiry.getTime());
        intoBill.setExpiredTimestamp(expiry.getTimeInMillis());
        intoBill.setIsDel(0);
        if (intoBillMapper.insertIntoBill(intoBill) != 1) {
            throw new IllegalStateException("Failed to create borrow points bill");
        }

        Member updateMember = new Member();
        updateMember.setId(memberId);
        updateMember.setCurrentPoints(afterPoints);
        updateMember.setLastOperator("SYSTEM");
        if (memberMapper.updateMember(updateMember) != 1) {
            throw new IllegalStateException("Failed to update member points");
        }
        if (pointsRuleMapper.incrementUsage(rule.getId(), points) != 1) {
            throw new IllegalStateException("BORROW_BOOK rule limit or budget exceeded");
        }
        return pointsResult("SUCCESS", points, orderNumber);
    }

    private int calculatePoints(PointsRule rule, int detailCount) {
        if ("FIXED".equals(rule.getCalculationMode()) && rule.getFixedPoints() != null) {
            return rule.getFixedPoints();
        } else if ("PER_ITEM".equals(rule.getCalculationMode()) && rule.getPointsPerUnit() != null) {
            return Math.multiplyExact(rule.getPointsPerUnit(), detailCount);
        } else {
            throw new IllegalStateException("Unsupported BORROW_BOOK calculation mode");
        }
    }

    private Map<String, Object> pointsResult(String status, int points, Object orderNumber) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        result.put("points", points);
        result.put("orderNumber", orderNumber);
        return result;
    }

    private int number(Object value) {
        return value == null ? 0 : new BigDecimal(value.toString()).intValue();
    }

    private String generateRuleOrderNumber() {
        return "IN" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    @Override
    public List<PointsRule> selectManualFixedRules(Integer memberId, String direction) {
        if (memberId == null) throw new IllegalArgumentException("会员ID不能为空");
        String normalizedDirection = direction == null ? "" : direction.trim().toUpperCase();
        if (!"ADD".equals(normalizedDirection) && !"DEDUCT".equals(normalizedDirection)) {
            throw new IllegalArgumentException("积分方向必须是ADD或DEDUCT");
        }
        Member member = memberMapper.selectMemberById(memberId);
        if (member == null) throw new IllegalArgumentException("会员不存在");
        return pointsRuleMapper.selectManualFixedRules(normalizedDirection, member.getDeptId(), member.getCardTypeId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult adjustPointsByRule(Integer memberId, Long ruleId, Integer requestedPoints, String description,
                                         Long operatorUserId, String operator, String operationDevice) {
        if (memberId == null || ruleId == null) return AjaxResult.error("会员和积分规则不能为空");
        if (operatorUserId == null || operator == null || operator.isBlank()) {
            return AjaxResult.error("无法识别当前操作人");
        }

        Member memberView = memberMapper.selectMemberById(memberId);
        if (memberView == null) return AjaxResult.error("会员不存在");
        PointsRule rule = pointsRuleMapper.selectManualFixedRuleForUpdate(
                ruleId, memberView.getDeptId(), memberView.getCardTypeId());
        if (rule == null) return AjaxResult.error("积分规则不存在、未生效或不适用于该会员");

        int configuredPoints = rule.getFixedPoints() != null ? rule.getFixedPoints()
                : rule.getPointsPerUnit() == null ? 0 : rule.getPointsPerUnit();
        int basePoints = configuredPoints;
        if (Integer.valueOf(1).equals(rule.getManualPointsEditable())) {
            if (requestedPoints == null || requestedPoints <= 0) return AjaxResult.error("请输入大于0的积分数值");
            basePoints = requestedPoints;
        }
        if (basePoints <= 0) return AjaxResult.error("积分规则数值必须大于0");

        boolean memberDayApplied = "ADD".equals(rule.getDirection()) && isMemberDay(rule);
        BigDecimal multiplier = memberDayApplied ? rule.getMemberDayMultiplier() : BigDecimal.ONE;
        int points;
        try {
            points = BigDecimal.valueOf(basePoints).multiply(multiplier).intValueExact();
        } catch (ArithmeticException e) {
            return AjaxResult.error("会员日倍增后的积分必须为整数");
        }
        if (rule.getMaxPointsPerOrder() != null && points > rule.getMaxPointsPerOrder()) {
            return AjaxResult.error("积分数值超过规则单笔上限");
        }
        if (rule.getMemberLimit() != null
                && pointsOrderMapper.countSuccessByRuleAndMember(ruleId, memberId) >= rule.getMemberLimit()) {
            return AjaxResult.error("该会员已达到此规则的使用次数上限");
        }

        Member member = memberMapper.selectMemberByIdForUpdate(memberId);
        if (member == null) return AjaxResult.error("会员不存在");
        int beforePoints = member.getCurrentPoints() == null ? 0 : member.getCurrentPoints();
        boolean isDeduct = "DEDUCT".equals(rule.getDirection());
        if (!isDeduct && !"ADD".equals(rule.getDirection())) return AjaxResult.error("积分规则方向无效");
        if (isDeduct && beforePoints < points) return AjaxResult.error("积分不足，当前余额：" + beforePoints);
        int signedPoints = isDeduct ? -points : points;
        int afterPoints = Math.addExact(beforePoints, signedPoints);
        String orderNumber = generateOrderNumber(isDeduct ? "OT" : "IN");
        String operationDescription = description == null || description.isBlank()
                ? rule.getRuleName() : rule.getRuleName() + "：" + description.trim();

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("ruleId", rule.getId());
        snapshot.put("ruleCode", rule.getRuleCode());
        snapshot.put("ruleName", rule.getRuleName());
        snapshot.put("direction", rule.getDirection());
        snapshot.put("calculationMode", rule.getCalculationMode());
        snapshot.put("configuredPoints", configuredPoints);
        snapshot.put("basePoints", basePoints);
        snapshot.put("memberDayApplied", memberDayApplied);
        snapshot.put("multiplier", multiplier);
        snapshot.put("finalPoints", points);

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderNumber", orderNumber);
        order.put("openId", member.getCardNo() == null ? "" : member.getCardNo());
        order.put("cardNo", member.getCardNo() == null ? "" : member.getCardNo());
        order.put("memberId", memberId);
        order.put("amount", signedPoints);
        order.put("points", points);
        order.put("description", operationDescription);
        order.put("beforePoints", beforePoints);
        order.put("afterPoints", afterPoints);
        order.put("ruleId", rule.getId());
        order.put("ruleCode", rule.getRuleCode());
        order.put("ruleName", rule.getRuleName());
        order.put("sceneCode", rule.getSceneCode());
        order.put("direction", rule.getDirection());
        order.put("triggerEvent", rule.getTriggerEvent());
        order.put("operatorUserId", operatorUserId);
        order.put("operatorName", operator);
        order.put("operationDevice", operationDevice == null ? "PC" : operationDevice);
        order.put("calculationSnapshot", JSON.toJSONString(snapshot));
        if (pointsOrderMapper.insertManualRulePointsOrder(order) != 1) {
            throw new IllegalStateException("积分订单创建失败");
        }

        if (isDeduct) {
            PointsUserOutBillDetail outBill = new PointsUserOutBillDetail();
            outBill.setMemberId(memberId);
            outBill.setPoints(points);
            outBill.setRemainingPoints(afterPoints);
            outBill.setDescription(operationDescription);
            outBill.setOrderNoSrc(orderNumber);
            outBill.setActivityKey(rule.getRuleCode());
            outBill.setActivityName(rule.getRuleName());
            outBill.setEventKey(rule.getTriggerEvent());
            outBill.setEventName(rule.getRuleName());
            outBill.setAccountType(0);
            outBill.setBillStatus(0);
            outBill.setIsDel(0);
            if (outBillMapper.insertOutBill(outBill) != 1) throw new IllegalStateException("积分出账明细创建失败");
        } else {
            Calendar expiry = Calendar.getInstance();
            expiry.add(Calendar.YEAR, 1);
            PointsUserIntoBillDetail intoBill = new PointsUserIntoBillDetail();
            intoBill.setMemberId(memberId);
            intoBill.setPoints(points);
            intoBill.setRemainingPoints(points);
            intoBill.setDescription(operationDescription);
            intoBill.setOrderNoSrc(orderNumber);
            intoBill.setOrderNoTarget("");
            intoBill.setActivityKey(rule.getRuleCode());
            intoBill.setActivityName(rule.getRuleName());
            intoBill.setEventKey(rule.getTriggerEvent());
            intoBill.setEventName(rule.getRuleName());
            intoBill.setAccountType(0);
            intoBill.setBillStatus(0);
            intoBill.setIsWhiteOrder(0);
            intoBill.setExpiredTime(expiry.getTime());
            intoBill.setExpiredTimestamp(expiry.getTimeInMillis());
            intoBill.setIsDel(0);
            if (intoBillMapper.insertIntoBill(intoBill) != 1) throw new IllegalStateException("积分入账明细创建失败");
        }

        Member updateMember = new Member();
        updateMember.setId(memberId);
        updateMember.setCurrentPoints(afterPoints);
        updateMember.setLastOperator(operator);
        if (memberMapper.updateMember(updateMember) != 1) throw new IllegalStateException("会员积分余额更新失败");
        if (pointsRuleMapper.incrementUsage(ruleId, points) != 1) {
            throw new IllegalStateException("积分规则次数或预算已用完");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNumber", orderNumber);
        result.put("amount", signedPoints);
        result.put("basePoints", basePoints);
        result.put("memberDayApplied", memberDayApplied);
        result.put("multiplier", multiplier);
        result.put("afterPoints", afterPoints);
        result.put("ruleName", rule.getRuleName());
        return AjaxResult.success(isDeduct ? "积分扣减成功" : "积分添加成功", result);
    }

    private boolean isMemberDay(PointsRule rule) {
        if (!Integer.valueOf(1).equals(rule.getMemberDayEnabled())
                || rule.getMemberDayMultiplier() == null
                || rule.getMemberDayMultiplier().compareTo(BigDecimal.ZERO) <= 0
                || rule.getMemberDayDays() == null) return false;
        try {
            int today = LocalDate.now(ZoneId.of("Asia/Shanghai")).getDayOfMonth();
            List<Integer> days = JSON.parseArray(rule.getMemberDayDays(), Integer.class);
            return days != null && days.contains(today);
        } catch (Exception e) {
            throw new IllegalStateException("积分规则的会员日配置无效", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult addPoints(Integer memberId, Integer points, String description, String operator, String operationDevice) {
        // 1. 悲观锁查询会员信息，防止并发修改
        Member member = memberMapper.selectMemberByIdForUpdate(memberId);
        if (member == null) {
            return AjaxResult.error("会员不存在");
        }

        // 2. 记录操作前积分（锁内读取，保证准确性）
        int oldPoints = (member.getCurrentPoints() != null ? member.getCurrentPoints() : 0);
        int newPoints = oldPoints + points;

        // 3. 生成订单号: IN + yyyyMMddHHmmss + 6位随机数
        String orderNumber = generateOrderNumber("IN");

        // 4. 插入订单表
        PointsOrder order = new PointsOrder();
        order.setOrderNumber(orderNumber);
        order.setOpenId(member.getCardNo() != null ? member.getCardNo() : "");
        order.setCardNo(member.getCardNo() != null ? member.getCardNo() : "");
        order.setMemberId(memberId);
        order.setAppOrderNumber("");
        order.setAmount(points);
        order.setDescription(description != null ? description : "管理员添加积分");
        order.setType(1);
        order.setCustomArgs("operator:" + operator);
        order.setCompletedTime(new Date());
        order.setClientIp("127.0.0.1");
        order.setItemId("");
        order.setItemName("");
        order.setPrice(points);
        order.setDiscountedPrice(points);
        order.setOperationDevice(operationDevice != null ? operationDevice : "PC");
        order.setIsDel(0);
        order.setAmountType(2);
        order.setAppId("");
        order.setOrginPoints(oldPoints);
        order.setAfterPoints(newPoints);
        pointsOrderMapper.insertPointsOrder(order);

        // 5. 插入入账单表
        PointsUserIntoBillDetail intoBill = new PointsUserIntoBillDetail();
        intoBill.setMemberId(memberId);
        intoBill.setPoints(points);
        intoBill.setRemainingPoints(points);
        intoBill.setDescription(description != null ? description : "管理员添加积分");
        intoBill.setOrderNoSrc(orderNumber);
        intoBill.setOrderNoTarget("");
        intoBill.setActivityKey("");
        intoBill.setActivityName("");
        intoBill.setEventKey("");
        intoBill.setEventName("");
        intoBill.setAccountType(0);
        intoBill.setBillStatus(0);
        intoBill.setIsWhiteOrder(0);
        intoBill.setExpiredTime(null);
        intoBill.setExpiredTimestamp(0L);
        intoBill.setIsDel(0);
        intoBillMapper.insertIntoBill(intoBill);

        // 6. 更新会员积分余额
        Member updateMember = new Member();
        updateMember.setId(memberId);
        updateMember.setCurrentPoints(newPoints);
        updateMember.setLastOperator(operator);
        memberMapper.updateMember(updateMember);

        return AjaxResult.success("积分添加成功，订单号：" + orderNumber);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deductPoints(Integer memberId, Integer points, String description, String operator, String operationDevice) {
        // 1. 悲观锁查询会员信息
        Member member = memberMapper.selectMemberByIdForUpdate(memberId);
        if (member == null) return AjaxResult.error("会员不存在");

        int currentPoints = member.getCurrentPoints() != null ? member.getCurrentPoints() : 0;
        if (currentPoints < points) return AjaxResult.error("积分不足，当前余额：" + currentPoints);

        int oldPoints = currentPoints;
        int newPoints = oldPoints - points;

        // 2. 生成订单号 OUT
        String orderNumber = generateOrderNumber("OUT");

        // 3. 插入订单表
        PointsOrder order = new PointsOrder();
        order.setOrderNumber(orderNumber);
        order.setOpenId(member.getCardNo() != null ? member.getCardNo() : "");
        order.setCardNo(member.getCardNo() != null ? member.getCardNo() : "");
        order.setMemberId(memberId);
        order.setAppOrderNumber("");
        order.setAmount(-points);
        order.setDescription(description != null ? description : "管理员扣减积分");
        order.setType(1);
        order.setCustomArgs("operator:" + operator);
        order.setCompletedTime(new Date());
        order.setClientIp("127.0.0.1");
        order.setItemId("");
        order.setItemName("");
        order.setPrice(points);
        order.setDiscountedPrice(points);
        order.setOperationDevice(operationDevice != null ? operationDevice : "小程序");
        order.setIsDel(0);
        order.setAmountType(2);
        order.setAppId("");
        order.setOrginPoints(oldPoints);
        order.setAfterPoints(newPoints);
        pointsOrderMapper.insertPointsOrder(order);

        // 4. 插入出账单表
        PointsUserOutBillDetail outBill = new PointsUserOutBillDetail();
        outBill.setMemberId(memberId);
        outBill.setPoints(points);
        outBill.setRemainingPoints(newPoints);
        outBill.setDescription(description != null ? description : "管理员扣减积分");
        outBill.setOrderNoSrc(orderNumber);
        outBill.setActivityKey("");
        outBill.setActivityName("");
        outBill.setEventKey("");
        outBill.setEventName("");
        outBill.setAccountType(0);
        outBill.setBillStatus(0);
        outBill.setIsDel(0);
        outBillMapper.insertOutBill(outBill);

        // 5. 更新会员积分余额
        Member updateMember = new Member();
        updateMember.setId(memberId);
        updateMember.setCurrentPoints(newPoints);
        updateMember.setLastOperator(operator);
        memberMapper.updateMember(updateMember);

        return AjaxResult.success("积分扣减成功，订单号：" + orderNumber);
    }

    /**
     * 生成订单号
     * 格式: 前缀 + yyyyMMddHHmmss + 6位随机数字
     */
    private String generateOrderNumber(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        int random = (int) (Math.random() * 900000) + 100000;
        return prefix + dateStr + random;
    }

    @Override
    public List<PointsOrder> selectByMemberId(Integer memberId) {
        return pointsOrderMapper.selectByMemberId(memberId);
    }

    @Override
    public List<PointsOrder> selectPage(String phone, Integer memberId, String direction, int offset, int limit) {
        return pointsOrderMapper.selectPage(phone, memberId, direction, offset, limit);
    }

    @Override
    public long countPage(String phone, Integer memberId, String direction) {
        return pointsOrderMapper.countPage(phone, memberId, direction);
    }

    @Override
    public int sumYearEarned(Integer memberId) {
        return pointsOrderMapper.sumYearEarned(memberId);
    }

    @Override
    public PointsOrder selectByOrderNumber(String orderNumber) {
        return pointsOrderMapper.selectByOrderNumber(orderNumber);
    }
}
