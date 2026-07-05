package com.xhbookstore.system.domain.member;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xhbookstore.common.core.domain.BaseEntity;

public class MemberCardOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long memberCardId;
    private Integer memberId;
    private String memberNo;
    private String memberName;
    private String memberPhone;
    private String memberNameLike;
    private Integer cardTypeId;
    private String cardTypeName;
    private Integer validDays;
    private BigDecimal receivableAmount;
    private BigDecimal paidAmount;
    private String paymentType;
    private Integer orderStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;
    private Long deptId;
    private String deptName;
    private String createStaffId;
    private String createStaffName;
    private String remark;
    private String beginPayTime;
    private String endPayTime;
    private String beginCreatedAt;
    private String endCreatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    private Integer isDel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getMemberCardId() { return memberCardId; }
    public void setMemberCardId(Long memberCardId) { this.memberCardId = memberCardId; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getMemberNo() { return memberNo; }
    public void setMemberNo(String memberNo) { this.memberNo = memberNo; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
    public String getMemberNameLike() { return memberNameLike; }
    public void setMemberNameLike(String memberNameLike) { this.memberNameLike = memberNameLike; }
    public Integer getCardTypeId() { return cardTypeId; }
    public void setCardTypeId(Integer cardTypeId) { this.cardTypeId = cardTypeId; }
    public String getCardTypeName() { return cardTypeName; }
    public void setCardTypeName(String cardTypeName) { this.cardTypeName = cardTypeName; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public BigDecimal getReceivableAmount() { return receivableAmount; }
    public void setReceivableAmount(BigDecimal receivableAmount) { this.receivableAmount = receivableAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }
    public Date getPayTime() { return payTime; }
    public void setPayTime(Date payTime) { this.payTime = payTime; }
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
    public String getBeginPayTime() { return beginPayTime; }
    public void setBeginPayTime(String beginPayTime) { this.beginPayTime = beginPayTime; }
    public String getEndPayTime() { return endPayTime; }
    public void setEndPayTime(String endPayTime) { this.endPayTime = endPayTime; }
    public String getBeginCreatedAt() { return beginCreatedAt; }
    public void setBeginCreatedAt(String beginCreatedAt) { this.beginCreatedAt = beginCreatedAt; }
    public String getEndCreatedAt() { return endCreatedAt; }
    public void setEndCreatedAt(String endCreatedAt) { this.endCreatedAt = endCreatedAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
}
