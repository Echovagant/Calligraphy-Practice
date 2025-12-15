package com.calligraphy.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT閰嶇疆灞炴€х被
 * 鐢ㄤ簬闆嗕腑绠＄悊JWT鐩稿叧閰嶇疆鍙傛暟
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT绛惧悕瀵嗛挜
     */
    private String secret;
    
    /**
     * 璁块棶浠ょ墝杩囨湡鏃堕棿锛堟绉掞級锛岄粯璁?4灏忔椂
     */
    private Long expiration = 86400000L;
    
    /**
     * 鍒锋柊浠ょ墝杩囨湡鏃堕棿锛堟绉掞級锛岄粯璁?澶?     */
    private Long refreshExpiration = 604800000L;
    
    /**
     * JWT绛惧彂鑰?     */
    private String issuer = "calligraphy-platform";
    
    /**
     * JWT鍙椾紬
     */
    private String audience = "calligraphy-users";
    
    /**
     * 浠ょ墝鍓嶇紑
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * 鎺堟潈澶村悕绉?     */
    private String authHeaderName = "Authorization";
    
    /**
     * 鑾峰彇JWT绛惧悕瀵嗛挜
     * @return 绛惧悕瀵嗛挜
     */
    public String getSecret() {
        return secret;
    }
    
    /**
     * 璁剧疆JWT绛惧悕瀵嗛挜
     * @param secret 绛惧悕瀵嗛挜
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    /**
     * 鑾峰彇璁块棶浠ょ墝杩囨湡鏃堕棿
     * @return 杩囨湡鏃堕棿锛堟绉掞級
     */
    public Long getExpiration() {
        return expiration;
    }
    
    /**
     * 璁剧疆璁块棶浠ょ墝杩囨湡鏃堕棿
     * @param expiration 杩囨湡鏃堕棿锛堟绉掞級
     */
    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
    
    /**
     * 鑾峰彇鍒锋柊浠ょ墝杩囨湡鏃堕棿
     * @return 杩囨湡鏃堕棿锛堟绉掞級
     */
    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
    
    /**
     * 璁剧疆鍒锋柊浠ょ墝杩囨湡鏃堕棿
     * @param refreshExpiration 杩囨湡鏃堕棿锛堟绉掞級
     */
    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }
    
    /**
     * 鑾峰彇JWT绛惧彂鑰?     * @return 绛惧彂鑰?     */
    public String getIssuer() {
        return issuer;
    }
    
    /**
     * 璁剧疆JWT绛惧彂鑰?     * @param issuer 绛惧彂鑰?     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    
    /**
     * 鑾峰彇JWT鍙椾紬
     * @return 鍙椾紬
     */
    public String getAudience() {
        return audience;
    }
    
    /**
     * 璁剧疆JWT鍙椾紬
     * @param audience 鍙椾紬
     */
    public void setAudience(String audience) {
        this.audience = audience;
    }
    
    /**
     * 鑾峰彇浠ょ墝鍓嶇紑
     * @return 浠ょ墝鍓嶇紑
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }
    
    /**
     * 璁剧疆浠ょ墝鍓嶇紑
     * @param tokenPrefix 浠ょ墝鍓嶇紑
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


