package com.xhbookstore.system.domain.member;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 书城币入账单明细表 xhbs_points_user_into_bill_detail
 */
public class PointsUserIntoBillDetail {
    private Long id;
    private Integer memberId;
    private Integer points;
    private Integer remainingPoints;
    private String description;
    private String orderNoSrc;
    private String orderNoTarget;
    private String activityKey;
    private String activityName;
    private String eventKey;
    private String eventName;
    private Integer accountType;
    private Integer billStatus;
    private Integer isWhiteOrder;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiredTime;
    private Long expiredTimestamp;
    private Integer isDel;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getRemainingPoints() { return remainingPoints; }
    public void setRemainingPoints(Integer remainingPoints) { this.remainingPoints = remainingPoints; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOrderNoSrc() { return orderNoSrc; }
    public void setOrderNoSrc(String orderNoSrc) { this.orderNoSrc = orderNoSrc; }
    public String getOrderNoTarget() { return orderNoTarget; }
    public void setOrderNoTarget(String orderNoTarget) { this.orderNoTarget = orderNoTarget; }
    public String getActivityKey() { return activityKey; }
    public void setActivityKey(String activityKey) { this.activityKey = activityKey; }
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public Integer getAccountType() { return accountType; }
    public void setAccountType(Integer accountType) { this.accountType = accountType; }
    public Integer getBillStatus() { return billStatus; }
    public void setBillStatus(Integer billStatus) { this.billStatus = billStatus; }
    public Integer getIsWhiteOrder() { return isWhiteOrder; }
    public void setIsWhiteOrder(Integer isWhiteOrder) { this.isWhiteOrder = isWhiteOrder; }
    public Date getExpiredTime() { return expiredTime; }
    public void setExpiredTime(Date expiredTime) { this.expiredTime = expiredTime; }
    public Long getExpiredTimestamp() { return expiredTimestamp; }
    public void setExpiredTimestamp(Long expiredTimestamp) { this.expiredTimestamp = expiredTimestamp; }
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
