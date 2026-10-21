package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.Role;
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
public class RoleDto {
    
    private String roleId;
    private String roleName;
    private String roleCode;
    private String description;
    private Role.RoleType type;
    private boolean isSystem;
    private boolean isActive;
    private int displayOrder;
    private String parentRoleId;
    private String parentRoleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 权限信息
    private List<PermissionDto> permissions;
    private int permissionCount;
    
    // 用户信息
    private long userCount;
    private long frontendUserCount;
    private long backendUserCount;
}


