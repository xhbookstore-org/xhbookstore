package com.xhbookstore.system.domain.member;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 会员扩展表 member_ext
 */
public class MemberExt {
    private Integer id;
    private Integer memberId;
    private String gender;
    private Integer age;
    private String unitPhone;
    private String wechat;
    private String weibo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date joinDate;
    private Integer totalPoints;
    private Integer levelPoints;
    private java.math.BigDecimal discount;
    private java.math.BigDecimal totalPurchaseAmount;
    private Integer totalPurchaseCount;
    private Integer totalPurchaseTimes;
    private String superiorName;
    private java.math.BigDecimal superiorPointsRatio;
    private String businessStaffName;
    private String excelRawData;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getUnitPhone() { return unitPhone; }
    public void setUnitPhone(String unitPhone) { this.unitPhone = unitPhone; }
    public String getWechat() { return wechat; }
    public void setWechat(String wechat) { this.wechat = wechat; }
    public String getWeibo() { return weibo; }
    public void setWeibo(String weibo) { this.weibo = weibo; }
    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }
    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }
    public Integer getLevelPoints() { return levelPoints; }
    public void setLevelPoints(Integer levelPoints) { this.levelPoints = levelPoints; }
    public java.math.BigDecimal getDiscount() { return discount; }
    public void setDiscount(java.math.BigDecimal discount) { this.discount = discount; }
    public java.math.BigDecimal getTotalPurchaseAmount() { return totalPurchaseAmount; }
    public void setTotalPurchaseAmount(java.math.BigDecimal totalPurchaseAmount) { this.totalPurchaseAmount = totalPurchaseAmount; }
    public Integer getTotalPurchaseCount() { return totalPurchaseCount; }
    public void setTotalPurchaseCount(Integer totalPurchaseCount) { this.totalPurchaseCount = totalPurchaseCount; }
    public Integer getTotalPurchaseTimes() { return totalPurchaseTimes; }
    public void setTotalPurchaseTimes(Integer totalPurchaseTimes) { this.totalPurchaseTimes = totalPurchaseTimes; }
    public String getSuperiorName() { return superiorName; }
    public void setSuperiorName(String superiorName) { this.superiorName = superiorName; }
    public java.math.BigDecimal getSuperiorPointsRatio() { return superiorPointsRatio; }
    public void setSuperiorPointsRatio(java.math.BigDecimal superiorPointsRatio) { this.superiorPointsRatio = superiorPointsRatio; }
    public String getBusinessStaffName() { return businessStaffName; }
    public void setBusinessStaffName(String businessStaffName) { this.businessStaffName = businessStaffName; }
    public String getExcelRawData() { return excelRawData; }
    public void setExcelRawData(String excelRawData) { this.excelRawData = excelRawData; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}