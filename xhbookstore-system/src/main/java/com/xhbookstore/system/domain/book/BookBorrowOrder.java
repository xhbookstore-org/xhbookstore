package com.xhbookstore.system.domain.book;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class BookBorrowOrder {
    private Long id;
    private String orderNo;
    private Integer memberId;
    private String memberCardNo;
    private String memberName;
    private String memberPhone;
    private String memberCardTypeName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date memberValidDate;
    private Integer totalBookCount;
    private Integer isFinished;
    private Integer borrowStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date borrowTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date returnAllTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedReturnTime;
    private String remark;
    private Long deptId;
    private String firstStaffId;
    private String firstStaffName;
    private String lastStaffId;
    private String lastStaffName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    private Integer isDel;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getMemberCardNo() { return memberCardNo; }
    public void setMemberCardNo(String memberCardNo) { this.memberCardNo = memberCardNo; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
    public String getMemberCardTypeName() { return memberCardTypeName; }
    public void setMemberCardTypeName(String memberCardTypeName) { this.memberCardTypeName = memberCardTypeName; }
    public Date getMemberValidDate() { return memberValidDate; }
    public void setMemberValidDate(Date memberValidDate) { this.memberValidDate = memberValidDate; }
    public Integer getTotalBookCount() { return totalBookCount; }
    public void setTotalBookCount(Integer totalBookCount) { this.totalBookCount = totalBookCount; }
    public Integer getIsFinished() { return isFinished; }
    public void setIsFinished(Integer isFinished) { this.isFinished = isFinished; }
    public Integer getBorrowStatus() { return borrowStatus; }
    public void setBorrowStatus(Integer borrowStatus) { this.borrowStatus = borrowStatus; }
    public Date getBorrowTime() { return borrowTime; }
    public void setBorrowTime(Date borrowTime) { this.borrowTime = borrowTime; }
    public Date getReturnAllTime() { return returnAllTime; }
    public void setReturnAllTime(Date returnAllTime) { this.returnAllTime = returnAllTime; }
    public Date getExpectedReturnTime() { return expectedReturnTime; }
    public void setExpectedReturnTime(Date expectedReturnTime) { this.expectedReturnTime = expectedReturnTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getFirstStaffId() { return firstStaffId; }
    public void setFirstStaffId(String firstStaffId) { this.firstStaffId = firstStaffId; }
    public String getFirstStaffName() { return firstStaffName; }
    public void setFirstStaffName(String firstStaffName) { this.firstStaffName = firstStaffName; }
    public String getLastStaffId() { return lastStaffId; }
    public void setLastStaffId(String lastStaffId) { this.lastStaffId = lastStaffId; }
    public String getLastStaffName() { return lastStaffName; }
    public void setLastStaffName(String lastStaffName) { this.lastStaffName = lastStaffName; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
}
