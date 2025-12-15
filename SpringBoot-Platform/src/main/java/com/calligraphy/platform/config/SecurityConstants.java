package com.calligraphy.platform.config;

public class SecurityConstants {
    
    // 鍏紑鐨凙PI璺緞
    public static final String[] PUBLIC_URLS = {
        "/api/auth/**",
        "/api/frontend/auth/**",
        "/api/backend/auth/**",
        "/api/public/**",
        "/",
        "/index.html",
        "/static/**",
        "/assets/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/webjars/**",
        "/favicon.ico"
    };
    
    // 鍓嶇鐢ㄦ埛鍙闂殑API璺緞
    public static final String[] USER_URLS = {
        "/api/frontend/**",
        "/api/user/**",
        "/api/works/**",
        "/api/comments/**",
        "/api/file/upload/**",
        "/api/file/download/**"
    };
    
    // 鍚庣绠＄悊鍛樺彲璁块棶鐨凙PI璺緞
    public static final String[] ADMIN_URLS = {
        "/api/backend/**",
        "/api/admin/**",
        "/api/file/review/**",
        "/api/file/approve/**",
        "/api/file/reject/**",
        "/api/users/manage/**"
    };
    
    // JWT鐩稿叧甯搁噺
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_SECRET = "mySecretKey";
    public static final long JWT_EXPIRATION = 86400000; // 24灏忔椂
    public static final long JWT_REFRESH_EXPIRATION = 604800000; // 7澶?    
    // 瑙掕壊甯搁噺
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    
    // 鐢ㄦ埛绫诲瀷甯搁噺
    public static final String USER_TYPE_FRONTEND = "USER";
    public static final String USER_TYPE_BACKEND = "ADMIN";
}


