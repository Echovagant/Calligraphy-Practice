package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "backend_users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_account_locked", columnList = "account_locked"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackendUser {
    
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 255, message = "密码长度必须在8-255个字符之间")
    @Column(name = "password", length = 255, nullable = false)
    @JsonIgnore
    private String password;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private BackendUserRole role;
    
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    @Size(max = 100, message = "部门长度不能超过100个字符")
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "permissions", columnDefinition = "JSON")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> permissions;
    
    @Column(name = "security_level", nullable = false)
    @Builder.Default
    private Integer securityLevel = 1;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;
    
    @Column(name = "lock_until")
    private LocalDateTime lockUntil;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "last_password_reset")
    private LocalDateTime lastPasswordReset;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 关联关系
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Work> reviewedWorks;
    
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> reviewedComments;
    
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FileReview> reviewedFiles;

    // Getter和Setter方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public BackendUserRole getRole() {
        return role;
    }
    
    public void setRole(BackendUserRole role) {
        this.role = role;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public Map<String, Object> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Map<String, Object> permissions) {
        this.permissions = permissions;
    }
    
    public Integer getSecurityLevel() {
        return securityLevel;
    }
    
    public void setSecurityLevel(Integer securityLevel) {
        this.securityLevel = securityLevel;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
    
    public Boolean getAccountLocked() {
        return accountLocked;
    }
    
    public void setAccountLocked(Boolean accountLocked) {
        this.accountLocked = accountLocked;
    }
    
    public LocalDateTime getLockUntil() {
        return lockUntil;
    }
    
    public void setLockUntil(LocalDateTime lockUntil) {
        this.lockUntil = lockUntil;
    }
    
    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }
    
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }
    
    public LocalDateTime getLastPasswordReset() {
        return lastPasswordReset;
    }
    
    public void setLastPasswordReset(LocalDateTime lastPasswordReset) {
        this.lastPasswordReset = lastPasswordReset;
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
    
    public List<Work> getReviewedWorks() {
        return reviewedWorks;
    }
    
    public void setReviewedWorks(List<Work> reviewedWorks) {
        this.reviewedWorks = reviewedWorks;
    }
    
    public List<Comment> getReviewedComments() {
        return reviewedComments;
    }
    
    public void setReviewedComments(List<Comment> reviewedComments) {
        this.reviewedComments = reviewedComments;
    }
    
    public List<FileReview> getReviewedFiles() {
        return reviewedFiles;
    }
    
    public void setReviewedFiles(List<FileReview> reviewedFiles) {
        this.reviewedFiles = reviewedFiles;
    }
    
    // 角色枚举
    public enum BackendUserRole {
        ADMIN, SUPER_ADMIN
    }
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (passwordChangedAt == null) {
            passwordChangedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 安全相关方法
    public boolean isAccountNonLocked() {
        return !accountLocked || (lockUntil != null && LocalDateTime.now().isAfter(lockUntil));
    }
    
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
    }
    
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
        this.lockUntil = null;
    }
    
    public void lockAccount(int lockMinutes) {
        this.accountLocked = true;
        this.lockUntil = LocalDateTime.now().plusMinutes(lockMinutes);
    }
    
    public boolean isSuperAdmin() {
        return BackendUserRole.SUPER_ADMIN.equals(this.role);
    }
    
    public boolean hasPermission(String permission) {
        if (isSuperAdmin()) {
            return true;
        }
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.containsKey(permission) && Boolean.TRUE.equals(permissions.get(permission));
    }
    
    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
        this.lastPasswordReset = LocalDateTime.now();
        resetFailedLoginAttempts();
    }
    
    // 业务方法
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
        resetFailedLoginAttempts();
    }

    // 安全方法
    @JsonProperty
    public String getMaskedEmail() {
        if (email == null || email.length() < 3) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
    
    public boolean isPasswordExpired(int maxPasswordAgeDays) {
        if (passwordChangedAt == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(passwordChangedAt.plusDays(maxPasswordAgeDays));
    }
}


