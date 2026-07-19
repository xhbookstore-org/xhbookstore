package com.xhbookstore.system.domain.book;

import java.util.Date;

public class BookBorrowDetailImage {
    private Long id;
    private Integer memberId;
    private Long borrowDetailId;
    private Long borrowOrderId;
    private String borrowOrderNo;
    private String imageId;
    private String imageName;
    private String imageUrl;
    private String thumbUrl;
    private Integer sortOrder;
    private Integer imageType;
    private Integer imageStatus;
    /** TEMP before a borrow detail is created, BOUND after binding. */
    private String bindStatus;
    private String createStaffId;
    private String createStaffName;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public Long getBorrowDetailId() { return borrowDetailId; }
    public void setBorrowDetailId(Long borrowDetailId) { this.borrowDetailId = borrowDetailId; }
    public Long getBorrowOrderId() { return borrowOrderId; }
    public void setBorrowOrderId(Long borrowOrderId) { this.borrowOrderId = borrowOrderId; }
    public String getBorrowOrderNo() { return borrowOrderNo; }
    public void setBorrowOrderNo(String borrowOrderNo) { this.borrowOrderNo = borrowOrderNo; }
    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getImageType() { return imageType; }
    public void setImageType(Integer imageType) { this.imageType = imageType; }
    public Integer getImageStatus() { return imageStatus; }
    public void setImageStatus(Integer imageStatus) { this.imageStatus = imageStatus; }
    public String getBindStatus() { return bindStatus; }
    public void setBindStatus(String bindStatus) { this.bindStatus = bindStatus; }
    public String getCreateStaffId() { return createStaffId; }
    public void setCreateStaffId(String createStaffId) { this.createStaffId = createStaffId; }
    public String getCreateStaffName() { return createStaffName; }
    public void setCreateStaffName(String createStaffName) { this.createStaffName = createStaffName; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
