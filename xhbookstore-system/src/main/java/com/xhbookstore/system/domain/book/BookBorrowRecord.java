package com.xhbookstore.system.domain.book;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xhbookstore.common.core.domain.BaseEntity;

/** Admin borrow-order list row and query conditions. */
public class BookBorrowRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Integer memberId;
    private String memberName;
    private String memberCardNo;
    private String memberPhone;
    private String cardTypeName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date validDate;
    private Integer totalBookCount;
    private Integer borrowStatus;
    /** NOT_RETURNED / PARTIAL_RETURNED / ALL_RETURNED，按逐册归还明细汇总。 */
    private String returnStatus;
    /** 本借阅单已成功发放的积分合计。 */
    private Integer points;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date borrowTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastReturnTime;
    private String remark;
    private Long deptId;
    private String deptName;
    private String lastStaffName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    // Query-only fields.
    private String bookCode;
    private String bookName;
    private String handlingType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberCardNo() { return memberCardNo; }
    public void setMemberCardNo(String memberCardNo) { this.memberCardNo = memberCardNo; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
    public String getCardTypeName() { return cardTypeName; }
    public void setCardTypeName(String cardTypeName) { this.cardTypeName = cardTypeName; }
    public Date getValidDate() { return validDate; }
    public void setValidDate(Date validDate) { this.validDate = validDate; }
    public Integer getTotalBookCount() { return totalBookCount; }
    public void setTotalBookCount(Integer totalBookCount) { this.totalBookCount = totalBookCount; }
    public Integer getBorrowStatus() { return borrowStatus; }
    public void setBorrowStatus(Integer borrowStatus) { this.borrowStatus = borrowStatus; }
    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Date getBorrowTime() { return borrowTime; }
    public void setBorrowTime(Date borrowTime) { this.borrowTime = borrowTime; }
    public Date getLastReturnTime() { return lastReturnTime; }
    public void setLastReturnTime(Date lastReturnTime) { this.lastReturnTime = lastReturnTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getLastStaffName() { return lastStaffName; }
    public void setLastStaffName(String lastStaffName) { this.lastStaffName = lastStaffName; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public String getHandlingType() { return handlingType; }
    public void setHandlingType(String handlingType) { this.handlingType = handlingType; }
}
