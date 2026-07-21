package com.xhbookstore.system.service.member;

import java.util.List;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.PointsRule;

/** 积分规则目录与数值维护。 */
public interface IPointsRuleService {
    List<PointsRule> selectRuleList(String ruleName, String direction, String implementationStatus);

    PointsRule selectRuleById(Long id);

    AjaxResult createRule(PointsRule rule, Long operatorUserId, String operatorName);

    AjaxResult updateRule(PointsRule rule, Long operatorUserId, String operatorName);

    AjaxResult deleteRules(Long[] ids, Long operatorUserId, String operatorName);

    AjaxResult updateRulePoints(Long id, Integer fixedPoints, Integer pointsPerUnit,
                                Long operatorUserId, String operatorName);
}
