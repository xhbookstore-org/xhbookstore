package com.xhbookstore.system.domain.member;

import java.util.Date;

public class PointsUserOutBillDetail {
    private Long id;
    private Integer memberId;
    private Integer points;
    private Integer remainingPoints;
    private String description;
    private String channel;
    private String orderNoSrc;
    private String activityKey;
    private String activityName;
    private String eventKey;
    private String eventName;
    private Integer accountType;
    private Integer billStatus;
    private Integer isDel;
    private Date createdAt;
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
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getOrderNoSrc() { return orderNoSrc; }
    public void setOrderNoSrc(String orderNoSrc) { this.orderNoSrc = orderNoSrc; }
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
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
