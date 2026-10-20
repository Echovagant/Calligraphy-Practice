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
    
    @NotBlank(message = "瑙掕壊浠ｇ爜涓嶈兘涓虹┖")
    @Size(max = 50, message = "瑙掕壊浠ｇ爜闀垮害涓嶈兘瓒呰繃50涓瓧绗")
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;
    
    @NotBlank(message = "瑙掕壊鍚嶇О涓嶈兘涓虹┖")
    @Size(max = 100, message = "瑙掕壊鍚嶇О长度不能超过100涓瓧绗")
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;
    
    @Size(max = 500, message = "角色描述长度不能超过500涓瓧绗")
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
    private Boolean isSystem = false; // 鏄惁涓虹郴缁熷唴缃鑹?    
    @Column(name = "level")
    private Integer level; // 瑙掕壊绛夌骇锛岀敤浜庢潈闄愭瘮杈?    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 澶氬澶氬叧绯伙細瑙掕壊涓庢潈闄
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
    
    // 棰勫畾涔夎鑹蹭唬鐮佸父閲
    public static class RoleCodes {
        // 鍓嶇用户角色
        public static final String FRONTEND_USER = "FRONTEND_USER";
        public static final String FRONTEND_VIP = "FRONTEND_VIP";
        public static final String FRONTEND_PREMIUM = "FRONTEND_PREMIUM";
        
        // 鍚庣绠＄悊鍛樿鑹
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


