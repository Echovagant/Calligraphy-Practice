package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.BackendUser;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackendUserDto {
    
    private String userId;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String phone;
    private String department;
    private String position;
    private String bio;
    private BackendUser.BackendUserRole role;
    private boolean isVerified;
    private boolean isTwoFactorEnabled;

    public BackendUser.BackendUserRole getRole() {
        return role;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public boolean isTwoFactorEnabled() {
        return isTwoFactorEnabled;
    }
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private long reviewCount;
    private long approvedCount;
    private long rejectedCount;
    private long revisionRequestedCount;
    
    // 权限信息
    private List<String> permissions;
    private List<String> roles;
    
    // 活动信息
    private LocalDateTime lastActivityAt;
    private String lastActivityType;
}


