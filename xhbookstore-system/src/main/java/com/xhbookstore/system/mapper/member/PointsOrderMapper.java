package com.xhbookstore.system.mapper.member;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.member.PointsOrder;

/**
 * 书城币订单表 Mapper
 */
public interface PointsOrderMapper {
    int insertPointsOrder(PointsOrder order);
    int insertRulePointsOrder(Map<String, Object> order);
    int insertManualRulePointsOrder(Map<String, Object> order);
    int insertFrozenCardPointsOrder(Map<String, Object> order);
    int insertFrozenCardReversalOrder(Map<String, Object> order);
    List<Long> selectPendingCardLifecycleIds(@Param("limit") int limit);
    Map<String, Object> selectCardLifecycleForUpdate(@Param("id") Long id);
    int markFrozenAvailable(@Param("id") Long id);
    int markFrozenCancelled(@Param("id") Long id);
    Map<String, Object> selectByBusinessKey(@Param("businessKey") String businessKey);
    int countSuccessByRuleAndMember(@Param("ruleId") Long ruleId, @Param("memberId") Integer memberId);
    PointsOrder selectByOrderNumber(String orderNumber);
    List<PointsOrder> selectByMemberId(Integer memberId);
    List<PointsOrder> selectPage(@Param("phone") String phone, @Param("memberId") Integer memberId,
                                 @Param("direction") String direction, @Param("offset") int offset,
                                 @Param("limit") int limit);
    long countPage(@Param("phone") String phone, @Param("memberId") Integer memberId,
                   @Param("direction") String direction);
    int sumYearEarned(@Param("memberId") Integer memberId);
}
