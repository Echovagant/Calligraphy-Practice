package com.calligraphy.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT閰嶇疆灞炴€х被
 * 鐢ㄤ簬闆嗕腑绠＄悊JWT相关配置参数
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT签名密钥
     */
    private String secret;
    
    /**
     * 璁块棶浠ょ墝杩囨湡鏃堕棿锛堟绉掞級锛岄粯璁?4小时
     */
    private Long expiration = 86400000L;
    
    /**
     * 鍒锋柊浠ょ墝杩囨湡鏃堕棿锛堟绉掞級锛岄粯璁?澶?     */
    private Long refreshExpiration = 604800000L;
    
    /**
     * JWT绛惧彂鑰?     */
    private String issuer = "calligraphy-platform";
    
    /**
     * JWT受众
     */
    private String audience = "calligraphy-users";
    
    /**
     * 令牌前缀
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * 鎺堟潈澶村悕绉?     */
    private String authHeaderName = "Authorization";
    
    /**
     * 获取JWT签名密钥
     * @return 签名密钥
     */
    public String getSecret() {
        return secret;
    }
    
    /**
     * 设置JWT签名密钥
     * @param secret 签名密钥
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    /**
     * 获取访问令牌过期时间
     * @return 杩囨湡鏃堕棿锛堟秒）
     */
    public Long getExpiration() {
        return expiration;
    }
    
    /**
     * 设置访问令牌过期时间
     * @param expiration 杩囨湡鏃堕棿锛堟秒）
     */
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
    
    /**
     * 获取刷新令牌过期时间
     * @return 杩囨湡鏃堕棿锛堟秒）
     */
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
    
    /**
     * 设置刷新令牌过期时间
     * @param refreshExpiration 杩囨湡鏃堕棿锛堟秒）
     */
    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
    
    /**
     * 获取JWT绛惧彂鑰?     * @return 绛惧彂鑰?     */
    public String getIssuer() {
        return issuer;
    }
    
    /**
     * 设置JWT绛惧彂鑰?     * @param issuer 绛惧彂鑰?     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    /**
     * 获取JWT受众
     * @return 受众
     */
    public String getAudience() {
        return audience;
    }
    
    /**
     * 设置JWT受众
     * @param audience 受众
     */
    public void setAudience(String audience) {
        this.audience = audience;
    }
    
    /**
     * 获取令牌前缀
     * @return 令牌前缀
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }
    
    /**
     * 设置令牌前缀
     * @param tokenPrefix 令牌前缀
     */
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }
    
    /**
     * 鑾峰彇鎺堟潈澶村悕绉?     * @return 鎺堟潈澶村悕绉?     */
    public String getAuthHeaderName() {
        return authHeaderName;
    }
    
    /**
     * 璁剧疆鎺堟潈澶村悕绉?     * @param authHeaderName 鎺堟潈澶村悕绉?     */
    public void setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
    }
}


