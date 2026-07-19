package com.xhbookstore.web.controller.member;

/** 按规则人工调整会员积分的请求参数。 */
public class PointsAdjustRequest {
    private Long ruleId;
    private Integer points;
    private String description;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
