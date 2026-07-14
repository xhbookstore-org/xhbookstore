package com.xhbookstore.system.service.member.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberCard;
import com.xhbookstore.system.domain.member.MemberCardBizLog;
import com.xhbookstore.system.domain.member.MemberCardOrder;
import com.xhbookstore.system.domain.member.MemberCardRefundOrder;
import com.xhbookstore.system.mapper.member.CardTypeMapper;
import com.xhbookstore.system.mapper.member.MemberCardBizLogMapper;
import com.xhbookstore.system.mapper.member.MemberCardMapper;
import com.xhbookstore.system.mapper.member.MemberCardOrderMapper;
import com.xhbookstore.system.mapper.member.MemberCardRefundOrderMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.member.IMemberCardService;
import com.xhbookstore.system.service.member.IMemberCodeTokenService;

@Service
public class MemberCardServiceImpl implements IMemberCardService {
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_EXPIRED = 2;
    private static final int STATUS_REFUNDED = 3;
    private static final long REFUND_WINDOW_MS = 7L * 24 * 60 * 60 * 1000;

    @Autowired private IMemberCodeTokenService memberCodeTokenService;
    @Autowired private MemberMapper memberMapper;
    @Autowired private CardTypeMapper cardTypeMapper;
    @Autowired private MemberCardMapper memberCardMapper;
    @Autowired private MemberCardOrderMapper orderMapper;
    @Autowired private MemberCardRefundOrderMapper refundOrderMapper;
    @Autowired private MemberCardBizLogMapper logMapper;

    @Override
    @Transactional
    public AjaxResult buyCardByToken(String memberCodeToken, Integer cardTypeId, BigDecimal paidAmount,
                                     String paymentType, String staffId, String staffName, Long deptId, String remark) {
        Member tokenMember = memberCodeTokenService.verifyToken(memberCodeToken, "BUY_CARD");
        return buyCard(tokenMember.getId(), cardTypeId, paidAmount, paymentType, staffId, staffName, deptId, remark);
    }

    @Override
    @Transactional
    public AjaxResult buyCard(Integer memberId, Integer cardTypeId, BigDecimal paidAmount,
                              String paymentType, String staffId, String staffName, Long deptId, String remark) {
        if (cardTypeId == null) return AjaxResult.error("cardTypeId is required");
        CardType cardType = cardTypeMapper.selectById(cardTypeId);
        if (cardType == null || cardType.getValidDays() == null || cardType.getValidDays() <= 0) {
            return AjaxResult.error("Invalid card type");
        }
        Member member = memberMapper.selectMemberByIdForUpdate(memberId);
        if (member == null) return AjaxResult.error("Member not found");
        refreshMemberCardStatus(memberId, staffId, staffName, "STAFF_MP");

        Date now = new Date();
        String orderNo = generateOrderNo("HYS");
        BigDecimal receivable = cardType.getPrice() != null ? cardType.getPrice() : BigDecimal.ZERO;
        BigDecimal paid = paidAmount != null ? paidAmount : receivable;

        MemberCardOrder order = new MemberCardOrder();
        order.setOrderNo(orderNo);
        order.setMemberId(member.getId());
        order.setMemberNo(member.getCardNo());
        order.setMemberName(member.getName());
        order.setMemberPhone(member.getPhone());
        order.setCardTypeId(cardType.getId());
        order.setCardTypeName(cardType.getTypeName());
        order.setValidDays(cardType.getValidDays());
        order.setReceivableAmount(receivable);
        order.setPaidAmount(paid);
        order.setPaymentType(paymentType != null ? paymentType : "STAFF_CASH");
        order.setOrderStatus(1);
        order.setPayTime(now);
        order.setDeptId(deptId != null ? deptId : member.getDeptId());
        order.setCreateStaffId(staffId);
        order.setCreateStaffName(staffName);
        order.setRemark(remark);
        orderMapper.insert(order);

        MemberCard active = memberCardMapper.selectActiveByMemberIdForUpdate(memberId);
        boolean activateNow = active == null;
        Date effectiveAt = activateNow ? now : nextCardStartAt(memberId, active, now);
        Date expiredAt = addDays(effectiveAt, cardType.getValidDays());

        MemberCard card = new MemberCard();
        card.setMemberId(member.getId());
        card.setMemberNo(member.getCardNo());
        card.setCardTypeId(cardType.getId());
        card.setCardTypeName(cardType.getTypeName());
        card.setValidDays(cardType.getValidDays());
        card.setSaleAmount(paid);
        card.setSaleOrderNo(orderNo);
        card.setStatus(activateNow ? STATUS_ACTIVE : STATUS_PENDING);
        card.setPaidAt(now);
        card.setEffectiveAt(effectiveAt);
        card.setExpiredAt(expiredAt);
        card.setDeptId(order.getDeptId());
        card.setCreateStaffId(staffId);
        card.setCreateStaffName(staffName);
        card.setRemark(remark);
        memberCardMapper.insert(card);
        rebuildPendingSchedule(member.getId(), staffId, staffName, "STAFF_MP");

        if (remark != null && !remark.trim().isEmpty()) {
            Member remarkUpdate = new Member();
            remarkUpdate.setId(member.getId());
            remarkUpdate.setRemark(remark.trim());
            remarkUpdate.setLastOperator(staffName);
            memberMapper.updateMember(remarkUpdate);
        }

        order.setMemberCardId(card.getId());
        orderMapper.bindMemberCard(order);
        if (activateNow) {
            syncMemberCurrentCard(member.getId(), card, staffName);
        }
        writeLog(card, "BUY_CARD", null, card, "sale_order,member_card",
                "Buy member card", staffId, staffName, "STAFF_MP", null);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("saleOrderNo", orderNo);
        data.put("memberCardId", card.getId());
        data.put("memberId", member.getId());
        data.put("memberNo", member.getCardNo());
        data.put("memberName", member.getName());
        data.put("cardTypeId", cardType.getId());
        data.put("cardTypeName", cardType.getTypeName());
        data.put("cardStatus", card.getStatus());
        data.put("paidAmount", paid);
        data.put("effectiveAt", millis(effectiveAt));
        data.put("expiredAt", millis(expiredAt));
        data.put("expectedEffectiveAt", millis(effectiveAt));
        data.put("expectedExpiredAt", millis(expiredAt));
        data.put("remark", remark);
        return AjaxResult.success(data);
    }

    @Override
    @Transactional
    public AjaxResult refundCard(Long memberCardId, BigDecimal refundAmount, String refundType,
                                 String reason, String operatorId, String operatorName, Long deptId, String remark) {
        if (memberCardId == null) return AjaxResult.error("memberCardId is required");
        MemberCard card = memberCardMapper.selectByIdForUpdate(memberCardId);
        if (card == null) return AjaxResult.error("Member card not found");
        refreshMemberCardStatus(card.getMemberId(), operatorId, operatorName, "ADMIN");
        card = memberCardMapper.selectByIdForUpdate(memberCardId);
        if (card == null) return AjaxResult.error("Member card not found");
        if (STATUS_REFUNDED == safeStatus(card) || STATUS_EXPIRED == safeStatus(card)) {
            return AjaxResult.error("Card cannot be refunded");
        }
        if (card.getPaidAt() == null || System.currentTimeMillis() - card.getPaidAt().getTime() > REFUND_WINDOW_MS) {
            return AjaxResult.error("Refund window expired");
        }
        if (refundOrderMapper.selectByMemberCardId(memberCardId) != null) {
            return AjaxResult.error("Card has already been refunded");
        }
        BigDecimal amount = refundAmount != null ? refundAmount : card.getSaleAmount();
        String refundOrderNo = generateOrderNo("HYT");
        MemberCard before = copyCard(card);

        MemberCardRefundOrder refund = new MemberCardRefundOrder();
        refund.setRefundOrderNo(refundOrderNo);
        refund.setSaleOrderNo(card.getSaleOrderNo());
        refund.setMemberCardId(card.getId());
        refund.setMemberId(card.getMemberId());
        refund.setMemberNo(card.getMemberNo());
        refund.setCardTypeId(card.getCardTypeId());
        refund.setCardTypeName(card.getCardTypeName());
        refund.setRefundAmount(amount);
        refund.setRefundType(refundType != null ? refundType : "STAFF_REFUND");
        refund.setRefundStatus(1);
        refund.setReason(reason);
        refund.setRefundTime(new Date());
        refund.setDeptId(deptId != null ? deptId : card.getDeptId());
        refund.setOperatorId(operatorId);
        refund.setOperatorName(operatorName);
        refund.setRemark(remark);
        refundOrderMapper.insert(refund);
        memberCardMapper.refund(card.getId(), refundOrderNo);
        orderMapper.updateOrderStatusByMemberCardId(card.getId(), 2);

        MemberCard after = memberCardMapper.selectById(card.getId());
        writeLog(after, "REFUND_CARD", before, after, "status,refund_order_no,refunded_at",
                reason, operatorId, operatorName, "ADMIN", refundOrderNo);
        rebuildPendingSchedule(card.getMemberId(), operatorId, operatorName, "ADMIN");
        refreshMemberCardStatus(card.getMemberId(), operatorId, operatorName, "ADMIN");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("refundOrderNo", refundOrderNo);
        data.put("saleOrderNo", card.getSaleOrderNo());
        data.put("memberCardId", card.getId());
        data.put("refundAmount", amount);
        data.put("refundedAt", System.currentTimeMillis());
        return AjaxResult.success(data);
    }

    @Override
    @Transactional
    public List<MemberCard> refreshMemberCardStatus(Integer memberId, String operatorId, String operatorName, String device) {
        Member member = memberMapper.selectMemberByIdForUpdate(memberId);
        if (member == null) return java.util.Collections.emptyList();
        Date now = new Date();
        MemberCard active = memberCardMapper.selectActiveByMemberIdForUpdate(memberId);
        Date nextStart = now;
        if (active != null && active.getExpiredAt() != null && active.getExpiredAt().after(now)) {
            if (!sameCurrentCard(member, active)) {
                syncMemberCurrentCard(memberId, active, operatorName);
            }
            rebuildPendingSchedule(memberId, operatorId, operatorName, device);
            return memberCardMapper.selectByMemberId(memberId);
        }
        if (active != null) {
            MemberCard before = copyCard(active);
            nextStart = active.getExpiredAt() != null ? active.getExpiredAt() : now;
            memberCardMapper.expire(active.getId());
            MemberCard after = memberCardMapper.selectById(active.getId());
            writeLog(after, "EXPIRE_CARD", before, after, "status",
                    "Active card expired", operatorId, operatorName, device, null);
        }
        boolean hasActiveCard = false;
        while (true) {
            MemberCard pending = memberCardMapper.selectNextPendingByMemberIdForUpdate(memberId);
            if (pending == null) {
                break;
            }
            if (pending.getValidDays() == null || pending.getValidDays() <= 0) {
                break;
            }
            Date effectiveAt = nextStart;
            Date expiredAt = addDays(effectiveAt, pending.getValidDays());
            MemberCard before = copyCard(pending);
            memberCardMapper.activate(pending.getId(), effectiveAt, expiredAt);
            MemberCard after = memberCardMapper.selectById(pending.getId());
            writeLog(after, "ACTIVATE_CARD", before, after, "status,effective_at,expired_at",
                    "Activate next pending card", operatorId, operatorName, device, null);

            if (expiredAt.after(now)) {
                syncMemberCurrentCard(memberId, after, operatorName);
                hasActiveCard = true;
                break;
            }

            MemberCard beforeExpire = copyCard(after);
            nextStart = expiredAt;
            memberCardMapper.expire(after.getId());
            MemberCard expired = memberCardMapper.selectById(after.getId());
            writeLog(expired, "EXPIRE_CARD", beforeExpire, expired, "status",
                    "Activated card already expired during refresh", operatorId, operatorName, device, null);
        }
        if (!hasActiveCard) {
            memberMapper.updateCurrentCard(memberId, null, null, 0, operatorName);
        }
        rebuildPendingSchedule(memberId, operatorId, operatorName, device);
        return memberCardMapper.selectByMemberId(memberId);
    }

    @Override
    @Transactional
    public Map<String, Object> getMemberCardView(Integer memberId) {
        List<MemberCard> cards = refreshMemberCardStatus(memberId, "system", "system", "QUERY");
        Map<String, Object> data = new HashMap<>();
        data.put("cards", cards);
        MemberCard active = null;
        for (MemberCard card : cards) {
            if (STATUS_ACTIVE == safeStatus(card)) {
                active = card;
                break;
            }
        }
        data.put("activeCard", active);
        data.put("hasActiveCard", active != null);
        return data;
    }

    private void syncMemberCurrentCard(Integer memberId, MemberCard card, String operatorName) {
        memberMapper.updateCurrentCard(memberId, card.getCardTypeId(), card.getExpiredAt(), 0, operatorName);
    }

    private void writeLog(MemberCard card, String logType, Object beforeData, Object afterData, String fields,
                          String reason, String operatorId, String operatorName, String device, String refundOrderNo) {
        MemberCardBizLog log = new MemberCardBizLog();
        log.setTraceId(UUID.randomUUID().toString().replace("-", ""));
        log.setMemberCardId(card != null ? card.getId() : null);
        log.setSaleOrderNo(card != null ? card.getSaleOrderNo() : null);
        log.setRefundOrderNo(refundOrderNo);
        log.setMemberId(card != null ? card.getMemberId() : null);
        log.setMemberNo(card != null ? card.getMemberNo() : null);
        log.setLogType(logType);
        log.setBeforeData(beforeData != null ? JSON.toJSONString(beforeData) : null);
        log.setAfterData(afterData != null ? JSON.toJSONString(afterData) : null);
        log.setChangeFields(fields);
        log.setReason(reason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setDevice(device);
        logMapper.insert(log);
    }

    private MemberCard copyCard(MemberCard src) {
        if (src == null) return null;
        MemberCard c = new MemberCard();
        c.setId(src.getId());
        c.setMemberId(src.getMemberId());
        c.setMemberNo(src.getMemberNo());
        c.setCardTypeId(src.getCardTypeId());
        c.setCardTypeName(src.getCardTypeName());
        c.setValidDays(src.getValidDays());
        c.setSaleAmount(src.getSaleAmount());
        c.setSaleOrderNo(src.getSaleOrderNo());
        c.setStatus(src.getStatus());
        c.setPaidAt(src.getPaidAt());
        c.setEffectiveAt(src.getEffectiveAt());
        c.setExpiredAt(src.getExpiredAt());
        c.setRefundedAt(src.getRefundedAt());
        c.setRefundOrderNo(src.getRefundOrderNo());
        c.setDeptId(src.getDeptId());
        c.setCreateStaffId(src.getCreateStaffId());
        c.setCreateStaffName(src.getCreateStaffName());
        c.setRemark(src.getRemark());
        return c;
    }

    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }

    private Date nextCardStartAt(Integer memberId, MemberCard active, Date now) {
        Date startAt = active != null && active.getExpiredAt() != null && active.getExpiredAt().after(now)
                ? active.getExpiredAt()
                : now;
        List<MemberCard> cards = memberCardMapper.selectByMemberId(memberId);
        if (cards == null) return startAt;
        for (MemberCard card : cards) {
            if (STATUS_PENDING == safeStatus(card) && card.getExpiredAt() != null && card.getExpiredAt().after(startAt)) {
                startAt = card.getExpiredAt();
            }
        }
        return startAt;
    }

    private void rebuildPendingSchedule(Integer memberId, String operatorId, String operatorName, String device) {
        Date cursor = new Date();
        MemberCard active = memberCardMapper.selectActiveByMemberIdForUpdate(memberId);
        if (active != null && active.getExpiredAt() != null && active.getExpiredAt().after(cursor)) {
            cursor = active.getExpiredAt();
        }
        List<MemberCard> pendingCards = memberCardMapper.selectPendingByMemberIdForUpdate(memberId);
        if (pendingCards == null || pendingCards.isEmpty()) {
            return;
        }
        for (MemberCard pending : pendingCards) {
            if (pending.getValidDays() == null || pending.getValidDays() <= 0) {
                continue;
            }
            Date effectiveAt = cursor;
            Date expiredAt = addDays(effectiveAt, pending.getValidDays());
            boolean changed = !sameTime(pending.getEffectiveAt(), effectiveAt) || !sameTime(pending.getExpiredAt(), expiredAt);
            if (changed) {
                MemberCard before = copyCard(pending);
                memberCardMapper.updateSchedule(pending.getId(), effectiveAt, expiredAt);
                MemberCard after = memberCardMapper.selectById(pending.getId());
                writeLog(after, "REBUILD_CARD_QUEUE", before, after, "effective_at,expired_at",
                        "Rebuild pending member card schedule", operatorId, operatorName, device, null);
            }
            cursor = expiredAt;
        }
    }

    private boolean sameTime(Date left, Date right) {
        if (left == null && right == null) return true;
        if (left == null || right == null) return false;
        return left.getTime() == right.getTime();
    }

    private boolean sameCurrentCard(Member member, MemberCard card) {
        return java.util.Objects.equals(member.getCardTypeId(), card.getCardTypeId())
                && sameTime(member.getValidDate(), card.getExpiredAt())
                && (member.getStatus() == null || member.getStatus() == 0);
    }

    private Long millis(Date date) {
        return date != null ? date.getTime() : null;
    }

    private int safeStatus(MemberCard card) {
        return card != null && card.getStatus() != null ? card.getStatus() : -1;
    }

    private String generateOrderNo(String prefix) {
        return prefix + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())
                + String.format("%04d", (int) (Math.random() * 10000));
    }
}
