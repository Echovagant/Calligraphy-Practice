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

@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_code", columnList = "code", unique = true),
    @Index(name = "idx_permission_type", columnList = "permission_type"),
    @Index(name = "idx_permission_module", columnList = "module"),
    @Index(name = "idx_permission_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @NotBlank(message = "权限代码不能为空")
    @Size(max = 50, message = "权限代码长度不能超过50个字符")
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;
    
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Size(max = 500, message = "权限描述长度不能超过500个字符")
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", length = 20, nullable = false)
    private PermissionType permissionType;
    
    @Size(max = 50, message = "模块名称长度不能超过50个字符")
    @Column(name = "module", length = 50)
    private String module;
    
    @Column(name = "module_order")
    private Integer moduleOrder; // 模块排序
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 多对多关系：权限与角色
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;
    
    // 权限类型枚举
    public enum PermissionType {
        CREATE, READ, UPDATE, DELETE, EXECUTE, ADMIN
    }
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (moduleOrder == null) {
            moduleOrder = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 基础getter方法
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    // 业务方法
    public boolean isCreatePermission() {
        return PermissionType.CREATE.equals(this.permissionType);
    }
    
    public boolean isReadPermission() {
        return PermissionType.READ.equals(this.permissionType);
    }
    
    public boolean isUpdatePermission() {
        return PermissionType.UPDATE.equals(this.permissionType);
    }
    
    public boolean isDeletePermission() {
        return PermissionType.DELETE.equals(this.permissionType);
    }
    
    public boolean isExecutePermission() {
        return PermissionType.EXECUTE.equals(this.permissionType);
    }
    
    public boolean isAdminPermission() {
        return PermissionType.ADMIN.equals(this.permissionType);
    }
    
    public String getFullCode() {
        if (module != null && !module.trim().isEmpty()) {
            return module.toUpperCase() + ":" + code;
        }
        return code;
    }
    
    public String getTypeDisplay() {
        switch (permissionType) {
            case CREATE:
                return "创建";
            case READ:
                return "读取";
            case UPDATE:
                return "更新";
            case DELETE:
                return "删除";
            case EXECUTE:
                return "执行";
            case ADMIN:
                return "管理";
            default:
                return "未知";
        }
    }
    
    // 预定义权限代码常量
    public static class PermissionCodes {
        // 用户管理权限
        public static final String USER_CREATE = "USER_CREATE";
        public static final String USER_READ = "USER_READ";
        public static final String USER_UPDATE = "USER_UPDATE";
        public static final String USER_DELETE = "USER_DELETE";
        
        // 管理员管理权限
        public static final String ADMIN_CREATE = "ADMIN_CREATE";
        public static final String ADMIN_READ = "ADMIN_READ";
        public static final String ADMIN_UPDATE = "ADMIN_UPDATE";
        public static final String ADMIN_DELETE = "ADMIN_DELETE";
        
        // 作品管理权限
        public static final String WORK_CREATE = "WORK_CREATE";
        public static final String WORK_READ = "WORK_READ";
        public static final String WORK_UPDATE = "WORK_UPDATE";
        public static final String WORK_DELETE = "WORK_DELETE";
        public static final String WORK_REVIEW = "WORK_REVIEW";
        
        // 评论管理权限
        public static final String COMMENT_CREATE = "COMMENT_CREATE";
        public static final String COMMENT_READ = "COMMENT_READ";
        public static final String COMMENT_UPDATE = "COMMENT_UPDATE";
        public static final String COMMENT_DELETE = "COMMENT_DELETE";
        public static final String COMMENT_MODERATE = "COMMENT_MODERATE";
        
        // 文件审核权限
        public static final String FILE_UPLOAD = "FILE_UPLOAD";
        public static final String FILE_REVIEW = "FILE_REVIEW";
        public static final String FILE_APPROVE = "FILE_APPROVE";
        public static final String FILE_REJECT = "FILE_REJECT";
        
        // 系统管理权限
        public static final String SYSTEM_CONFIG = "SYSTEM_CONFIG";
        public static final String SYSTEM_MONITOR = "SYSTEM_MONITOR";
        public static final String SYSTEM_LOG = "SYSTEM_LOG";
        public static final String SYSTEM_BACKUP = "SYSTEM_BACKUP";
        
        // 练习记录权限
        public static final String PRACTICE_CREATE = "PRACTICE_CREATE";
        public static final String PRACTICE_READ = "PRACTICE_READ";
        public static final String PRACTICE_UPDATE = "PRACTICE_UPDATE";
        public static final String PRACTICE_DELETE = "PRACTICE_DELETE";
        
        // 活动日志权限
        public static final String LOG_READ = "LOG_READ";
        public static final String LOG_EXPORT = "LOG_EXPORT";
        public static final String LOG_DELETE = "LOG_DELETE";
    }
}


