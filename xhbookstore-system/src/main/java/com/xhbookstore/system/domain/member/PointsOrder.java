package com.xhbookstore.system.domain.member;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 书城币订单表 xhbs_points_order
 */
public class PointsOrder {
    private Integer id;
    private String orderNumber;
    private String openId;
    private String cardNo;
    private Integer memberId;
    private String appOrderNumber;
    private Integer amount;
    private String description;
    private Integer type;
    private String customArgs;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderTime;
    private String clientIp;
    private String itemId;
    private String itemName;
    private Integer price;
    private Integer discountedPrice;
    private String operationDevice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    private Integer isDel;
    private Integer amountType;
    private String appId;
    private Integer orginPoints;
    private Integer afterPoints;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getAppOrderNumber() { return appOrderNumber; }
    public void setAppOrderNumber(String appOrderNumber) { this.appOrderNumber = appOrderNumber; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getCustomArgs() { return customArgs; }
    public void setCustomArgs(String customArgs) { this.customArgs = customArgs; }
    public Date getCompletedTime() { return completedTime; }
    public void setCompletedTime(Date completedTime) { this.completedTime = completedTime; }
    public Date getOrderTime() { return orderTime; }
    public void setOrderTime(Date orderTime) { this.orderTime = orderTime; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public Integer getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(Integer discountedPrice) { this.discountedPrice = discountedPrice; }
    public String getOperationDevice() { return operationDevice; }
    public void setOperationDevice(String operationDevice) { this.operationDevice = operationDevice; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
    public Integer getAmountType() { return amountType; }
    public void setAmountType(Integer amountType) { this.amountType = amountType; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public Integer getOrginPoints() { return orginPoints; }
    public void setOrginPoints(Integer orginPoints) { this.orginPoints = orginPoints; }
    public Integer getAfterPoints() { return afterPoints; }
    public void setAfterPoints(Integer afterPoints) { this.afterPoints = afterPoints; }
}
