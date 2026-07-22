package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.MemberCardOrder;

public interface MemberCardOrderMapper {
    int insert(MemberCardOrder order);
    int bindMemberCard(MemberCardOrder order);
    MemberCardOrder selectByOrderNo(String orderNo);
    MemberCardOrder selectByMemberCardId(Long memberCardId);
    int updateOrderStatusByMemberCardId(@org.apache.ibatis.annotations.Param("memberCardId") Long memberCardId,
                                        @org.apache.ibatis.annotations.Param("orderStatus") Integer orderStatus);
    List<MemberCardOrder> selectList(MemberCardOrder order);
}
