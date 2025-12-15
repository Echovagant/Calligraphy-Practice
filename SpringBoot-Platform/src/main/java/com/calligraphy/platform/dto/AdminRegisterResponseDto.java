package com.calligraphy.platform.dto;

import java.time.LocalDateTime;

public class AdminRegisterResponseDto {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String roles;
    private String permissions;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    
    public AdminRegisterResponseDto() {
    }
    
    public AdminRegisterResponseDto(Long id, String username, String email, String nickname, String roles, 
                                   String permissions, Boolean isActive, Boolean isEmailVerified, 
                                   LocalDateTime createdAt, LocalDateTime updatedAt, String accessToken, 
                                   String refreshToken, String tokenType, Long expiresIn) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.roles = roles;
        this.permissions = permissions;
        this.isActive = isActive;
        this.isEmailVerified = isEmailVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    
    // Builder class
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String roles;
        private String permissions;
        private Boolean isActive;
        private Boolean isEmailVerified;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        
        private Builder() {}
        
        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder roles(String roles) { this.roles = roles; return this; }
        public Builder permissions(String permissions) { this.permissions = permissions; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder isEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresIn(Long expiresIn) { this.expiresIn = expiresIn; return this; }
        
        public AdminRegisterResponseDto build() {
            return new AdminRegisterResponseDto(id, username, email, nickname, roles, permissions, 
                                              isActive, isEmailVerified, createdAt, updatedAt, 
                                              accessToken, refreshToken, tokenType, expiresIn);
        }
    }
}


