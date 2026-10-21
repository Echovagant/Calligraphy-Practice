package com.calligraphy.platform.dto;

public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    
    // 无参构造器
    public JwtAuthenticationResponse() {
    }
    
    // 带两个String参数的构造器
    public JwtAuthenticationResponse(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }
    
    // 只带accessToken的构造器
    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
    
    // Getter 和 Setter 方法
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}


