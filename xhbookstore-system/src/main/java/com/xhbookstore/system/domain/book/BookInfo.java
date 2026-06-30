package com.xhbookstore.system.domain.book;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class BookInfo {
    private Long id;
    private String bookName;
    private String isbn;
    private String author;
    private String publisher;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQty;
    private Integer lendableQty;
    private Integer bookStatus;
    private String coverUrl;
    private String description;
    private String remark;
    private String createStaffId;
    private String createStaffName;
    private String updateStaffId;
    private String updateStaffName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    private Integer isDel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public Integer getStockQty() { return stockQty; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }
    public Integer getLendableQty() { return lendableQty; }
    public void setLendableQty(Integer lendableQty) { this.lendableQty = lendableQty; }
    public Integer getBookStatus() { return bookStatus; }
    public void setBookStatus(Integer bookStatus) { this.bookStatus = bookStatus; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
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
    public Integer getIsDel() { return isDel; }
    public void setIsDel(Integer isDel) { this.isDel = isDel; }
}
