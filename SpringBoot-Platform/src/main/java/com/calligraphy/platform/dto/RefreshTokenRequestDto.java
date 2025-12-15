package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDto {
    
    @NotBlank(message = "鍒锋柊浠ょ墝涓嶈兘涓虹┖")
    private String refreshToken;
    
    // 鏃犲弬鏋勯€犲櫒
    public RefreshTokenRequestDto() {
    }
    
    // 鍏ㄥ弬鏋勯€犲櫒
    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getter 鍜?Setter 鏂规硶
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Builder 绫?    public static class Builder {
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


