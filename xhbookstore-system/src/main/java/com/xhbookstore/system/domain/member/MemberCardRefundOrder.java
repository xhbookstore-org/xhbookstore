package com.xhbookstore.system.domain.member;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xhbookstore.common.core.domain.BaseEntity;

public class MemberCardRefundOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String refundOrderNo;
    private String saleOrderNo;
    private Long memberCardId;
    private Integer memberId;
    private String memberNo;
    private String memberName;
    private String memberPhone;
    private String memberNameLike;
    private Integer cardTypeId;
    private String cardTypeName;
    private BigDecimal refundAmount;
    private String refundType;
    private Integer refundStatus;
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date refundTime;
    private Long deptId;
    private String deptName;
    private String operatorId;
    private String operatorName;
    private String remark;
    private String beginRefundTime;
    private String endRefundTime;
    private String beginCreatedAt;
    private String endCreatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    private Integer isDel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRefundOrderNo() { return refundOrderNo; }
    public void setRefundOrderNo(String refundOrderNo) { this.refundOrderNo = refundOrderNo; }
    public String getSaleOrderNo() { return saleOrderNo; }
    public void setSaleOrderNo(String saleOrderNo) { this.saleOrderNo = saleOrderNo; }
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
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public String getRefundType() { return refundType; }
    public void setRefundType(String refundType) { this.refundType = refundType; }
    public Integer getRefundStatus() { return refundStatus; }
    public void setRefundStatus(Integer refundStatus) { this.refundStatus = refundStatus; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Date getRefundTime() { return refundTime; }
    public void setRefundTime(Date refundTime) { this.refundTime = refundTime; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getBeginRefundTime() { return beginRefundTime; }
    public void setBeginRefundTime(String beginRefundTime) { this.beginRefundTime = beginRefundTime; }
    public String getEndRefundTime() { return endRefundTime; }
    public void setEndRefundTime(String endRefundTime) { this.endRefundTime = endRefundTime; }
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
