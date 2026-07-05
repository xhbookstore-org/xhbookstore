package com.xhbookstore.system.domain.member;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xhbookstore.common.core.domain.BaseEntity;

public class MemberCard extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer memberId;
    private String memberNo;
    private String memberName;
    private String memberPhone;
    private Integer cardTypeId;
    private String cardTypeName;
    private Integer validDays;
    private BigDecimal saleAmount;
    private String saleOrderNo;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paidAt;
    private String beginPaidAt;
    private String endPaidAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveAt;
    private String beginEffectiveAt;
    private String endEffectiveAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiredAt;
    private String beginExpiredAt;
    private String endExpiredAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date refundedAt;
    private String refundOrderNo;
    private Long deptId;
    private String deptName;
    private String createStaffId;
    private String createStaffName;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    private Integer isDel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
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
    public BigDecimal getSaleAmount() { return saleAmount; }
    public void setSaleAmount(BigDecimal saleAmount) { this.saleAmount = saleAmount; }
    public String getSaleOrderNo() { return saleOrderNo; }
    public void setSaleOrderNo(String saleOrderNo) { this.saleOrderNo = saleOrderNo; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getPaidAt() { return paidAt; }
    public void setPaidAt(Date paidAt) { this.paidAt = paidAt; }
    public String getBeginPaidAt() { return beginPaidAt; }
    public void setBeginPaidAt(String beginPaidAt) { this.beginPaidAt = beginPaidAt; }
    public String getEndPaidAt() { return endPaidAt; }
    public void setEndPaidAt(String endPaidAt) { this.endPaidAt = endPaidAt; }
    public Date getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(Date effectiveAt) { this.effectiveAt = effectiveAt; }
    public String getBeginEffectiveAt() { return beginEffectiveAt; }
    public void setBeginEffectiveAt(String beginEffectiveAt) { this.beginEffectiveAt = beginEffectiveAt; }
    public String getEndEffectiveAt() { return endEffectiveAt; }
    public void setEndEffectiveAt(String endEffectiveAt) { this.endEffectiveAt = endEffectiveAt; }
    public Date getExpiredAt() { return expiredAt; }
    public void setExpiredAt(Date expiredAt) { this.expiredAt = expiredAt; }
    public String getBeginExpiredAt() { return beginExpiredAt; }
    public void setBeginExpiredAt(String beginExpiredAt) { this.beginExpiredAt = beginExpiredAt; }
    public String getEndExpiredAt() { return endExpiredAt; }
    public void setEndExpiredAt(String endExpiredAt) { this.endExpiredAt = endExpiredAt; }
    public Date getRefundedAt() { return refundedAt; }
    public void setRefundedAt(Date refundedAt) { this.refundedAt = refundedAt; }
    public String getRefundOrderNo() { return refundOrderNo; }
    public void setRefundOrderNo(String refundOrderNo) { this.refundOrderNo = refundOrderNo; }
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
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
}
