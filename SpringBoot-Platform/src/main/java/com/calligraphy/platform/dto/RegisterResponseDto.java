package com.calligraphy.platform.dto;

import java.time.LocalDateTime;

public class RegisterResponseDto {
    
    private String userId;
    private String username;
    private String nickname;
    private String email;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private LocalDateTime tokenExpiry;
    private boolean verificationRequired;
    private boolean emailSent;
    private String message;
    private LocalDateTime createdAt;
    
    public RegisterResponseDto() {
    }
    
    public RegisterResponseDto(String userId, String username, String nickname, String email, String accessToken, 
                              String refreshToken, String tokenType, long expiresIn, LocalDateTime tokenExpiry, 
                              boolean verificationRequired, boolean emailSent, String message, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.tokenExpiry = tokenExpiry;
        this.verificationRequired = verificationRequired;
        this.emailSent = emailSent;
        this.message = message;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public LocalDateTime getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(LocalDateTime tokenExpiry) { this.tokenExpiry = tokenExpiry; }
    public boolean isVerificationRequired() { return verificationRequired; }
    public void setVerificationRequired(boolean verificationRequired) { this.verificationRequired = verificationRequired; }
    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    // Builder class
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private String username;
        private String nickname;
        private String email;
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private LocalDateTime tokenExpiry;
        private boolean verificationRequired;
        private boolean emailSent;
        private String message;
        private LocalDateTime createdAt;
        
        private Builder() {}
        
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public Builder tokenExpiry(LocalDateTime tokenExpiry) { this.tokenExpiry = tokenExpiry; return this; }
        public Builder verificationRequired(boolean verificationRequired) { this.verificationRequired = verificationRequired; return this; }
        public Builder emailSent(boolean emailSent) { this.emailSent = emailSent; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        
        public RegisterResponseDto build() {
            return new RegisterResponseDto(userId, username, nickname, email, accessToken, refreshToken, 
                                         tokenType, expiresIn, tokenExpiry, verificationRequired, 
                                         emailSent, message, createdAt);
        }
    }
}


