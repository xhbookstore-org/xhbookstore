package com.xhbookstore.system.domain.member;

import java.math.BigDecimal;
import java.util.Date;
import com.xhbookstore.common.annotation.Excel;
import com.xhbookstore.common.annotation.Excel.ColumnType;

/**
 * 会员导出数据，字段顺序对齐ERP会员导入Excel。
 */
public class MemberExport {
    @Excel(name = "会员编号", sort = 1, width = 18)
    private String cardNo;

    @Excel(name = "姓名", sort = 2, width = 14)
    private String name;

    @Excel(name = "卡类型", sort = 3, width = 14)
    private String cardTypeName;

    @Excel(name = "会员级别", sort = 4, width = 14)
    private String levelName;

    @Excel(name = "折扣", sort = 5, cellType = ColumnType.NUMERIC)
    private BigDecimal discount;

    @Excel(name = "总积分", sort = 6, cellType = ColumnType.NUMERIC)
    private Integer totalPoints;

    @Excel(name = "当前积分", sort = 7, cellType = ColumnType.NUMERIC)
    private Integer currentPoints;

    @Excel(name = "级别积分", sort = 8, cellType = ColumnType.NUMERIC)
    private Integer levelPoints;

    @Excel(name = "注销", sort = 9)
    private String cancelled;

    @Excel(name = "挂失 标记", sort = 10)
    private String lostFlag;

    @Excel(name = "手机", sort = 11, width = 16)
    private String phone;

    @Excel(name = "累计购书额", sort = 12, cellType = ColumnType.NUMERIC, width = 14)
    private BigDecimal totalPurchaseAmount;

    @Excel(name = "累计购书册数", sort = 13, cellType = ColumnType.NUMERIC, width = 14)
    private Integer totalPurchaseCount;

    @Excel(name = "累计购书次数", sort = 14, cellType = ColumnType.NUMERIC, width = 14)
    private Integer totalPurchaseTimes;

    @Excel(name = "入会日期", sort = 15, width = 14, dateFormat = "yyyy-MM-dd")
    private Date joinDate;

    @Excel(name = "性别", sort = 16)
    private String gender;

    @Excel(name = "年龄", sort = 17, cellType = ColumnType.NUMERIC)
    private Integer age;

    @Excel(name = "备注", sort = 18, width = 30)
    private String remark;

    @Excel(name = "单位电话", sort = 19, width = 16)
    private String unitPhone;

    @Excel(name = "有效日期", sort = 20, width = 14, dateFormat = "yyyy-MM-dd")
    private Date validDate;

    @Excel(name = "微信", sort = 21, width = 16)
    private String wechat;

    @Excel(name = "微博", sort = 22, width = 16)
    private String weibo;

    @Excel(name = "上级积分比例", sort = 23, cellType = ColumnType.NUMERIC, width = 14)
    private BigDecimal superiorPointsRatio;

    @Excel(name = "上级名称", sort = 24, width = 16)
    private String superiorName;

    @Excel(name = "业务员名称", sort = 25, width = 16)
    private String businessStaffName;

    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCardTypeName() { return cardTypeName; }
    public void setCardTypeName(String cardTypeName) { this.cardTypeName = cardTypeName; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }
    public Integer getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(Integer currentPoints) { this.currentPoints = currentPoints; }
    public Integer getLevelPoints() { return levelPoints; }
    public void setLevelPoints(Integer levelPoints) { this.levelPoints = levelPoints; }
    public String getCancelled() { return cancelled; }
    public void setCancelled(String cancelled) { this.cancelled = cancelled; }
    public String getLostFlag() { return lostFlag; }
    public void setLostFlag(String lostFlag) { this.lostFlag = lostFlag; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getTotalPurchaseAmount() { return totalPurchaseAmount; }
    public void setTotalPurchaseAmount(BigDecimal totalPurchaseAmount) { this.totalPurchaseAmount = totalPurchaseAmount; }
    public Integer getTotalPurchaseCount() { return totalPurchaseCount; }
    public void setTotalPurchaseCount(Integer totalPurchaseCount) { this.totalPurchaseCount = totalPurchaseCount; }
    public Integer getTotalPurchaseTimes() { return totalPurchaseTimes; }
    public void setTotalPurchaseTimes(Integer totalPurchaseTimes) { this.totalPurchaseTimes = totalPurchaseTimes; }
    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getUnitPhone() { return unitPhone; }
    public void setUnitPhone(String unitPhone) { this.unitPhone = unitPhone; }
    public Date getValidDate() { return validDate; }
    public void setValidDate(Date validDate) { this.validDate = validDate; }
    public String getWechat() { return wechat; }
    public void setWechat(String wechat) { this.wechat = wechat; }
    public String getWeibo() { return weibo; }
    public void setWeibo(String weibo) { this.weibo = weibo; }
    public BigDecimal getSuperiorPointsRatio() { return superiorPointsRatio; }
    public void setSuperiorPointsRatio(BigDecimal superiorPointsRatio) { this.superiorPointsRatio = superiorPointsRatio; }
    public String getSuperiorName() { return superiorName; }
    public void setSuperiorName(String superiorName) { this.superiorName = superiorName; }
    public String getBusinessStaffName() { return businessStaffName; }
    public void setBusinessStaffName(String businessStaffName) { this.businessStaffName = businessStaffName; }
}
