package com.xhbookstore.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private Jwt jwt = new Jwt();
    private RateLimit rateLimit = new RateLimit();

    public Jwt getJwt() { return jwt; }
    public RateLimit getRateLimit() { return rateLimit; }

    public static class Jwt {
        private String secret = "xhbookstore-jwt-secret-key-2026";
        private long accessTokenExpire = 7200;
        private long refreshTokenExpire = 2592000;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getAccessTokenExpire() { return accessTokenExpire; }
        public void setAccessTokenExpire(long accessTokenExpire) { this.accessTokenExpire = accessTokenExpire; }
        public long getRefreshTokenExpire() { return refreshTokenExpire; }
        public void setRefreshTokenExpire(long refreshTokenExpire) { this.refreshTokenExpire = refreshTokenExpire; }
    }

    public static class RateLimit {
        private int loginPerMinute = 5;
        private int writePerMinute = 30;
        private int pointsPerMinute = 10;
        public int getLoginPerMinute() { return loginPerMinute; }
        public void setLoginPerMinute(int loginPerMinute) { this.loginPerMinute = loginPerMinute; }
        public int getWritePerMinute() { return writePerMinute; }
        public void setWritePerMinute(int writePerMinute) { this.writePerMinute = writePerMinute; }
        public int getPointsPerMinute() { return pointsPerMinute; }
        public void setPointsPerMinute(int pointsPerMinute) { this.pointsPerMinute = pointsPerMinute; }
    }
}
