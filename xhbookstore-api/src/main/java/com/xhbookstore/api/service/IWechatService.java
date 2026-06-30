package com.xhbookstore.api.service;

/**
 * 微信服务接口
 */
public interface IWechatService {

    /**
     * 获取微信 access_token（带Redis缓存，自动刷新）
     */
    String getAccessToken();

    /**
     * 通过微信手机号授权code获取手机号
     * @param code 微信getPhoneNumber返回的code
     * @return 手机号（不含国家代码）
     */
    String getPhoneNumber(String code);
}
