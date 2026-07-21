package com.xhbookstore.system.domain.book;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class BookReturnDetail {
    private Long id;
    private String returnOrderNo;
    private Long borrowOrderId;
    private String borrowOrderNo;
    private Long borrowDetailId;
    private Integer memberId;
    /** Legacy compatibility only. */
    private Long bookId;
    private String bookCode;
    private String bookName;
    private Integer returnQty;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date returnTime;
    private Integer returnType;
    private String remark;
    private Long deptId;
    private String staffId;
    private String staffName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    private Integer isDel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReturnOrderNo() { return returnOrderNo; }
    public void setReturnOrderNo(String returnOrderNo) { this.returnOrderNo = returnOrderNo; }
    public Long getBorrowOrderId() { return borrowOrderId; }
    public void setBorrowOrderId(Long borrowOrderId) { this.borrowOrderId = borrowOrderId; }
    public String getBorrowOrderNo() { return borrowOrderNo; }
    public void setBorrowOrderNo(String borrowOrderNo) { this.borrowOrderNo = borrowOrderNo; }
    public Long getBorrowDetailId() { return borrowDetailId; }
    public void setBorrowDetailId(Long borrowDetailId) { this.borrowDetailId = borrowDetailId; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public Integer getReturnQty() { return returnQty; }
    public void setReturnQty(Integer returnQty) { this.returnQty = returnQty; }
    public Date getReturnTime() { return returnTime; }
    public void setReturnTime(Date returnTime) { this.returnTime = returnTime; }
    public Integer getReturnType() { return returnType; }
    public void setReturnType(Integer returnType) { this.returnType = returnType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
}
