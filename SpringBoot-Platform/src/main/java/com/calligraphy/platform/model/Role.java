package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "name", unique = true),
    @Index(name = "idx_role_code", columnList = "code", unique = true),
    @Index(name = "idx_role_type", columnList = "role_type"),
    @Index(name = "idx_role_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @NotBlank(message = "角色代码不能为空")
    @Size(max = 50, message = "角色代码长度不能超过50个字符")
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;
    
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100个字符")
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;
    
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", length = 20, nullable = false)
    private RoleType roleType;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false; // 是否为系统内置角色     
    @Column(name = "level")
    private Integer level; // 角色等级，用于权限比较     
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 多对多关系：角色与权限
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"),
        indexes = {
            @Index(name = "idx_role_perm_role", columnList = "role_id"),
            @Index(name = "idx_role_perm_permission", columnList = "permission_id"),
            @Index(name = "idx_role_perm_unique", columnList = "role_id, permission_id", unique = true)
        }
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
    
    // 角色类型枚举
    public enum RoleType {
        FRONTEND_USER, BACKEND_ADMIN, SYSTEM
    }
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isSystem == null) {
            isSystem = false;
        }
        if (level == null) {
            level = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 业务方法
    public boolean hasPermission(String permissionCode) {
        if (permissions == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(permission -> permission.getCode().equals(permissionCode));
    }
    
    public boolean hasPermission(Permission permission) {
        if (permissions == null || permission == null) {
            return false;
        }
        return permissions.contains(permission);
    }
    
    public boolean hasAnyPermission(String... permissionCodes) {
        if (permissions == null || permissionCodes == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(permission -> {
                    for (String code : permissionCodes) {
                        if (permission.getCode().equals(code)) {
                            return true;
                        }
                    }
                    return false;
                });
    }
    
    public boolean hasAllPermissions(String... permissionCodes) {
        if (permissions == null || permissionCodes == null) {
            return false;
        }
        Set<String> permissionCodeSet = new HashSet<>();
        permissions.forEach(permission -> permissionCodeSet.add(permission.getCode()));
        
        for (String code : permissionCodes) {
            if (!permissionCodeSet.contains(code)) {
                return false;
            }
        }
        return true;
    }
    
    public void addPermission(Permission permission) {
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        if (permission != null) {
            permissions.add(permission);
        }
    }
    
    public void removePermission(Permission permission) {
        if (permissions != null && permission != null) {
            permissions.remove(permission);
        }
    }
    
    public void clearPermissions() {
        if (permissions != null) {
            permissions.clear();
        }
    }
    
    public boolean isFrontendRole() {
        return RoleType.FRONTEND_USER.equals(this.roleType);
    }
    
    public boolean isBackendRole() {
        return RoleType.BACKEND_ADMIN.equals(this.roleType);
    }
    
    public boolean isSystemRole() {
        return RoleType.SYSTEM.equals(this.roleType);
    }
    
    public boolean canAccessAdminPanel() {
        return isBackendRole() && isActive;
    }
    
    public int getPermissionCount() {
        return permissions != null ? permissions.size() : 0;
    }
    
    // 预定义角色代码常量
    public static class RoleCodes {
        // 前端用户角色
        public static final String FRONTEND_USER = "FRONTEND_USER";
        public static final String FRONTEND_VIP = "FRONTEND_VIP";
        public static final String FRONTEND_PREMIUM = "FRONTEND_PREMIUM";
        
        // 后端管理员角色
        public static final String BACKEND_ADMIN = "BACKEND_ADMIN";
        public static final String BACKEND_SUPER_ADMIN = "BACKEND_SUPER_ADMIN";
        public static final String BACKEND_CONTENT_MODERATOR = "BACKEND_CONTENT_MODERATOR";
        public static final String BACKEND_SYSTEM_ADMIN = "BACKEND_SYSTEM_ADMIN";
        public static final String BACKEND_AUDITOR = "BACKEND_AUDITOR";
        
        // 系统角色
        public static final String SYSTEM_GUEST = "SYSTEM_GUEST";
        public static final String SYSTEM_MAINTENANCE = "SYSTEM_MAINTENANCE";
    }
}


