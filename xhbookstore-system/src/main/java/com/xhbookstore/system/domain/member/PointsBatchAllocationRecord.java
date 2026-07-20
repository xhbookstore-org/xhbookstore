package com.xhbookstore.system.domain.member;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/** 一次出账对一个入账批次的积分核销明细。 */
public class PointsBatchAllocationRecord {
    private Long id;
    private Long intoBillId;
    private Long outBillId;
    private Integer points;
    private String intoOrderNo;
    private String sourceDescription;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date effectiveTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date expiredTime;
    private Integer batchOriginalPoints;
    private Integer batchRemainingPoints;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIntoBillId() { return intoBillId; }
    public void setIntoBillId(Long intoBillId) { this.intoBillId = intoBillId; }
    public Long getOutBillId() { return outBillId; }
    public void setOutBillId(Long outBillId) { this.outBillId = outBillId; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getIntoOrderNo() { return intoOrderNo; }
    public void setIntoOrderNo(String intoOrderNo) { this.intoOrderNo = intoOrderNo; }
    public String getSourceDescription() { return sourceDescription; }
    public void setSourceDescription(String sourceDescription) { this.sourceDescription = sourceDescription; }
    public Date getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(Date effectiveTime) { this.effectiveTime = effectiveTime; }
    public Date getExpiredTime() { return expiredTime; }
    public void setExpiredTime(Date expiredTime) { this.expiredTime = expiredTime; }
    public Integer getBatchOriginalPoints() { return batchOriginalPoints; }
    public void setBatchOriginalPoints(Integer batchOriginalPoints) { this.batchOriginalPoints = batchOriginalPoints; }
    public Integer getBatchRemainingPoints() { return batchRemainingPoints; }
    public void setBatchRemainingPoints(Integer batchRemainingPoints) { this.batchRemainingPoints = batchRemainingPoints; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
