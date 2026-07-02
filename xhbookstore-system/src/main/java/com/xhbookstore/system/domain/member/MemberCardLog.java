package com.xhbookstore.system.domain.member;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 会员开卡续费操作日志 member_card_log
 */
public class MemberCardLog {
    private Long id;
    private Integer memberId;
    private String memberNo;
    private String operationType;        // ACTIVATE=开卡 RENEW=续费
    private Integer beforeCardTypeId;
    private String beforeCardType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date beforeValidDate;
    private Integer afterCardTypeId;
    private String afterCardType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date afterValidDate;
    private String operatorId;
    private String operatorName;
    private String device;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getMemberNo() { return memberNo; }
    public void setMemberNo(String memberNo) { this.memberNo = memberNo; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public Integer getBeforeCardTypeId() { return beforeCardTypeId; }
    public void setBeforeCardTypeId(Integer beforeCardTypeId) { this.beforeCardTypeId = beforeCardTypeId; }
    public String getBeforeCardType() { return beforeCardType; }
    public void setBeforeCardType(String beforeCardType) { this.beforeCardType = beforeCardType; }
    public Date getBeforeValidDate() { return beforeValidDate; }
    public void setBeforeValidDate(Date beforeValidDate) { this.beforeValidDate = beforeValidDate; }
    public Integer getAfterCardTypeId() { return afterCardTypeId; }
    public void setAfterCardTypeId(Integer afterCardTypeId) { this.afterCardTypeId = afterCardTypeId; }
    public String getAfterCardType() { return afterCardType; }
    public void setAfterCardType(String afterCardType) { this.afterCardType = afterCardType; }
    public Date getAfterValidDate() { return afterValidDate; }
    public void setAfterValidDate(Date afterValidDate) { this.afterValidDate = afterValidDate; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
