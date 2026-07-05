package com.xhbookstore.system.service.member;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.MemberCard;

public interface IMemberCardService {
    AjaxResult buyCardByToken(String memberCodeToken, Integer cardTypeId, BigDecimal paidAmount,
                              String paymentType, String staffId, String staffName, Long deptId, String remark);
    AjaxResult buyCard(Integer memberId, Integer cardTypeId, BigDecimal paidAmount,
                       String paymentType, String staffId, String staffName, Long deptId, String remark);
    AjaxResult refundCard(Long memberCardId, BigDecimal refundAmount, String refundType,
                          String reason, String operatorId, String operatorName, Long deptId, String remark);
    List<MemberCard> refreshMemberCardStatus(Integer memberId, String operatorId, String operatorName, String device);
    Map<String, Object> getMemberCardView(Integer memberId);
}
