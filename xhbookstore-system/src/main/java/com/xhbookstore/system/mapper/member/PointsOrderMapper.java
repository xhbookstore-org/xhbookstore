package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.PointsOrder;

/**
 * 书城币订单表 Mapper
 */
public interface PointsOrderMapper {
    int insertPointsOrder(PointsOrder order);
    PointsOrder selectByOrderNumber(String orderNumber);
    List<PointsOrder> selectByMemberId(Integer memberId);
}
