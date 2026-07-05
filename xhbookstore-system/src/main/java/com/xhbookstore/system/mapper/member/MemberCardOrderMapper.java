package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.MemberCardOrder;

public interface MemberCardOrderMapper {
    int insert(MemberCardOrder order);
    int bindMemberCard(MemberCardOrder order);
    MemberCardOrder selectByOrderNo(String orderNo);
    MemberCardOrder selectByMemberCardId(Long memberCardId);
    List<MemberCardOrder> selectList(MemberCardOrder order);
}
