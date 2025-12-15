package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_type", columnList = "user_type"),
    @Index(name = "idx_activity_type", columnList = "activity_type"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_ip_address", columnList = "ip_address"),
    @Index(name = "idx_user_activity_time", columnList = "user_id, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 20, nullable = false)
    private UserType userType;
    
    @NotBlank(message = "娲诲姩绫诲瀷涓嶈兘涓虹┖")
    @Size(max = 50, message = "娲诲姩绫诲瀷闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    @Column(name = "activity_type", length = 50, nullable = false)
    private String activityType;
    
    @Column(name = "activity_description", columnDefinition = "TEXT")
    private String activityDescription;
    
    @Size(max = 100, message = "IP鍦板潃闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Column(name = "ip_address", length = 100)
    private String ipAddress;
    
    @Size(max = 500, message = "鐢ㄦ埛浠ｇ悊闀垮害涓嶈兘瓒呰繃500涓瓧绗?)
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Size(max = 50, message = "璁惧绫诲瀷闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Size(max = 100, message = "鍦扮悊浣嶇疆闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Column(name = "location", length = 100)
    private String location;
    
    @Column(name = "request_url", length = 500)
    private String requestUrl;
    
    @Size(max = 10, message = "HTTP鏂规硶闀垮害涓嶈兘瓒呰繃10涓瓧绗?)
    @Column(name = "http_method", length = 10)
    private String httpMethod;
    
    // 鍝嶅簲鐘舵€佹灇涓?    public enum ResponseStatus {
        SUCCESS, ERROR, PENDING, TIMEOUT
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "response_status_enum", length = 20)
    private ResponseStatus responseStatusEnum;
    
    @Min(value = 0, message = "鍝嶅簲鏃堕棿涓嶈兘涓鸿礋鏁?)
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Convert(converter = JsonConverter.class)
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private java.util.Map<String, Object> additionalData;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 鐢ㄦ埛绫诲瀷鏋氫妇
    public enum UserType {
        FRONTEND, BACKEND
    }
    
    // 棰勬寔涔呭寲鍥炶皟
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 涓氬姟鏂规硶
    public boolean isSuccessful() {
        return responseStatusEnum == ResponseStatus.SUCCESS;
    }
    
    public boolean isError() {
        return responseStatusEnum == ResponseStatus.ERROR;
    }
    
    public String getResponseStatusCategory() {
        if (responseStatusEnum == null) {
            return "鏈煡";
        }
        
        switch (responseStatusEnum) {
            case SUCCESS:
                return "鎴愬姛";
            case ERROR:
                return "閿欒";
            case PENDING:
                return "寰呭鐞?;
            case TIMEOUT:
                return "瓒呮椂";
            default:
                return "鏈煡";
        }
    }
    
    public void addAdditionalData(String key, Object value) {
        if (this.additionalData == null) {
            this.additionalData = new java.util.HashMap<>();
        }
        this.additionalData.put(key, value);
    }
    
    public Object getAdditionalData(String key) {
        if (this.additionalData == null) {
            return null;
        }
        return this.additionalData.get(key);
    }
    
    public static class ActivityTypes {
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String REGISTER = "REGISTER";
        public static final String PASSWORD_RESET = "PASSWORD_RESET";
        public static final String PROFILE_UPDATE = "PROFILE_UPDATE";
        public static final String FILE_UPLOAD = "FILE_UPLOAD";
        public static final String FILE_DOWNLOAD = "FILE_DOWNLOAD";
        public static final String FILE_REVIEW = "FILE_REVIEW";
        public static final String WORK_CREATE = "WORK_CREATE";
        public static final String WORK_UPDATE = "WORK_UPDATE";
        public static final String WORK_DELETE = "WORK_DELETE";
        public static final String COMMENT_CREATE = "COMMENT_CREATE";
        public static final String COMMENT_UPDATE = "COMMENT_UPDATE";
        public static final String COMMENT_DELETE = "COMMENT_DELETE";
        public static final String PRACTICE_START = "PRACTICE_START";
        public static final String PRACTICE_END = "PRACTICE_END";
        public static final String ADMIN_LOGIN = "ADMIN_LOGIN";
        public static final String ADMIN_ACTION = "ADMIN_ACTION";
        public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
        public static final String FAILED_LOGIN = "FAILED_LOGIN";
        public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
        public static final String PASSWORD_CHANGE = "PASSWORD_CHANGE";
        public static final String EMAIL_VERIFICATION = "EMAIL_VERIFICATION";
        public static final String TWO_FACTOR_AUTH = "TWO_FACTOR_AUTH";
    }
}


