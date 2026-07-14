package com.xhbookstore.system.domain.dashboard;

import java.io.Serializable;

public class MemberDashboardDeptStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long deptId;
    private String deptName;
    private Long totalMembers = 0L;
    private Long borrowCardMembers = 0L;
    private Long normalMembers = 0L;
    private Long unitBorrowCardMembers = 0L;
    private Long naturalBorrowCardMembers = 0L;
    private Long yearCardMembers = 0L;
    private Long unitYearCardMembers = 0L;
    private Long naturalYearCardMembers = 0L;
    private Long halfYearCardMembers = 0L;
    private Long unitHalfYearCardMembers = 0L;
    private Long naturalHalfYearCardMembers = 0L;
    private Long totalMemberCodeShowCount = 0L;
    private Long yearMemberCodeShowCount = 0L;
    private Long monthMemberCodeShowCount = 0L;
    private Long yesterdayMemberCodeShowCount = 0L;
    private Long totalLoginCount = 0L;
    private Long yearLoginCount = 0L;
    private Long monthLoginCount = 0L;
    private Long yesterdayLoginCount = 0L;

    public void add(MemberDashboardDeptStats other) {
        if (other == null) {
            return;
        }
        totalMembers += nvl(other.totalMembers);
        borrowCardMembers += nvl(other.borrowCardMembers);
        normalMembers += nvl(other.normalMembers);
        unitBorrowCardMembers += nvl(other.unitBorrowCardMembers);
        naturalBorrowCardMembers += nvl(other.naturalBorrowCardMembers);
        yearCardMembers += nvl(other.yearCardMembers);
        unitYearCardMembers += nvl(other.unitYearCardMembers);
        naturalYearCardMembers += nvl(other.naturalYearCardMembers);
        halfYearCardMembers += nvl(other.halfYearCardMembers);
        unitHalfYearCardMembers += nvl(other.unitHalfYearCardMembers);
        naturalHalfYearCardMembers += nvl(other.naturalHalfYearCardMembers);
        totalMemberCodeShowCount += nvl(other.totalMemberCodeShowCount);
        yearMemberCodeShowCount += nvl(other.yearMemberCodeShowCount);
        monthMemberCodeShowCount += nvl(other.monthMemberCodeShowCount);
        yesterdayMemberCodeShowCount += nvl(other.yesterdayMemberCodeShowCount);
        totalLoginCount += nvl(other.totalLoginCount);
        yearLoginCount += nvl(other.yearLoginCount);
        monthLoginCount += nvl(other.monthLoginCount);
        yesterdayLoginCount += nvl(other.yesterdayLoginCount);
    }

    private long nvl(Long value) {
        return value == null ? 0L : value;
    }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getTotalMembers() { return totalMembers; }
    public void setTotalMembers(Long totalMembers) { this.totalMembers = totalMembers; }
    public Long getBorrowCardMembers() { return borrowCardMembers; }
    public void setBorrowCardMembers(Long borrowCardMembers) { this.borrowCardMembers = borrowCardMembers; }
    public Long getNormalMembers() { return normalMembers; }
    public void setNormalMembers(Long normalMembers) { this.normalMembers = normalMembers; }
    public Long getUnitBorrowCardMembers() { return unitBorrowCardMembers; }
    public void setUnitBorrowCardMembers(Long unitBorrowCardMembers) { this.unitBorrowCardMembers = unitBorrowCardMembers; }
    public Long getNaturalBorrowCardMembers() { return naturalBorrowCardMembers; }
    public void setNaturalBorrowCardMembers(Long naturalBorrowCardMembers) { this.naturalBorrowCardMembers = naturalBorrowCardMembers; }
    public Long getYearCardMembers() { return yearCardMembers; }
    public void setYearCardMembers(Long yearCardMembers) { this.yearCardMembers = yearCardMembers; }
    public Long getUnitYearCardMembers() { return unitYearCardMembers; }
    public void setUnitYearCardMembers(Long unitYearCardMembers) { this.unitYearCardMembers = unitYearCardMembers; }
    public Long getNaturalYearCardMembers() { return naturalYearCardMembers; }
    public void setNaturalYearCardMembers(Long naturalYearCardMembers) { this.naturalYearCardMembers = naturalYearCardMembers; }
    public Long getHalfYearCardMembers() { return halfYearCardMembers; }
    public void setHalfYearCardMembers(Long halfYearCardMembers) { this.halfYearCardMembers = halfYearCardMembers; }
    public Long getUnitHalfYearCardMembers() { return unitHalfYearCardMembers; }
    public void setUnitHalfYearCardMembers(Long unitHalfYearCardMembers) { this.unitHalfYearCardMembers = unitHalfYearCardMembers; }
    public Long getNaturalHalfYearCardMembers() { return naturalHalfYearCardMembers; }
    public void setNaturalHalfYearCardMembers(Long naturalHalfYearCardMembers) { this.naturalHalfYearCardMembers = naturalHalfYearCardMembers; }
    public Long getTotalMemberCodeShowCount() { return totalMemberCodeShowCount; }
    public void setTotalMemberCodeShowCount(Long totalMemberCodeShowCount) { this.totalMemberCodeShowCount = totalMemberCodeShowCount; }
    public Long getYearMemberCodeShowCount() { return yearMemberCodeShowCount; }
    public void setYearMemberCodeShowCount(Long yearMemberCodeShowCount) { this.yearMemberCodeShowCount = yearMemberCodeShowCount; }
    public Long getMonthMemberCodeShowCount() { return monthMemberCodeShowCount; }
    public void setMonthMemberCodeShowCount(Long monthMemberCodeShowCount) { this.monthMemberCodeShowCount = monthMemberCodeShowCount; }
    public Long getYesterdayMemberCodeShowCount() { return yesterdayMemberCodeShowCount; }
    public void setYesterdayMemberCodeShowCount(Long yesterdayMemberCodeShowCount) { this.yesterdayMemberCodeShowCount = yesterdayMemberCodeShowCount; }
    public Long getTotalLoginCount() { return totalLoginCount; }
    public void setTotalLoginCount(Long totalLoginCount) { this.totalLoginCount = totalLoginCount; }
    public Long getYearLoginCount() { return yearLoginCount; }
    public void setYearLoginCount(Long yearLoginCount) { this.yearLoginCount = yearLoginCount; }
    public Long getMonthLoginCount() { return monthLoginCount; }
    public void setMonthLoginCount(Long monthLoginCount) { this.monthLoginCount = monthLoginCount; }
    public Long getYesterdayLoginCount() { return yesterdayLoginCount; }
    public void setYesterdayLoginCount(Long yesterdayLoginCount) { this.yesterdayLoginCount = yesterdayLoginCount; }
}
