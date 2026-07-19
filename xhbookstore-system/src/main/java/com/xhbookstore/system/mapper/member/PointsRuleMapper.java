package com.xhbookstore.system.mapper.member;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.member.PointsRule;

public interface PointsRuleMapper {
    PointsRule selectActiveForUpdate(@Param("sceneCode") String sceneCode,
                                     @Param("triggerEvent") String triggerEvent,
                                     @Param("deptId") Long deptId,
                                     @Param("cardTypeId") Integer cardTypeId);

    int incrementUsage(@Param("ruleId") Long ruleId, @Param("points") Integer points);

    List<PointsRule> selectManualFixedRules(@Param("direction") String direction,
                                            @Param("deptId") Long deptId,
                                            @Param("cardTypeId") Integer cardTypeId);

    PointsRule selectManualFixedRuleForUpdate(@Param("ruleId") Long ruleId,
                                               @Param("deptId") Long deptId,
                                               @Param("cardTypeId") Integer cardTypeId);

    List<PointsRule> selectRuleList(@Param("ruleName") String ruleName,
                                    @Param("direction") String direction,
                                    @Param("implementationStatus") String implementationStatus);

    PointsRule selectRuleById(@Param("id") Long id);

    PointsRule selectEnabledByRuleCodeForUpdate(@Param("ruleCode") String ruleCode,
                                                 @Param("deptId") Long deptId,
                                                 @Param("cardTypeId") Integer cardTypeId);

    int countRuleCode(@Param("ruleCode") String ruleCode, @Param("excludeId") Long excludeId);

    int insertRule(PointsRule rule);

    int updateRule(PointsRule rule);

    int countPointsOrdersByRuleId(@Param("ruleId") Long ruleId);

    int logicalDeleteRule(@Param("id") Long id,
                          @Param("operatorUserId") Long operatorUserId,
                          @Param("operatorName") String operatorName);

    int updateRulePoints(@Param("id") Long id,
                         @Param("fixedPoints") Integer fixedPoints,
                         @Param("pointsPerUnit") Integer pointsPerUnit,
                         @Param("operatorUserId") Long operatorUserId,
                         @Param("operatorName") String operatorName);
}
