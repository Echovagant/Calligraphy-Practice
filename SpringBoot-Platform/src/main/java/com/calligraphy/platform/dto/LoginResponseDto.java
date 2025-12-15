package com.calligraphy.platform.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LoginResponseDto {
    
    private String userId;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private LocalDateTime tokenExpiry;
    private boolean twoFactorEnabled;
    private boolean twoFactorRequired;
    private List<String> roles;
    private List<String> permissions;
    private UserInfoDto userInfo;
    
    public LoginResponseDto() {
    }
    
    public LoginResponseDto(String userId, String username, String nickname, String email, String avatar, 
                           String accessToken, String refreshToken, String tokenType, long expiresIn, 
                           LocalDateTime tokenExpiry, boolean twoFactorEnabled, boolean twoFactorRequired, 
                           List<String> roles, List<String> permissions, UserInfoDto userInfo) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.avatar = avatar;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.tokenExpiry = tokenExpiry;
        this.twoFactorEnabled = twoFactorEnabled;
        this.twoFactorRequired = twoFactorRequired;
        this.roles = roles;
        this.permissions = permissions;
        this.userInfo = userInfo;
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
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
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
    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }
    public boolean isTwoFactorRequired() { return twoFactorRequired; }
    public void setTwoFactorRequired(boolean twoFactorRequired) { this.twoFactorRequired = twoFactorRequired; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public UserInfoDto getUserInfo() { return userInfo; }
    public void setUserInfo(UserInfoDto userInfo) { this.userInfo = userInfo; }
    
    // Builder class
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String userId;
        private String username;
        private String nickname;
        private String email;
        private String avatar;
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private LocalDateTime tokenExpiry;
        private boolean twoFactorEnabled;
        private boolean twoFactorRequired;
        private List<String> roles;
        private List<String> permissions;
        private UserInfoDto userInfo;
        
        private Builder() {}
        
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder avatar(String avatar) { this.avatar = avatar; return this; }
        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public Builder tokenExpiry(LocalDateTime tokenExpiry) { this.tokenExpiry = tokenExpiry; return this; }
        public Builder twoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; return this; }
        public Builder twoFactorRequired(boolean twoFactorRequired) { this.twoFactorRequired = twoFactorRequired; return this; }
        public Builder roles(List<String> roles) { this.roles = roles; return this; }
        public Builder permissions(List<String> permissions) { this.permissions = permissions; return this; }
        public Builder userInfo(UserInfoDto userInfo) { this.userInfo = userInfo; return this; }
        
        public LoginResponseDto build() {
            return new LoginResponseDto(userId, username, nickname, email, avatar, accessToken, refreshToken, 
                                       tokenType, expiresIn, tokenExpiry, twoFactorEnabled, twoFactorRequired, 
                                       roles, permissions, userInfo);
        }
    }
    
    public static class UserInfoDto {
        private String userId;
        private String username;
        private String nickname;
        private String email;
        private String avatar;
        private String phone;
        private String bio;
        private LocalDateTime createdAt;
        private boolean isVerified;
        private boolean isTwoFactorEnabled;
        
        public UserInfoDto() {
        }
        
        public UserInfoDto(String userId, String username, String nickname, String email, String avatar, 
                         String phone, String bio, LocalDateTime createdAt, boolean isVerified, 
                         boolean isTwoFactorEnabled) {
            this.userId = userId;
            this.username = username;
            this.nickname = nickname;
            this.email = email;
            this.avatar = avatar;
            this.phone = phone;
            this.bio = bio;
            this.createdAt = createdAt;
            this.isVerified = isVerified;
            this.isTwoFactorEnabled = isTwoFactorEnabled;
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
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public boolean isVerified() { return isVerified; }
        public void setVerified(boolean isVerified) { this.isVerified = isVerified; }
        public boolean isTwoFactorEnabled() { return isTwoFactorEnabled; }
        public void setTwoFactorEnabled(boolean isTwoFactorEnabled) { this.isTwoFactorEnabled = isTwoFactorEnabled; }
        
        // Builder class
        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }
        
        public static class UserInfoBuilder {
            private String userId;
            private String username;
            private String nickname;
            private String email;
            private String avatar;
            private String phone;
            private String bio;
            private LocalDateTime createdAt;
            private boolean isVerified;
            private boolean isTwoFactorEnabled;
            
            private UserInfoBuilder() {}
            
            public UserInfoBuilder userId(String userId) { this.userId = userId; return this; }
            public UserInfoBuilder username(String username) { this.username = username; return this; }
            public UserInfoBuilder nickname(String nickname) { this.nickname = nickname; return this; }
            public UserInfoBuilder email(String email) { this.email = email; return this; }
            public UserInfoBuilder avatar(String avatar) { this.avatar = avatar; return this; }
            public UserInfoBuilder phone(String phone) { this.phone = phone; return this; }
            public UserInfoBuilder bio(String bio) { this.bio = bio; return this; }
            public UserInfoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
            public UserInfoBuilder isVerified(boolean isVerified) { this.isVerified = isVerified; return this; }
            public UserInfoBuilder isTwoFactorEnabled(boolean isTwoFactorEnabled) { this.isTwoFactorEnabled = isTwoFactorEnabled; return this; }
            
            public UserInfoDto build() {
                return new UserInfoDto(userId, username, nickname, email, avatar, phone, bio, 
                                     createdAt, isVerified, isTwoFactorEnabled);
            }
        }
    }
}


