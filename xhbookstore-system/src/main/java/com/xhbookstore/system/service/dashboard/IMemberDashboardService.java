package com.xhbookstore.system.service.dashboard;

import java.util.List;
import com.xhbookstore.common.core.domain.entity.SysDept;
import com.xhbookstore.system.domain.dashboard.MemberDashboardOverview;

public interface IMemberDashboardService {
    MemberDashboardOverview getOverview(Long deptId);

    List<SysDept> getVisibleDeptOptions();

    boolean refreshStatsWithLock();
}
