package com.xhbookstore.api.model;

import java.util.List;

/**
 * 小程序API统一分页结构
 */
public class PageResult<T> {

    private List<T> list;
    private int pageNo;
    private int pageSize;
    private long total;
    private boolean hasMore;

    public PageResult() {}

    public PageResult(List<T> list, int pageNo, int pageSize, long total) {
        this.list = list;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
        this.hasMore = (long) pageNo * pageSize < total;
    }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    public int getPageNo() { return pageNo; }
    public void setPageNo(int pageNo) { this.pageNo = pageNo; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
}
