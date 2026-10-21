package com.calligraphy.platform.config;

public class SecurityConstants {
    
    // 公开的API路径
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
    
    // 前端用户可访问的API路径
    public static final String[] USER_URLS = {
        "/api/frontend/**",
        "/api/user/**",
        "/api/works/**",
        "/api/comments/**",
        "/api/file/upload/**",
        "/api/file/download/**"
    };
    
    // 后端管理员可访问的API路径
    public static final String[] ADMIN_URLS = {
        "/api/backend/**",
        "/api/admin/**",
        "/api/file/review/**",
        "/api/file/approve/**",
        "/api/file/reject/**",
        "/api/users/manage/**"
    };
    
    // JWT相关常量
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final String JWT_SECRET = "mySecretKey";
    public static final long JWT_EXPIRATION = 86400000; // 24小时
    public static final long JWT_REFRESH_EXPIRATION = 604800000; // 7天     
    // 角色常量
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
    
    // 用户类型常量
    public static final String USER_TYPE_FRONTEND = "USER";
    public static final String USER_TYPE_BACKEND = "ADMIN";
}


