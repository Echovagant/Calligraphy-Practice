package com.calligraphy.platform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "frontend_users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrontendUser {
    
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;
    
    @NotBlank(message = "鐢ㄦ埛鍚嶄笉鑳戒负绌?)
    @Size(min = 3, max = 50, message = "鐢ㄦ埛鍚嶉暱搴﹀繀椤诲湪3-50涓瓧绗︿箣闂?)
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;
    
    @NotBlank(message = "瀵嗙爜涓嶈兘涓虹┖")
    @Size(min = 6, max = 255, message = "瀵嗙爜闀垮害蹇呴』鍦?-255涓瓧绗︿箣闂?)
    @Column(name = "password", length = 255, nullable = false)
    @JsonIgnore
    private String password;
    
    @NotBlank(message = "閭涓嶈兘涓虹┖")
    @Email(message = "閭鏍煎紡涓嶆纭?)
    @Size(max = 100, message = "閭闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private FrontendUserRole role = FrontendUserRole.USER;
    
    @Size(max = 100, message = "濮撳悕闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    @Size(max = 500, message = "澶村儚URL闀垮害涓嶈兘瓒呰繃500涓瓧绗?)
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Size(max = 20, message = "鎵嬫満鍙烽暱搴︿笉鑳借秴杩?0涓瓧绗?)
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private FrontendUserStatus status = FrontendUserStatus.ACTIVE;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "login_count", nullable = false)
    @Builder.Default
    private Integer loginCount = 0;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 鍏宠仈鍏崇郴
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Work> works;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PracticeRecord> practiceRecords;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;
    
    @OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FileReview> fileReviews;
    
    // 瑙掕壊鏋氫妇
    public enum FrontendUserRole {
        USER, PREMIUM_USER
    }
    
    // 鐘舵€佹灇涓?    public enum FrontendUserStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
    
    // 棰勬寔涔呭寲鍥炶皟
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 涓氬姟鏂规硶
    public void incrementLoginCount() {
        this.loginCount = (this.loginCount == null ? 0 : this.loginCount) + 1;
        this.lastLogin = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return FrontendUserStatus.ACTIVE.equals(this.status);
    }
    
    public boolean isPremiumUser() {
        return FrontendUserRole.PREMIUM_USER.equals(this.role);
    }
    
    // Getter鍜孲etter鏂规硶
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
    
    public FrontendUserRole getRole() {
        return role;
    }
    
    public void setRole(FrontendUserRole role) {
        this.role = role;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
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
    
    // 瀹夊叏鏂规硶
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
    
    public String getMaskedPhone() {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}


