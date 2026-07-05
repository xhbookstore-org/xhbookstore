package com.xhbookstore.system.domain.dashboard;

import java.io.Serializable;

public class MemberDashboardLoginStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long totalLoginCount = 0L;
    private Long yearLoginCount = 0L;
    private Long monthLoginCount = 0L;
    private Long yesterdayLoginCount = 0L;

    public Long getTotalLoginCount() { return totalLoginCount; }
    public void setTotalLoginCount(Long totalLoginCount) { this.totalLoginCount = totalLoginCount; }
    public Long getYearLoginCount() { return yearLoginCount; }
    public void setYearLoginCount(Long yearLoginCount) { this.yearLoginCount = yearLoginCount; }
    public Long getMonthLoginCount() { return monthLoginCount; }
    public void setMonthLoginCount(Long monthLoginCount) { this.monthLoginCount = monthLoginCount; }
    public Long getYesterdayLoginCount() { return yesterdayLoginCount; }
    public void setYesterdayLoginCount(Long yesterdayLoginCount) { this.yesterdayLoginCount = yesterdayLoginCount; }
}
