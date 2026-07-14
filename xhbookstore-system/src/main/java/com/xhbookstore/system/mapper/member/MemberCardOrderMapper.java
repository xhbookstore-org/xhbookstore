package com.xhbookstore.system.mapper.member;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.member.MemberCardOrder;

public interface MemberCardOrderMapper {
    int insert(MemberCardOrder order);
    int bindMemberCard(MemberCardOrder order);
    int updateOrderStatusByMemberCardId(@Param("memberCardId") Long memberCardId,
                                        @Param("orderStatus") Integer orderStatus);
    MemberCardOrder selectByOrderNo(String orderNo);
    MemberCardOrder selectByMemberCardId(Long memberCardId);
    List<MemberCardOrder> selectList(MemberCardOrder order);
}
