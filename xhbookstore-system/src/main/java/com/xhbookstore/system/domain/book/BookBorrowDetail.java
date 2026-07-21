package com.xhbookstore.system.domain.book;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class BookBorrowDetail {
    private Long id;
    private Long borrowOrderId;
    private String borrowOrderNo;
    private Integer memberId;
    /** Legacy compatibility only. New borrow records do not depend on book_info. */
    private Long bookId;
    private String bookCode;
    private String bookName;
    private Integer borrowQty;
    private Integer returnedQty;
    private Integer purchaseQty;
    private Integer borrowStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date borrowTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date returnAllTime;
    private String purchaseOrderNo;
    private String remark;
    private String firstStaffId;
    private String firstStaffName;
    private String lastStaffId;
    private String lastStaffName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    private Integer isDel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBorrowOrderId() { return borrowOrderId; }
    public void setBorrowOrderId(Long borrowOrderId) { this.borrowOrderId = borrowOrderId; }
    public String getBorrowOrderNo() { return borrowOrderNo; }
    public void setBorrowOrderNo(String borrowOrderNo) { this.borrowOrderNo = borrowOrderNo; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public Integer getBorrowQty() { return borrowQty; }
    public void setBorrowQty(Integer borrowQty) { this.borrowQty = borrowQty; }
    public Integer getReturnedQty() { return returnedQty; }
    public void setReturnedQty(Integer returnedQty) { this.returnedQty = returnedQty; }
    public Integer getPurchaseQty() { return purchaseQty; }
    public void setPurchaseQty(Integer purchaseQty) { this.purchaseQty = purchaseQty; }
    public Integer getBorrowStatus() { return borrowStatus; }
    public void setBorrowStatus(Integer borrowStatus) { this.borrowStatus = borrowStatus; }
    public Date getBorrowTime() { return borrowTime; }
    public void setBorrowTime(Date borrowTime) { this.borrowTime = borrowTime; }
    public Date getReturnAllTime() { return returnAllTime; }
    public void setReturnAllTime(Date returnAllTime) { this.returnAllTime = returnAllTime; }
    public String getPurchaseOrderNo() { return purchaseOrderNo; }
    public void setPurchaseOrderNo(String purchaseOrderNo) { this.purchaseOrderNo = purchaseOrderNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
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
