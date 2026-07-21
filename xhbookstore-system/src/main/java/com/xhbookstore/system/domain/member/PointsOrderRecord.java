package com.xhbookstore.system.domain.member;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xhbookstore.common.core.domain.BaseEntity;
import com.xhbookstore.common.annotation.Excel;

/** Admin 积分流水列表、详情及查询条件。 */
public class PointsOrderRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    @Excel(name = "积分订单号", sort = 1, width = 24)
    private String orderNumber;
    private Integer memberId;
    @Excel(name = "姓名", sort = 2) private String memberName;
    @Excel(name = "会员编号", sort = 3, width = 16) private String memberNo;
    @Excel(name = "手机号", sort = 4, width = 14) private String memberPhone;
    private Long deptId;
    @Excel(name = "门店", sort = 14) private String deptName;
    @Excel(name = "积分变化", sort = 7) private Integer amount;
    @Excel(name = "备注", sort = 17, width = 28) private String description;
    @Excel(name = "操作前积分", sort = 8) private Integer orginPoints;
    @Excel(name = "操作后积分", sort = 9) private Integer afterPoints;
    private Long ruleId;
    private String ruleCode;
    @Excel(name = "积分规则", sort = 5, width = 18) private String ruleName;
    private String sceneCode;
    @Excel(name = "操作类型", sort = 11, readConverterExp = "NORMAL=正常入账/消费,REVERSAL=冲销,EXPIRATION=过期")
    private String operationKind;
    @Excel(name = "积分方向", sort = 6, readConverterExp = "ADD=增加积分,DEDUCT=扣减积分")
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
    @Excel(name = "可用状态", sort = 10, readConverterExp = "AVAILABLE=可用,FROZEN=冻结中,CANCELLED=已取消")
    private String availabilityStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date availableAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date unfrozenAt;
    @Excel(name = "业务类型", sort = 12) private String businessType;
    @Excel(name = "业务单号", sort = 13, width = 20) private String businessOrderNo;
    private String originalOrderNo;
    private String operatorType;
    private Long operatorUserId;
    @Excel(name = "操作人", sort = 15) private String operatorName;
    @Excel(name = "操作时间", sort = 16, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date operationTime;
    private String operationDevice;
    @Excel(name = "订单状态", sort = 18, readConverterExp = "SUCCESS=成功,REVERSED=已冲销,FAILED=失败")
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
