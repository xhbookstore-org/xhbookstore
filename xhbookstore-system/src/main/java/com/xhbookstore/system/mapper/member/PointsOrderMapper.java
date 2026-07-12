package com.xhbookstore.system.mapper.member;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.member.PointsOrder;

/**
 * 书城币订单表 Mapper
 */
public interface PointsOrderMapper {
    int insertPointsOrder(PointsOrder order);
    PointsOrder selectByOrderNumber(String orderNumber);
    List<PointsOrder> selectByMemberId(Integer memberId);
    List<PointsOrder> selectPage(@Param("phone") String phone, @Param("memberId") Integer memberId,
                                 @Param("direction") String direction, @Param("offset") int offset,
                                 @Param("limit") int limit);
    long countPage(@Param("phone") String phone, @Param("memberId") Integer memberId,
                   @Param("direction") String direction);
    int sumYearEarned(@Param("memberId") Integer memberId);
}
