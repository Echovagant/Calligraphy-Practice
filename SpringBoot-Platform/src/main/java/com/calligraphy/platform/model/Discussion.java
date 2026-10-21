package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "discussions", indexes = {
    @Index(name = "idx_discussion_user_id", columnList = "user_id"),
    @Index(name = "idx_discussion_category", columnList = "category"),
    @Index(name = "idx_discussion_status", columnList = "status"),
    @Index(name = "idx_discussion_created_at", columnList = "created_at"),
    @Index(name = "idx_discussions_user_status", columnList = "user_id, status"),
    @Index(name = "idx_discussions_category_status", columnList = "category, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discussion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @NotBlank(message = "讨论标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @NotBlank(message = "讨论内容不能为空")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @NotBlank(message = "讨论分类不能为空")
    @Size(max = 50, message = "分类长度不能超过50个字符")
    @Column(name = "category", length = 50, nullable = false)
    private String category;
    
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DiscussionStatus status = DiscussionStatus.ACTIVE;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;
    
    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "tags", length = 500)
    private String tags;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<DiscussionReply> replies = new ArrayList<>();
    
    // 状态枚举
    public enum DiscussionStatus {
        ACTIVE, INACTIVE, DELETED
    }
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (viewCount == null) viewCount = 0;
        if (likeCount == null) likeCount = 0;
        if (replyCount == null) replyCount = 0;
        if (isPinned == null) isPinned = false;
        if (isFeatured == null) isFeatured = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 业务方法
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }
    
    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }
    
    public void decrementLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
    }
    
    public void incrementReplyCount() {
        this.replyCount = (this.replyCount == null ? 0 : this.replyCount) + 1;
    }
    
    public void decrementReplyCount() {
        this.replyCount = Math.max(0, (this.replyCount == null ? 0 : this.replyCount) - 1);
    }
    
    public boolean isActive() {
        return DiscussionStatus.ACTIVE.equals(this.status);
    }
    
    public boolean isDeleted() {
        return DiscussionStatus.DELETED.equals(this.status);
    }
    
    // 添加缺少的getter和setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
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
    
    public DiscussionStatus getStatus() {
        return status;
    }
    
    public void setStatus(DiscussionStatus status) {
        this.status = status;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
    
    public Boolean getIsPinned() {
        return isPinned;
    }
    
    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }
    
    public Boolean getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
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
    
    public List<DiscussionReply> getReplies() {
        return replies;
    }
    
    public void setReplies(List<DiscussionReply> replies) {
        this.replies = replies;
    }
    
    // Builder class
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String title;
        private String content;
        private String category;
        private FrontendUser user;
        private String userId;
        private String username;
        private String userAvatar;
        private DiscussionStatus status = DiscussionStatus.ACTIVE;
        private Integer viewCount = 0;
        private Integer likeCount = 0;
        private Integer replyCount = 0;
        private Boolean isPinned = false;
        private Boolean isFeatured = false;
        private String tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<DiscussionReply> replies = new ArrayList<>();
        
        private Builder() {}
        
        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder user(FrontendUser user) { this.user = user; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder userAvatar(String userAvatar) { this.userAvatar = userAvatar; return this; }
        public Builder status(DiscussionStatus status) { this.status = status; return this; }
        public Builder viewCount(Integer viewCount) { this.viewCount = viewCount; return this; }
        public Builder likeCount(Integer likeCount) { this.likeCount = likeCount; return this; }
        public Builder replyCount(Integer replyCount) { this.replyCount = replyCount; return this; }
        public Builder isPinned(Boolean isPinned) { this.isPinned = isPinned; return this; }
        public Builder isFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; return this; }
        public Builder tags(String tags) { this.tags = tags; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder replies(List<DiscussionReply> replies) { this.replies = replies; return this; }
        
        public Discussion build() {
            Discussion discussion = new Discussion();
            discussion.id = this.id;
            discussion.title = this.title;
            discussion.content = this.content;
            discussion.category = this.category;
            discussion.user = this.user;
            discussion.userId = this.userId;
            discussion.username = this.username;
            discussion.userAvatar = this.userAvatar;
            discussion.status = this.status;
            discussion.viewCount = this.viewCount;
            discussion.likeCount = this.likeCount;
            discussion.replyCount = this.replyCount;
            discussion.isPinned = this.isPinned;
            discussion.isFeatured = this.isFeatured;
            discussion.tags = this.tags;
            discussion.createdAt = this.createdAt;
            discussion.updatedAt = this.updatedAt;
            discussion.replies = this.replies;
            return discussion;
        }
    }
}


