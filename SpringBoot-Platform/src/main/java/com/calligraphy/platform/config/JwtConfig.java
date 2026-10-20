package com.calligraphy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    
    private String secret = "mySecretKey";
    private int expiration = 86400000; // 24小时
    private int refreshExpiration = 604800000; // 7澶
    private String header = "Authorization";
    private String prefix = "Bearer ";
    private String frontendSecret = "frontendSecretKey";
    private String backendSecret = "backendSecretKey";
    private int frontendExpiration = 86400000; // 鍓嶇用户24小时
    private int backendExpiration = 86400000; // 鍚庣绠＄悊鍛?4小时
}


