package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "discussion_replies", indexes = {
    @Index(name = "idx_reply_discussion_id", columnList = "discussion_id"),
    @Index(name = "idx_reply_user_id", columnList = "user_id"),
    @Index(name = "idx_reply_parent_id", columnList = "parent_id"),
    @Index(name = "idx_reply_created_at", columnList = "created_at"),
    @Index(name = "idx_replies_discussion_status", columnList = "discussion_id, status")
})
public class DiscussionReply {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @NotBlank(message = "回复内容不能为空")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", nullable = false)
    @JsonIgnore
    private Discussion discussion;
    
    @Column(name = "discussion_id", nullable = false, insertable = false, updatable = false)
    private Long discussionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private FrontendUser user;
    
    @Column(name = "user_id", length = 36, nullable = false, insertable = false, updatable = false)
    private String userId;
    
    @Column(name = "username", length = 100)
    private String username;
    
    @Column(name = "user_avatar", length = 500)
    private String userAvatar;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "reply_to_user_id", length = 36)
    private String replyToUserId;
    
    @Column(name = "reply_to_username", length = 100)
    private String replyToUsername;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReplyStatus status = ReplyStatus.ACTIVE;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 状态枚举
    public enum ReplyStatus {
        ACTIVE, INACTIVE, DELETED
    }
    
    // 无参构造器
    public DiscussionReply() {
    }
    
    // 全参构造器
    public DiscussionReply(Long id, String content, Discussion discussion, Long discussionId, FrontendUser user, 
                          String userId, String username, String userAvatar, Long parentId, String replyToUserId, 
                          String replyToUsername, ReplyStatus status, Integer likeCount, LocalDateTime createdAt, 
                          LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.discussion = discussion;
        this.discussionId = discussionId;
        this.user = user;
        this.userId = userId;
        this.username = username;
        this.userAvatar = userAvatar;
        this.parentId = parentId;
        this.replyToUserId = replyToUserId;
        this.replyToUsername = replyToUsername;
        this.status = status;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Discussion getDiscussion() {
        return discussion;
    }
    
    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }
    
    public Long getDiscussionId() {
        return discussionId;
    }
    
    public void setDiscussionId(Long discussionId) {
        this.discussionId = discussionId;
    }
    
    public FrontendUser getUser() {
        return user;
    }
    
    public void setUser(FrontendUser user) {
        this.user = user;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserAvatar() {
        return userAvatar;
    }
    
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public String getReplyToUserId() {
        return replyToUserId;
    }
    
    public void setReplyToUserId(String replyToUserId) {
        this.replyToUserId = replyToUserId;
    }
    
    public String getReplyToUsername() {
        return replyToUsername;
    }
    
    public void setReplyToUsername(String replyToUsername) {
        this.replyToUsername = replyToUsername;
    }
    
    public ReplyStatus getStatus() {
        return status;
    }
    
    public void setStatus(ReplyStatus status) {
        this.status = status;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
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
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 业务方法
    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }
    
    public void decrementLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
    }
    
    public boolean isActive() {
        return ReplyStatus.ACTIVE.equals(this.status);
    }
    
    public boolean isDeleted() {
        return ReplyStatus.DELETED.equals(this.status);
    }
    
    // Builder 类
    public static class Builder {
        private Long id;
        private String content;
        private Discussion discussion;
        private Long discussionId;
        private FrontendUser user;
        private String userId;
        private String username;
        private String userAvatar;
        private Long parentId;
        private String replyToUserId;
        private String replyToUsername;
        private ReplyStatus status = ReplyStatus.ACTIVE;
        private Integer likeCount = 0;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder discussion(Discussion discussion) {
            this.discussion = discussion;
            return this;
        }
        
        public Builder discussionId(Long discussionId) {
            this.discussionId = discussionId;
            return this;
        }
        
        public Builder user(FrontendUser user) {
            this.user = user;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder userAvatar(String userAvatar) {
            this.userAvatar = userAvatar;
            return this;
        }
        
        public Builder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }
        
        public Builder replyToUserId(String replyToUserId) {
            this.replyToUserId = replyToUserId;
            return this;
        }
        
        public Builder replyToUsername(String replyToUsername) {
            this.replyToUsername = replyToUsername;
            return this;
        }
        
        public Builder status(ReplyStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder likeCount(Integer likeCount) {
            this.likeCount = likeCount;
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
        
        public DiscussionReply build() {
            return new DiscussionReply(id, content, discussion, discussionId, user, userId, username, userAvatar, 
                                     parentId, replyToUserId, replyToUsername, status, likeCount, createdAt, updatedAt);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


