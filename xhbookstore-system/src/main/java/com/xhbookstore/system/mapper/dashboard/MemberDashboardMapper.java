package com.xhbookstore.system.mapper.dashboard;

import java.util.List;

import com.xhbookstore.system.domain.dashboard.MemberDashboardDeptStats;

public interface MemberDashboardMapper {
    List<MemberDashboardDeptStats> selectDeptMemberStats();

    List<MemberDashboardDeptStats> selectDeptMemberCodeStats();

    List<MemberDashboardDeptStats> selectDeptLoginStats();

}
