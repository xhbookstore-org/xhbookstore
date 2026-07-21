package com.xhbookstore.system.domain.book;

import java.util.Date;
import com.xhbookstore.common.annotation.Excel;
import com.xhbookstore.common.annotation.Excel.ColumnType;

/** One physical-copy row in the Admin borrow Excel export. */
public class BookBorrowDetailExport {
    @Excel(name = "借阅单号", sort = 1, width = 24) private String orderNo;
    @Excel(name = "明细ID", sort = 2, cellType = ColumnType.NUMERIC) private Long detailId;
    @Excel(name = "借阅时间", sort = 3, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss") private Date borrowTime;
    @Excel(name = "会员编号", sort = 4, width = 18) private String memberCardNo;
    @Excel(name = "姓名", sort = 5, width = 14) private String memberName;
    @Excel(name = "手机", sort = 6, width = 16) private String memberPhone;
    @Excel(name = "会员类型", sort = 7, width = 14) private String cardTypeName;
    @Excel(name = "有效期", sort = 8, width = 14, dateFormat = "yyyy-MM-dd") private Date validDate;
    @Excel(name = "图书编号", sort = 9, width = 20) private String bookCode;
    @Excel(name = "图书名称", sort = 10, width = 30) private String bookName;
    @Excel(name = "数量", sort = 11, cellType = ColumnType.NUMERIC) private Integer quantity;
    @Excel(name = "明细状态", sort = 12, width = 14) private String detailStatusName;
    @Excel(name = "归还时间", sort = 13, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss") private Date returnTime;
    @Excel(name = "处理时间", sort = 14, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss") private Date handleTime;
    @Excel(name = "门店", sort = 15, width = 18) private String deptName;
    @Excel(name = "最后操作人", sort = 16, width = 14) private String lastStaffName;
    @Excel(name = "备注", sort = 17, width = 30) private String remark;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Date getBorrowTime() { return borrowTime; }
    public void setBorrowTime(Date borrowTime) { this.borrowTime = borrowTime; }
    public String getMemberCardNo() { return memberCardNo; }
    public void setMemberCardNo(String memberCardNo) { this.memberCardNo = memberCardNo; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
    public String getCardTypeName() { return cardTypeName; }
    public void setCardTypeName(String cardTypeName) { this.cardTypeName = cardTypeName; }
    public Date getValidDate() { return validDate; }
    public void setValidDate(Date validDate) { this.validDate = validDate; }
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getDetailStatusName() { return detailStatusName; }
    public void setDetailStatusName(String detailStatusName) { this.detailStatusName = detailStatusName; }
    public Date getReturnTime() { return returnTime; }
    public void setReturnTime(Date returnTime) { this.returnTime = returnTime; }
    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getLastStaffName() { return lastStaffName; }
    public void setLastStaffName(String lastStaffName) { this.lastStaffName = lastStaffName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
