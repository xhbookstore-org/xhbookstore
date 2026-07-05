package com.xhbookstore.system.mapper.dashboard;

import java.util.List;

import com.xhbookstore.system.domain.dashboard.MemberDashboardDeptStats;
import com.xhbookstore.system.domain.dashboard.MemberDashboardLoginStats;

public interface MemberDashboardMapper {
    List<MemberDashboardDeptStats> selectDeptMemberStats();

    List<MemberDashboardDeptStats> selectDeptMemberCodeStats();

    MemberDashboardLoginStats selectLoginStats();
}
