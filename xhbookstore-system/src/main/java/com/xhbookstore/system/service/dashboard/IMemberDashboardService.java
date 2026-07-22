package com.xhbookstore.system.service.dashboard;

import com.xhbookstore.system.domain.dashboard.MemberDashboardOverview;
import com.xhbookstore.common.core.domain.entity.SysDept;
import java.util.List;

public interface IMemberDashboardService {
    MemberDashboardOverview getOverview();

    MemberDashboardOverview getOverview(Long deptId);

    List<SysDept> getVisibleDepts();

    boolean refreshStatsWithLock();
}
