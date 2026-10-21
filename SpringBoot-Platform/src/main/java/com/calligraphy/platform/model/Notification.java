package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_recipient_id", columnList = "recipient_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_is_read", columnList = "is_read"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_recipient_read", columnList = "recipient_id, is_read")
})
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "接收者ID不能为空")
    @Column(name = "recipient_id", length = 36, nullable = false)
    private String recipientId;
    
    @Column(name = "recipient_type", length = 20)
    private String recipientType;
    
    @NotBlank(message = "通知标题不能为空")
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @NotBlank(message = "通知内容不能为空")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @NotBlank(message = "通知类型不能为空")
    @Column(name = "type", length = 50, nullable = false)
    private String type;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "sender_name", length = 100)
    private String senderName;
    
    @Column(name = "related_id", length = 36)
    private String relatedId;
    
    @Column(name = "related_type", length = 50)
    private String relatedType;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    // 无参构造器
    public Notification() {
    }
    
    // 全参构造器
    public Notification(Long id, String recipientId, String recipientType, String title, String content, String type, 
                       Boolean isRead, LocalDateTime readAt, String senderName, String relatedId, String relatedType, 
                       Integer priority, Boolean isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt, 
                       LocalDateTime expiresAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.recipientType = recipientType;
        this.title = title;
        this.content = content;
        this.type = type;
        this.isRead = isRead;
        this.readAt = readAt;
        this.senderName = senderName;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.priority = priority;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
    
    public String getRecipientType() {
        return recipientType;
    }
    
    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getRelatedId() {
        return relatedId;
    }
    
    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }
    
    public String getRelatedType() {
        return relatedType;
    }
    
    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
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
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (priority == null) {
            priority = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Builder 类
    public static class Builder {
        private Long id;
        private String recipientId;
        private String recipientType;
        private String title;
        private String content;
        private String type;
        private Boolean isRead = false;
        private LocalDateTime readAt;
        private String senderName;
        private String relatedId;
        private String relatedType;
        private Integer priority = 0;
        private Boolean isDeleted = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime expiresAt;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder recipientId(String recipientId) {
            this.recipientId = recipientId;
            return this;
        }
        
        public Builder recipientType(String recipientType) {
            this.recipientType = recipientType;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder isRead(Boolean isRead) {
            this.isRead = isRead;
            return this;
        }
        
        public Builder readAt(LocalDateTime readAt) {
            this.readAt = readAt;
            return this;
        }
        
        public Builder senderName(String senderName) {
            this.senderName = senderName;
            return this;
        }
        
        public Builder relatedId(String relatedId) {
            this.relatedId = relatedId;
            return this;
        }
        
        public Builder relatedType(String relatedType) {
            this.relatedType = relatedType;
            return this;
        }
        
        public Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder isDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
        
        public Notification build() {
            return new Notification(id, recipientId, recipientType, title, content, type, isRead, readAt, senderName, 
                                  relatedId, relatedType, priority, isDeleted, createdAt, updatedAt, expiresAt);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


