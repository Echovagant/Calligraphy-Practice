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

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_work_id", columnList = "work_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_comments_user_status", columnList = "user_id, status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @NotBlank(message = "璇勮鍐呭涓嶈兘涓虹┖")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private FrontendUser user;
    
    @Column(name = "user_id", length = 36, nullable = false, insertable = false, updatable = false)
    private String userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    @JsonIgnore
    private Work work;
    
    @Column(name = "work_id", nullable = false, insertable = false, updatable = false)
    private Long workId;

    @Column(name = "parent_id")
    private Long parentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private CommentStatus status = CommentStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    @JsonIgnore
    private BackendUser reviewer;
    
    @Column(name = "reviewer_id", length = 36, insertable = false, updatable = false)
    private String reviewerId;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;
    
    @Column(name = "review_time")
    private LocalDateTime reviewTime;
    
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 鐘舵€佹灇涓
    public enum CommentStatus {
        PENDING, APPROVED, REJECTED, DELETED
    }
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
        if (isFeatured == null) isFeatured = false;
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
    
    public void approve(BackendUser reviewer, String notes) {
        this.reviewer = reviewer;
        this.reviewerId = reviewer.getId();
        this.status = CommentStatus.APPROVED;
        this.reviewNotes = notes;
        this.reviewTime = LocalDateTime.now();
    }
    
    public void reject(BackendUser reviewer, String notes) {
        this.reviewer = reviewer;
        this.reviewerId = reviewer.getId();
        this.status = CommentStatus.REJECTED;
        this.reviewNotes = notes;
        this.reviewTime = LocalDateTime.now();
    }
    
    public boolean isPublic() {
        return CommentStatus.APPROVED.equals(this.status);
    }
    
    public boolean canBeReviewed() {
        return CommentStatus.PENDING.equals(this.status);
    }
    
    // 娣诲姞缂哄皯鐨刧etter鍜宻etter方法
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
    
    public Work getWork() {
        return work;
    }
    
    public void setWork(Work work) {
        this.work = work;
    }
    
    public Long getWorkId() {
        return workId;
    }
    
    public void setWorkId(Long workId) {
        this.workId = workId;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public CommentStatus getStatus() {
        return status;
    }
    
    public void setStatus(CommentStatus status) {
        this.status = status;
    }
    
    public BackendUser getReviewer() {
        return reviewer;
    }
    
    public void setReviewer(BackendUser reviewer) {
        this.reviewer = reviewer;
    }
    
    public String getReviewerId() {
        return reviewerId;
    }
    
    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }
    
    public String getReviewNotes() {
        return reviewNotes;
    }
    
    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
    
    public LocalDateTime getReviewTime() {
        return reviewTime;
    }
    
    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Boolean getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
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
    
    // Builder class
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String content;
        private FrontendUser user;
        private String userId;
        private Work work;
        private Long workId;
        private Long parentId;
        private CommentStatus status = CommentStatus.PENDING;
        private BackendUser reviewer;
        private String reviewerId;
        private String reviewNotes;
        private LocalDateTime reviewTime;
        private Integer likeCount = 0;
        private Boolean isFeatured = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        private Builder() {}
        
        public Builder id(Long id) { this.id = id; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder user(FrontendUser user) { this.user = user; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder work(Work work) { this.work = work; return this; }
        public Builder workId(Long workId) { this.workId = workId; return this; }
        public Builder parentId(Long parentId) { this.parentId = parentId; return this; }
        public Builder status(CommentStatus status) { this.status = status; return this; }
        public Builder reviewer(BackendUser reviewer) { this.reviewer = reviewer; return this; }
        public Builder reviewerId(String reviewerId) { this.reviewerId = reviewerId; return this; }
        public Builder reviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; return this; }
        public Builder reviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; return this; }
        public Builder likeCount(Integer likeCount) { this.likeCount = likeCount; return this; }
        public Builder isFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public Comment build() {
            Comment comment = new Comment();
            comment.id = this.id;
            comment.content = this.content;
            comment.user = this.user;
            comment.userId = this.userId;
            comment.work = this.work;
            comment.workId = this.workId;
            comment.parentId = this.parentId;
            comment.status = this.status;
            comment.reviewer = this.reviewer;
            comment.reviewerId = this.reviewerId;
            comment.reviewNotes = this.reviewNotes;
            comment.reviewTime = this.reviewTime;
            comment.likeCount = this.likeCount;
            comment.isFeatured = this.isFeatured;
            comment.createdAt = this.createdAt;
            comment.updatedAt = this.updatedAt;
            return comment;
        }
    }
}


