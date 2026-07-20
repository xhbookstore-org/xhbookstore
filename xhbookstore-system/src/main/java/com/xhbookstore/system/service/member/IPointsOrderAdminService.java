package com.xhbookstore.system.service.member;

import java.util.List;
import java.util.Map;
import com.xhbookstore.system.domain.member.PointsOrderRecord;
import com.xhbookstore.system.domain.member.PointsRule;

public interface IPointsOrderAdminService {
    List<PointsOrderRecord> selectList(PointsOrderRecord query);
    Map<String, Object> selectDetail(PointsOrderRecord query);
    List<PointsRule> selectRuleOptions();
}
