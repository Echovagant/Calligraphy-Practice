package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDto {
    
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
    
    // 无参构造器
    public RefreshTokenRequestDto() {
    }
    
    // 全参构造器
    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getter 和 Setter 方法
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Builder 类
    public static class Builder {
        private String refreshToken;
        
        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }
        
        public RefreshTokenRequestDto build() {
            return new RefreshTokenRequestDto(refreshToken);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


