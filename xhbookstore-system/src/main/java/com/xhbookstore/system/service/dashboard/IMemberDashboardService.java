package com.xhbookstore.system.service.dashboard;

import com.xhbookstore.system.domain.dashboard.MemberDashboardOverview;

public interface IMemberDashboardService {
    MemberDashboardOverview getOverview();

    boolean refreshStatsWithLock();
}
