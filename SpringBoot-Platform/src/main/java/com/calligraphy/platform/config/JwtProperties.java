package com.calligraphy.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性类
 * 用于集中管理JWT相关配置参数
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT签名密钥
     */
    private String secret;
    
    /**
     * 访问令牌过期时间（毫秒），默认24小时
     */
    private Long expiration = 86400000L;
    
    /**
     * 刷新令牌过期时间（毫秒），默认7天
     */
    private Long refreshExpiration = 604800000L;
    
    /**
     * JWT签名者
     */
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
     * 授权头名称
     */
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
     * @return 过期时间（毫秒）
     */
    public Long getExpiration() {
        return expiration;
    }
    
    /**
     * 设置访问令牌过期时间
     * @param expiration 过期时间（毫秒）
     */
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
    
    /**
     * 获取刷新令牌过期时间
     * @return 过期时间（毫秒）
     */
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
    
    /**
     * 设置刷新令牌过期时间
     * @param refreshExpiration 过期时间（毫秒）
     */
    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
    
    /**
     * 获取JWT签名者
     * @return 签名者
     */
    public String getIssuer() {
        return issuer;
    }
    
    /**
     * 设置JWT签名者
     * @param issuer 签名者
     */
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
     * 获取授权头名称
     * @return 授权头名称
     */
    public String getAuthHeaderName() {
        return authHeaderName;
    }
    
    /**
     * 设置授权头名称
     * @param authHeaderName 授权头名称
     */
    public void setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
    }
}


