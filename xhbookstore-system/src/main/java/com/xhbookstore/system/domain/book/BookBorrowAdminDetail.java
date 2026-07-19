package com.xhbookstore.system.domain.book;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/** Admin view of one physical-copy borrow detail. */
public class BookBorrowAdminDetail {
    private Long id;
    private String bookCode;
    private String bookName;
    private Integer borrowQty;
    private Integer borrowStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date borrowTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date returnTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;
    private String lastStaffName;
    private String remark;
    private List<BookBorrowDetailImage> images;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public Integer getBorrowQty() { return borrowQty; }
    public void setBorrowQty(Integer borrowQty) { this.borrowQty = borrowQty; }
    public Integer getBorrowStatus() { return borrowStatus; }
    public void setBorrowStatus(Integer borrowStatus) { this.borrowStatus = borrowStatus; }
    public Date getBorrowTime() { return borrowTime; }
    public void setBorrowTime(Date borrowTime) { this.borrowTime = borrowTime; }
    public Date getReturnTime() { return returnTime; }
    public void setReturnTime(Date returnTime) { this.returnTime = returnTime; }
    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }
    public String getLastStaffName() { return lastStaffName; }
    public void setLastStaffName(String lastStaffName) { this.lastStaffName = lastStaffName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<BookBorrowDetailImage> getImages() { return images; }
    public void setImages(List<BookBorrowDetailImage> images) { this.images = images; }
}
