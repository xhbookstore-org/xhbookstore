package com.xhbookstore.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 */
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {
    /** 小程序AppID */
    private String appId;
    /** 小程序AppSecret */
    private String appSecret;
    /** access_token缓存Redis key */
    private String tokenCacheKey = "wechat:access_token";
    /** access_token有效期(秒)，微信默认7200 */
    private long tokenExpireSeconds = 7200;
    /** 提前刷新时间(秒)，默认300(5分钟) */
    private long tokenRefreshAhead = 300;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getTokenCacheKey() { return tokenCacheKey; }
    public void setTokenCacheKey(String tokenCacheKey) { this.tokenCacheKey = tokenCacheKey; }
    public long getTokenExpireSeconds() { return tokenExpireSeconds; }
    public void setTokenExpireSeconds(long tokenExpireSeconds) { this.tokenExpireSeconds = tokenExpireSeconds; }
    public long getTokenRefreshAhead() { return tokenRefreshAhead; }
    public void setTokenRefreshAhead(long tokenRefreshAhead) { this.tokenRefreshAhead = tokenRefreshAhead; }
}
