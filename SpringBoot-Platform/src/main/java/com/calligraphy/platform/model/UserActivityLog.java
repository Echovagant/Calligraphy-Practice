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
    
    @NotBlank(message = "活动类型不能为空")
    @Size(max = 50, message = "活动类型长度不能超过50个字符")
    @Column(name = "activity_type", length = 50, nullable = false)
    private String activityType;
    
    @Column(name = "activity_description", columnDefinition = "TEXT")
    private String activityDescription;
    
    @Size(max = 100, message = "IP地址长度不能超过100个字符")
    @Column(name = "ip_address", length = 100)
    private String ipAddress;
    
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Size(max = 50, message = "设备类型长度不能超过50个字符")
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Size(max = 100, message = "地理位置长度不能超过100个字符")
    @Column(name = "location", length = 100)
    private String location;
    
    @Column(name = "request_url", length = 500)
    private String requestUrl;
    
    @Size(max = 10, message = "HTTP方法长度不能超过10个字符")
    @Column(name = "http_method", length = 10)
    private String httpMethod;
    
    // 响应状态枚举
    public enum ResponseStatus {
        SUCCESS, ERROR, PENDING, TIMEOUT
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "response_status_enum", length = 20)
    private ResponseStatus responseStatusEnum;
    
    @Min(value = 0, message = "响应时间不能为负数")
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Convert(converter = JsonConverter.class)
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private java.util.Map<String, Object> additionalData;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 用户类型枚举
    public enum UserType {
        FRONTEND, BACKEND
    }
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // 业务方法
    public boolean isSuccessful() {
        return responseStatusEnum == ResponseStatus.SUCCESS;
    }
    
    public boolean isError() {
        return responseStatusEnum == ResponseStatus.ERROR;
    }
    
    public String getResponseStatusCategory() {
        if (responseStatusEnum == null) {
            return "未知";
        }
        
        switch (responseStatusEnum) {
            case SUCCESS:
                return "成功";
            case ERROR:
                return "错误";
            case PENDING:
                return "待处理";
            case TIMEOUT:
                return "超时";
            default:
                return "未知";
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


