package com.xhbookstore.web.controller.member;

/** 积分规则数值修改请求。 */
public class PointsRuleUpdateRequest {
    private Integer fixedPoints;
    private Integer pointsPerUnit;

    public Integer getFixedPoints() { return fixedPoints; }
    public void setFixedPoints(Integer fixedPoints) { this.fixedPoints = fixedPoints; }
    public Integer getPointsPerUnit() { return pointsPerUnit; }
    public void setPointsPerUnit(Integer pointsPerUnit) { this.pointsPerUnit = pointsPerUnit; }
}
