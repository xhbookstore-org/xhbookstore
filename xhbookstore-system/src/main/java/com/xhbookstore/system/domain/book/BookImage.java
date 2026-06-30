package com.xhbookstore.system.domain.book;

import java.util.Date;

public class BookImage {
    private Long id;
    private Long bookId;
    private String imageId;
    private String imageName;
    private String imageUrl;
    private String thumbUrl;
    private Integer sortOrder;
    private Integer imageType;
    private Integer imageStatus;
    private String createStaffId;
    private String createStaffName;
    private String updateStaffId;
    private String updateStaffName;
    private Date createdAt;
    private Date updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
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
    public String getCreateStaffId() { return createStaffId; }
    public void setCreateStaffId(String createStaffId) { this.createStaffId = createStaffId; }
    public String getCreateStaffName() { return createStaffName; }
    public void setCreateStaffName(String createStaffName) { this.createStaffName = createStaffName; }
    public String getUpdateStaffId() { return updateStaffId; }
    public void setUpdateStaffId(String updateStaffId) { this.updateStaffId = updateStaffId; }
    public String getUpdateStaffName() { return updateStaffName; }
    public void setUpdateStaffName(String updateStaffName) { this.updateStaffName = updateStaffName; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
