package com.xhbookstore.system.domain.member;

import java.math.BigDecimal;
import java.util.Date;
import com.xhbookstore.common.annotation.Excel;
import com.xhbookstore.common.annotation.Excel.ColumnType;

public class MemberCardExport {
    @Excel(name = "会员卡记录ID", sort = 1, cellType = ColumnType.NUMERIC, width = 14)
    private Long id;

    @Excel(name = "会员编号", sort = 2, width = 18)
    private String memberNo;

    @Excel(name = "会员姓名", sort = 3, width = 14)
    private String memberName;

    @Excel(name = "手机号", sort = 4, width = 16)
    private String memberPhone;

    @Excel(name = "卡类型ID", sort = 5, cellType = ColumnType.NUMERIC, width = 12)
    private Integer cardTypeId;

    @Excel(name = "卡类型名称", sort = 6, width = 14)
    private String cardTypeName;

    @Excel(name = "有效天数", sort = 7, cellType = ColumnType.NUMERIC, width = 12)
    private Integer validDays;

    @Excel(name = "卡状态", sort = 8, width = 12)
    private String statusName;

    @Excel(name = "是否当前生效", sort = 9, width = 14)
    private String activeFlag;

    @Excel(name = "售卡订单号", sort = 10, width = 24)
    private String saleOrderNo;

    @Excel(name = "实收金额", sort = 11, cellType = ColumnType.NUMERIC, width = 12)
    private BigDecimal saleAmount;

    @Excel(name = "付款时间", sort = 12, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date paidAt;

    @Excel(name = "预计/实际生效时间", sort = 13, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveAt;

    @Excel(name = "预计/实际到期时间", sort = 14, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expiredAt;

    @Excel(name = "退款订单号", sort = 15, width = 24)
    private String refundOrderNo;

    @Excel(name = "退款时间", sort = 16, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date refundedAt;

    @Excel(name = "开卡门店ID", sort = 17, cellType = ColumnType.NUMERIC, width = 14)
    private Long deptId;

    @Excel(name = "开卡门店", sort = 18, width = 18)
    private String deptName;

    @Excel(name = "操作员工ID", sort = 19, width = 14)
    private String createStaffId;

    @Excel(name = "操作员工", sort = 20, width = 14)
    private String createStaffName;

    @Excel(name = "备注", sort = 21, width = 30)
    private String remark;

    @Excel(name = "创建时间", sort = 22, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @Excel(name = "更新时间", sort = 23, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMemberNo() { return memberNo; }
    public void setMemberNo(String memberNo) { this.memberNo = memberNo; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
    public Integer getCardTypeId() { return cardTypeId; }
    public void setCardTypeId(Integer cardTypeId) { this.cardTypeId = cardTypeId; }
    public String getCardTypeName() { return cardTypeName; }
    public void setCardTypeName(String cardTypeName) { this.cardTypeName = cardTypeName; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public String getActiveFlag() { return activeFlag; }
    public void setActiveFlag(String activeFlag) { this.activeFlag = activeFlag; }
    public String getSaleOrderNo() { return saleOrderNo; }
    public void setSaleOrderNo(String saleOrderNo) { this.saleOrderNo = saleOrderNo; }
    public BigDecimal getSaleAmount() { return saleAmount; }
    public void setSaleAmount(BigDecimal saleAmount) { this.saleAmount = saleAmount; }
    public Date getPaidAt() { return paidAt; }
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }
    public Date getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(Date effectiveAt) { this.effectiveAt = effectiveAt; }
    public Date getExpiredAt() { return expiredAt; }
    public void setExpiredAt(Date expiredAt) { this.expiredAt = expiredAt; }
    public String getRefundOrderNo() { return refundOrderNo; }
    public void setRefundOrderNo(String refundOrderNo) { this.refundOrderNo = refundOrderNo; }
    public Date getRefundedAt() { return refundedAt; }
    public void setRefundedAt(Date refundedAt) { this.refundedAt = refundedAt; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getCreateStaffId() { return createStaffId; }
    public void setCreateStaffId(String createStaffId) { this.createStaffId = createStaffId; }
    public String getCreateStaffName() { return createStaffName; }
    public void setCreateStaffName(String createStaffName) { this.createStaffName = createStaffName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
