package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.Permission;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {
    
    private String permissionId;
    private String permissionName;
    private String permissionCode;
    private String description;
    private String module;
    private String category;
    private Permission.PermissionType permissionType;
    private String resource;
    private String action;
    private boolean isActive;
    private boolean isSystem;
    private int displayOrder;
    private String parentPermissionId;
    private String parentPermissionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private long roleCount;
    private long userCount;
}


