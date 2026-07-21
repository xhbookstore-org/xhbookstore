package com.xhbookstore.system.domain.member;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xhbookstore.common.core.domain.BaseEntity;

/** Admin 积分流水列表、详情及查询条件。 */
public class PointsOrderRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String orderNumber;
    private Integer memberId;
    private String memberName;
    private String memberNo;
    private String memberPhone;
    private Long deptId;
    private String deptName;
    private Integer amount;
    private String description;
    private Integer orginPoints;
    private Integer afterPoints;
    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private String sceneCode;
    private String operationKind;
    private String direction;
    private String triggerMode;
    private String triggerEvent;
    private String calculationMode;
    private BigDecimal baseAmount;
    private BigDecimal baseQuantity;
    private Integer basePoints;
    private BigDecimal multiplier;
    private String balanceBucket;
    private Integer beforeFrozenPoints;
    private Integer afterFrozenPoints;
    private String availabilityStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date availableAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date unfrozenAt;
    private String businessType;
    private String businessOrderNo;
    private String originalOrderNo;
    private String operatorType;
    private Long operatorUserId;
    private String operatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date operationTime;
    private String operationDevice;
    private String orderStatus;
    private String calculationSnapshot;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date createdAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getMemberNo() { return memberNo; }
    public void setMemberNo(String memberNo) { this.memberNo = memberNo; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getOrginPoints() { return orginPoints; }
    public void setOrginPoints(Integer orginPoints) { this.orginPoints = orginPoints; }
    public Integer getAfterPoints() { return afterPoints; }
    public void setAfterPoints(Integer afterPoints) { this.afterPoints = afterPoints; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getSceneCode() { return sceneCode; }
    public void setSceneCode(String sceneCode) { this.sceneCode = sceneCode; }
    public String getOperationKind() { return operationKind; }
    public void setOperationKind(String operationKind) { this.operationKind = operationKind; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getTriggerMode() { return triggerMode; }
    public void setTriggerMode(String triggerMode) { this.triggerMode = triggerMode; }
    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }
    public String getCalculationMode() { return calculationMode; }
    public void setCalculationMode(String calculationMode) { this.calculationMode = calculationMode; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
    public BigDecimal getBaseQuantity() { return baseQuantity; }
    public void setBaseQuantity(BigDecimal baseQuantity) { this.baseQuantity = baseQuantity; }
    public Integer getBasePoints() { return basePoints; }
    public void setBasePoints(Integer basePoints) { this.basePoints = basePoints; }
    public BigDecimal getMultiplier() { return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }
    public String getBalanceBucket() { return balanceBucket; }
    public void setBalanceBucket(String balanceBucket) { this.balanceBucket = balanceBucket; }
    public Integer getBeforeFrozenPoints() { return beforeFrozenPoints; }
    public void setBeforeFrozenPoints(Integer beforeFrozenPoints) { this.beforeFrozenPoints = beforeFrozenPoints; }
    public Integer getAfterFrozenPoints() { return afterFrozenPoints; }
    public void setAfterFrozenPoints(Integer afterFrozenPoints) { this.afterFrozenPoints = afterFrozenPoints; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public Date getAvailableAt() { return availableAt; }
    public void setAvailableAt(Date availableAt) { this.availableAt = availableAt; }
    public Date getUnfrozenAt() { return unfrozenAt; }
    public void setUnfrozenAt(Date unfrozenAt) { this.unfrozenAt = unfrozenAt; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getBusinessOrderNo() { return businessOrderNo; }
    public void setBusinessOrderNo(String businessOrderNo) { this.businessOrderNo = businessOrderNo; }
    public String getOriginalOrderNo() { return originalOrderNo; }
    public void setOriginalOrderNo(String originalOrderNo) { this.originalOrderNo = originalOrderNo; }
    public String getOperatorType() { return operatorType; }
    public void setOperatorType(String operatorType) { this.operatorType = operatorType; }
    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getOperationTime() { return operationTime; }
    public void setOperationTime(Date operationTime) { this.operationTime = operationTime; }
    public String getOperationDevice() { return operationDevice; }
    public void setOperationDevice(String operationDevice) { this.operationDevice = operationDevice; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getCalculationSnapshot() { return calculationSnapshot; }
    public void setCalculationSnapshot(String calculationSnapshot) { this.calculationSnapshot = calculationSnapshot; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
