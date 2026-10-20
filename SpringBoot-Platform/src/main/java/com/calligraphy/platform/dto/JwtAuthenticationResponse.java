package com.calligraphy.platform.dto;

public class JwtAuthenticationResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    
    // 鏃犲弬鏋勯€犲櫒
    public JwtAuthenticationResponse() {
    }
    
    // 甯︿袱涓猄tring参数的构造器
    public JwtAuthenticationResponse(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }
    
    // 鍙甫accessToken的构造器
    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }
    
    // Getter 鍜?Setter 方法
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


