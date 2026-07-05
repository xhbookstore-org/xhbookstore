package com.xhbookstore.system.domain.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class MemberDashboardOverview implements Serializable {
    private static final long serialVersionUID = 1L;

    private MemberDashboardDeptStats stats = new MemberDashboardDeptStats();
    private MemberDashboardLoginStats loginStats = new MemberDashboardLoginStats();
    private List<Long> visibleDeptIds = new ArrayList<>();
    private Integer visibleDeptCount = 0;
    private Integer missingDeptCount = 0;
    private String scopeName;
    private String loginStatsScopeNote;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date refreshedAt;
    private String refreshStatus;

    public MemberDashboardDeptStats getStats() { return stats; }
    public void setStats(MemberDashboardDeptStats stats) { this.stats = stats; }
    public MemberDashboardLoginStats getLoginStats() { return loginStats; }
    public void setLoginStats(MemberDashboardLoginStats loginStats) { this.loginStats = loginStats; }
    public List<Long> getVisibleDeptIds() { return visibleDeptIds; }
    public void setVisibleDeptIds(List<Long> visibleDeptIds) { this.visibleDeptIds = visibleDeptIds; }
    public Integer getVisibleDeptCount() { return visibleDeptCount; }
    public void setVisibleDeptCount(Integer visibleDeptCount) { this.visibleDeptCount = visibleDeptCount; }
    public Integer getMissingDeptCount() { return missingDeptCount; }
    public void setMissingDeptCount(Integer missingDeptCount) { this.missingDeptCount = missingDeptCount; }
    public String getScopeName() { return scopeName; }
    public void setScopeName(String scopeName) { this.scopeName = scopeName; }
    public String getLoginStatsScopeNote() { return loginStatsScopeNote; }
    public void setLoginStatsScopeNote(String loginStatsScopeNote) { this.loginStatsScopeNote = loginStatsScopeNote; }
    public Date getRefreshedAt() { return refreshedAt; }
    public void setRefreshedAt(Date refreshedAt) { this.refreshedAt = refreshedAt; }
    public String getRefreshStatus() { return refreshStatus; }
    public void setRefreshStatus(String refreshStatus) { this.refreshStatus = refreshStatus; }
}
