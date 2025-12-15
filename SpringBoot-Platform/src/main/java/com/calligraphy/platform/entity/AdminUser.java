package com.calligraphy.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "admin_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password;
    
    @Column(name = "nickname", length = 50)
    private String nickname;
    
    @Column(name = "roles", nullable = false, length = 255)
    private String roles;
    
    @ElementCollection
    @CollectionTable(name = "admin_user_permissions", joinColumns = @JoinColumn(name = "admin_user_id"))
    @MapKeyColumn(name = "permission_key")
    @Column(name = "permission_value")
    private Map<String, Boolean> permissions;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isEmailVerified == null) {
            isEmailVerified = false;
        }
        if (roles == null) {
            roles = "ROLE_ADMIN";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 娣诲姞蹇呰鐨刧etter鏂规硶
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getRoles() {
        return roles;
    }
    
    public void setRoles(String roles) {
        this.roles = roles;
    }
    
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }
    
    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
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
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    // Builder class
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String password;
        private String nickname;
        private String roles;
        private Map<String, Boolean> permissions;
        private Boolean isActive;
        private Boolean isEmailVerified;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastLogin;
        
        private Builder() {}
        
        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder roles(String roles) { this.roles = roles; return this; }
        public Builder permissions(Map<String, Boolean> permissions) { this.permissions = permissions; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder isEmailVerified(Boolean isEmailVerified) { this.isEmailVerified = isEmailVerified; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder lastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; return this; }
        
        public AdminUser build() {
            AdminUser adminUser = new AdminUser();
            adminUser.id = this.id;
            adminUser.username = this.username;
            adminUser.email = this.email;
            adminUser.password = this.password;
            adminUser.nickname = this.nickname;
            adminUser.roles = this.roles;
            adminUser.permissions = this.permissions;
            adminUser.isActive = this.isActive;
            adminUser.isEmailVerified = this.isEmailVerified;
            adminUser.createdAt = this.createdAt;
            adminUser.updatedAt = this.updatedAt;
            adminUser.lastLogin = this.lastLogin;
            return adminUser;
        }
    }
}


