package com.xhbookstore.system.domain.member;

import java.io.Serializable;

public class MemberCodeTokenInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;
    private Integer memberId;
    private String memberNo;
    private String scene;
    private Long issuedAt;
    private Long expiresAt;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getMemberNo() { return memberNo; }
    public void setMemberNo(String memberNo) { this.memberNo = memberNo; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public Long getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Long issuedAt) { this.issuedAt = issuedAt; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
