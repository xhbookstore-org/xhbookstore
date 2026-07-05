package com.xhbookstore.system.domain.member;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class MemberCardBizLog {
    private Long id;
    private String traceId;
    private Long memberCardId;
    private String saleOrderNo;
    private String refundOrderNo;
    private Integer memberId;
    private String memberNo;
    private String logType;
    private String beforeData;
    private String afterData;
    private String changeFields;
    private String reason;
    private String operatorId;
    private String operatorName;
    private String device;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    private Integer isDel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Long getMemberCardId() { return memberCardId; }
    public void setMemberCardId(Long memberCardId) { this.memberCardId = memberCardId; }
    public String getSaleOrderNo() { return saleOrderNo; }
    public void setSaleOrderNo(String saleOrderNo) { this.saleOrderNo = saleOrderNo; }
    public String getRefundOrderNo() { return refundOrderNo; }
    public void setRefundOrderNo(String refundOrderNo) { this.refundOrderNo = refundOrderNo; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getMemberNo() { return memberNo; }
    public void setMemberNo(String memberNo) { this.memberNo = memberNo; }
    public String getLogType() { return logType; }
    public void setLogType(String logType) { this.logType = logType; }
    public String getBeforeData() { return beforeData; }
    public void setBeforeData(String beforeData) { this.beforeData = beforeData; }
    public String getAfterData() { return afterData; }
    public void setAfterData(String afterData) { this.afterData = afterData; }
    public String getChangeFields() { return changeFields; }
    public void setChangeFields(String changeFields) { this.changeFields = changeFields; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
}
