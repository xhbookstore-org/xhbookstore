package com.xhbookstore.system.domain.member;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xhbookstore.common.core.domain.BaseEntity;

/**
 * 会员主表 member
 */
public class Member extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String cardNo;
    private String name;
    private String phone;
    private Integer cardTypeId;
    private Integer levelId;
    private Long deptId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date validDate;
    private Integer status;
    private Integer borrowCountValid;
    private Integer currentPoints;
    private String remark;
    private String lastOperator;
    private String source;
    private Integer syncErp;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    // 关联字段
    private String cardTypeName;
    private String levelName;
    private String deptName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCardNo() { return cardNo; }
    public void setCardNo(String cardNo) { this.cardNo = cardNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getCardTypeId() { return cardTypeId; }
    public void setCardTypeId(Integer cardTypeId) { this.cardTypeId = cardTypeId; }
    public Integer getLevelId() { return levelId; }
    public void setLevelId(Integer levelId) { this.levelId = levelId; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Date getValidDate() { return validDate; }
    public void setValidDate(Date validDate) { this.validDate = validDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getBorrowCountValid() { return borrowCountValid; }
    public void setBorrowCountValid(Integer borrowCountValid) { this.borrowCountValid = borrowCountValid; }
    public Integer getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(Integer currentPoints) { this.currentPoints = currentPoints; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getLastOperator() { return lastOperator; }
    public void setLastOperator(String lastOperator) { this.lastOperator = lastOperator; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getSyncErp() { return syncErp; }
    public void setSyncErp(Integer syncErp) { this.syncErp = syncErp; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getCardTypeName() { return cardTypeName; }
    public void setCardTypeName(String cardTypeName) { this.cardTypeName = cardTypeName; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
}