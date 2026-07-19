package com.xhbookstore.system.domain.member;

import java.math.BigDecimal;
import java.util.Date;

/** 积分发放/消耗规则 xhbs_points_rule。 */
public class PointsRule {
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String sceneCode;
    private String ruleSource;
    private String implementationStatus;
    private String direction;
    private String triggerMode;
    private String triggerEvent;
    private String calculationMode;
    private Integer fixedPoints;
    private Integer pointsPerUnit;
    private String unitType;
    private Integer manualPointsEditable;
    private Integer memberDayEnabled;
    private String memberDayDays;
    private BigDecimal memberDayMultiplier;
    private Date effectiveFrom;
    private Date effectiveTo;
    private Integer memberLimit;
    private Integer totalLimit;
    private Integer usedCount;
    private Long budgetPoints;
    private Long usedPoints;
    private Integer maxPointsPerOrder;
    private Integer requireBizOrder;
    private Integer requireEvidence;
    private Integer excludeBulkPurchase;
    private Integer freezeDays;
    private String status;
    private String remark;
    private Integer sortOrder;
    private Long operatorUserId;
    private String operatorName;
    private Date operationTime;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getSceneCode() { return sceneCode; }
    public void setSceneCode(String sceneCode) { this.sceneCode = sceneCode; }
    public String getRuleSource() { return ruleSource; }
    public void setRuleSource(String ruleSource) { this.ruleSource = ruleSource; }
    public String getImplementationStatus() { return implementationStatus; }
    public void setImplementationStatus(String implementationStatus) { this.implementationStatus = implementationStatus; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getTriggerMode() { return triggerMode; }
    public void setTriggerMode(String triggerMode) { this.triggerMode = triggerMode; }
    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(String triggerEvent) { this.triggerEvent = triggerEvent; }
    public String getCalculationMode() { return calculationMode; }
    public void setCalculationMode(String calculationMode) { this.calculationMode = calculationMode; }
    public Integer getFixedPoints() { return fixedPoints; }
    public void setFixedPoints(Integer fixedPoints) { this.fixedPoints = fixedPoints; }
    public Integer getPointsPerUnit() { return pointsPerUnit; }
    public void setPointsPerUnit(Integer pointsPerUnit) { this.pointsPerUnit = pointsPerUnit; }
    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }
    public Integer getManualPointsEditable() { return manualPointsEditable; }
    public void setManualPointsEditable(Integer manualPointsEditable) { this.manualPointsEditable = manualPointsEditable; }
    public Integer getMemberDayEnabled() { return memberDayEnabled; }
    public void setMemberDayEnabled(Integer memberDayEnabled) { this.memberDayEnabled = memberDayEnabled; }
    public String getMemberDayDays() { return memberDayDays; }
    public void setMemberDayDays(String memberDayDays) { this.memberDayDays = memberDayDays; }
    public BigDecimal getMemberDayMultiplier() { return memberDayMultiplier; }
    public void setMemberDayMultiplier(BigDecimal memberDayMultiplier) { this.memberDayMultiplier = memberDayMultiplier; }
    public Date getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(Date effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public Date getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(Date effectiveTo) { this.effectiveTo = effectiveTo; }
    public Integer getMemberLimit() { return memberLimit; }
    public void setMemberLimit(Integer memberLimit) { this.memberLimit = memberLimit; }
    public Integer getTotalLimit() { return totalLimit; }
    public void setTotalLimit(Integer totalLimit) { this.totalLimit = totalLimit; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
    public Long getBudgetPoints() { return budgetPoints; }
    public void setBudgetPoints(Long budgetPoints) { this.budgetPoints = budgetPoints; }
    public Long getUsedPoints() { return usedPoints; }
    public void setUsedPoints(Long usedPoints) { this.usedPoints = usedPoints; }
    public Integer getMaxPointsPerOrder() { return maxPointsPerOrder; }
    public void setMaxPointsPerOrder(Integer maxPointsPerOrder) { this.maxPointsPerOrder = maxPointsPerOrder; }
    public Integer getRequireBizOrder() { return requireBizOrder; }
    public void setRequireBizOrder(Integer requireBizOrder) { this.requireBizOrder = requireBizOrder; }
    public Integer getRequireEvidence() { return requireEvidence; }
    public void setRequireEvidence(Integer requireEvidence) { this.requireEvidence = requireEvidence; }
    public Integer getExcludeBulkPurchase() { return excludeBulkPurchase; }
    public void setExcludeBulkPurchase(Integer excludeBulkPurchase) { this.excludeBulkPurchase = excludeBulkPurchase; }
    public Integer getFreezeDays() { return freezeDays; }
    public void setFreezeDays(Integer freezeDays) { this.freezeDays = freezeDays; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Long getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getOperationTime() { return operationTime; }
    public void setOperationTime(Date operationTime) { this.operationTime = operationTime; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
