package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.MemberCardRefundOrder;

public interface MemberCardRefundOrderMapper {
    int insert(MemberCardRefundOrder order);
    MemberCardRefundOrder selectByRefundOrderNo(String refundOrderNo);
    MemberCardRefundOrder selectByMemberCardId(Long memberCardId);
    List<MemberCardRefundOrder> selectList(MemberCardRefundOrder order);
}
