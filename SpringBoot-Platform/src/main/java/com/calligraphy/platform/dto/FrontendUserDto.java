package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class FrontendUserDto {
    
    private String userId;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String phone;
    private String bio;
    private String preferences;
    private User.UserStatus status;
    private boolean isVerified;
    private boolean isTwoFactorEnabled;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private long practiceCount;
    private long workCount;
    private long commentCount;
    private long totalLikesReceived;
    private long totalViewsReceived;
    
    // 角色信息
    private List<String> roles;
    
    // 活动信息
    private LocalDateTime lastActivityAt;
    private String lastActivityType;
    
    // 无参构造器
    public FrontendUserDto() {
    }
    
    // 全参构造器
    public FrontendUserDto(String userId, String username, String email, String nickname, String avatar, String phone, 
                          String bio, String preferences, User.UserStatus status, boolean isVerified, 
                          boolean isTwoFactorEnabled, LocalDateTime lastLoginAt, String lastLoginIp, 
                          int failedLoginAttempts, LocalDateTime lockedUntil, LocalDateTime createdAt, 
                          LocalDateTime updatedAt, long practiceCount, long workCount, long commentCount, 
                          long totalLikesReceived, long totalViewsReceived, List<String> roles, 
                          LocalDateTime lastActivityAt, String lastActivityType) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.avatar = avatar;
        this.phone = phone;
        this.bio = bio;
        this.preferences = preferences;
        this.status = status;
        this.isVerified = isVerified;
        this.isTwoFactorEnabled = isTwoFactorEnabled;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.practiceCount = practiceCount;
        this.workCount = workCount;
        this.commentCount = commentCount;
        this.totalLikesReceived = totalLikesReceived;
        this.totalViewsReceived = totalViewsReceived;
        this.roles = roles;
        this.lastActivityAt = lastActivityAt;
        this.lastActivityType = lastActivityType;
    }
    
    // Getter 和 Setter 方法
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public String getPreferences() {
        return preferences;
    }
    
    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    
    public User.UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(User.UserStatus status) {
        this.status = status;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public boolean isTwoFactorEnabled() {
        return isTwoFactorEnabled;
    }
    
    public void setTwoFactorEnabled(boolean isTwoFactorEnabled) {
        this.isTwoFactorEnabled = isTwoFactorEnabled;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public String getLastLoginIp() {
        return lastLoginIp;
    }
    
    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
    
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
    
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public long getPracticeCount() {
        return practiceCount;
    }
    
    public void setPracticeCount(long practiceCount) {
        this.practiceCount = practiceCount;
    }
    
    public long getWorkCount() {
        return workCount;
    }
    
    public void setWorkCount(long workCount) {
        this.workCount = workCount;
    }
    
    public long getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }
    
    public long getTotalLikesReceived() {
        return totalLikesReceived;
    }
    
    public void setTotalLikesReceived(long totalLikesReceived) {
        this.totalLikesReceived = totalLikesReceived;
    }
    
    public long getTotalViewsReceived() {
        return totalViewsReceived;
    }
    
    public void setTotalViewsReceived(long totalViewsReceived) {
        this.totalViewsReceived = totalViewsReceived;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }
    
    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
    
    public String getLastActivityType() {
        return lastActivityType;
    }
    
    public void setLastActivityType(String lastActivityType) {
        this.lastActivityType = lastActivityType;
    }
    
    // Builder 类
    public static class Builder {
        private String userId;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String phone;
        private String bio;
        private String preferences;
        private User.UserStatus status;
        private boolean isVerified;
        private boolean isTwoFactorEnabled;
        private LocalDateTime lastLoginAt;
        private String lastLoginIp;
        private int failedLoginAttempts;
        private LocalDateTime lockedUntil;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private long practiceCount;
        private long workCount;
        private long commentCount;
        private long totalLikesReceived;
        private long totalViewsReceived;
        private List<String> roles;
        private LocalDateTime lastActivityAt;
        private String lastActivityType;
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }
        
        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }
        
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }
        
        public Builder preferences(String preferences) {
            this.preferences = preferences;
            return this;
        }
        
        public Builder status(User.UserStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder verified(boolean isVerified) {
            this.isVerified = isVerified;
            return this;
        }
        
        public Builder twoFactorEnabled(boolean isTwoFactorEnabled) {
            this.isTwoFactorEnabled = isTwoFactorEnabled;
            return this;
        }
        
        public Builder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }
        
        public Builder lastLoginIp(String lastLoginIp) {
            this.lastLoginIp = lastLoginIp;
            return this;
        }
        
        public Builder failedLoginAttempts(int failedLoginAttempts) {
            this.failedLoginAttempts = failedLoginAttempts;
            return this;
        }
        
        public Builder lockedUntil(LocalDateTime lockedUntil) {
            this.lockedUntil = lockedUntil;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder practiceCount(long practiceCount) {
            this.practiceCount = practiceCount;
            return this;
        }
        
        public Builder workCount(long workCount) {
            this.workCount = workCount;
            return this;
        }
        
        public Builder commentCount(long commentCount) {
            this.commentCount = commentCount;
            return this;
        }
        
        public Builder totalLikesReceived(long totalLikesReceived) {
            this.totalLikesReceived = totalLikesReceived;
            return this;
        }
        
        public Builder totalViewsReceived(long totalViewsReceived) {
            this.totalViewsReceived = totalViewsReceived;
            return this;
        }
        
        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }
        
        public Builder lastActivityAt(LocalDateTime lastActivityAt) {
            this.lastActivityAt = lastActivityAt;
            return this;
        }
        
        public Builder lastActivityType(String lastActivityType) {
            this.lastActivityType = lastActivityType;
            return this;
        }
        
        public FrontendUserDto build() {
            return new FrontendUserDto(userId, username, email, nickname, avatar, phone, bio, preferences, status, 
                                      isVerified, isTwoFactorEnabled, lastLoginAt, lastLoginIp, failedLoginAttempts, 
                                      lockedUntil, createdAt, updatedAt, practiceCount, workCount, commentCount, 
                                      totalLikesReceived, totalViewsReceived, roles, lastActivityAt, lastActivityType);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


