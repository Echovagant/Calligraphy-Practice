package com.calligraphy.platform.constant;

public class SecurityConstants {
    
    public static final String[] PUBLIC_URLS = {
        "/api/auth/**",
        "/api/public/**",
        "/api/health",
        "/error",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**"
    };
    
    public static final String JWT_SECRET = "calligraphy-platform-secret-key";
    public static final long JWT_EXPIRATION = 86400000; // 24 hours
    public static final long JWT_REFRESH_EXPIRATION = 604800000; // 7 days
    
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    
    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }
}


