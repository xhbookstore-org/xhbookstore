package com.xhbookstore.system.domain.member;

/**
 * 卡类型 card_type
 */
public class CardType {
    private Integer id;
    private String typeName;
    private Integer validDays;
    private Integer isRenewal;
    private Integer status;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }
    public Integer getIsRenewal() { return isRenewal; }
    public void setIsRenewal(Integer isRenewal) { this.isRenewal = isRenewal; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}