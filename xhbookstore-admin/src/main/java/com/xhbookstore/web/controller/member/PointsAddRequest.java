package com.xhbookstore.web.controller.member;

/**
 * 积分添加请求参数
 */
public class PointsAddRequest {
    private Integer points;
    private String description;

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
