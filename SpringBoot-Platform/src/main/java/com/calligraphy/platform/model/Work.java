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

@Entity
@Table(name = "works", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_review_status", columnList = "review_status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_works_user_status", columnList = "user_id, status, review_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Work {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    @Column(name = "title", length = 255, nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private FrontendUser user;
    
    @Column(name = "user_id", length = 36, nullable = false, insertable = false, updatable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WorkStatus status = WorkStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 20)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;
    
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
    
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;
    
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 关联关系
    @OneToMany(mappedBy = "work", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments;
    
    // 状态枚举
    public enum WorkStatus {
        DRAFT, PUBLISHED, HIDDEN, UNDER_REVIEW
    }
    
    // 审核状态枚举
    public enum ReviewStatus {
        PENDING, APPROVED, REJECTED, NEEDS_REVISION
    }
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (likeCount == null) likeCount = 0;
        if (commentCount == null) commentCount = 0;
        if (viewCount == null) viewCount = 0;
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
    
    public void incrementCommentCount() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }
    
    public void decrementCommentCount() {
        this.commentCount = Math.max(0, (this.commentCount == null ? 0 : this.commentCount) - 1);
    }
    
    public void approve(BackendUser reviewer, String notes) {
        this.reviewer = reviewer;
        this.reviewerId = reviewer.getId();
        this.reviewStatus = ReviewStatus.APPROVED;
        this.reviewNotes = notes;
        this.reviewTime = LocalDateTime.now();
        this.status = WorkStatus.PUBLISHED;
    }
    
    public void reject(BackendUser reviewer, String notes) {
        this.reviewer = reviewer;
        this.reviewerId = reviewer.getId();
        this.reviewStatus = ReviewStatus.REJECTED;
        this.reviewNotes = notes;
        this.reviewTime = LocalDateTime.now();
        this.status = WorkStatus.HIDDEN;
    }
    
    public void requestRevision(BackendUser reviewer, String notes) {
        this.reviewer = reviewer;
        this.reviewerId = reviewer.getId();
        this.reviewStatus = ReviewStatus.NEEDS_REVISION;
        this.reviewNotes = notes;
        this.reviewTime = LocalDateTime.now();
        this.status = WorkStatus.UNDER_REVIEW;
    }
    
    public boolean isPublic() {
        return WorkStatus.PUBLISHED.equals(this.status) && 
               ReviewStatus.APPROVED.equals(this.reviewStatus);
    }
    
    public boolean canBeReviewed() {
        return ReviewStatus.PENDING.equals(this.reviewStatus) || 
               ReviewStatus.NEEDS_REVISION.equals(this.reviewStatus);
    }
    
    // 娣诲姞缂哄皯鐨刧etter鍜宻etter方法
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
    
    public WorkStatus getStatus() {
        return status;
    }
    
    public void setStatus(WorkStatus status) {
        this.status = status;
    }
    
    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }
    
    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
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
    
    public Integer getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
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
    
    public List<Comment> getComments() {
        return comments;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    // Builder class
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String content;
        private String imageUrl;
        private FrontendUser user;
        private String userId;
        private WorkStatus status = WorkStatus.DRAFT;
        private ReviewStatus reviewStatus = ReviewStatus.PENDING;
        private BackendUser reviewer;
        private String reviewerId;
        private String reviewNotes;
        private LocalDateTime reviewTime;
        private Integer likeCount = 0;
        private Integer commentCount = 0;
        private Integer viewCount = 0;
        private Boolean isFeatured = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<Comment> comments;
        
        private Builder() {}
        
        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder user(FrontendUser user) { this.user = user; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder status(WorkStatus status) { this.status = status; return this; }
        public Builder reviewStatus(ReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; return this; }
        public Builder reviewer(BackendUser reviewer) { this.reviewer = reviewer; return this; }
        public Builder reviewerId(String reviewerId) { this.reviewerId = reviewerId; return this; }
        public Builder reviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; return this; }
        public Builder reviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; return this; }
        public Builder likeCount(Integer likeCount) { this.likeCount = likeCount; return this; }
        public Builder commentCount(Integer commentCount) { this.commentCount = commentCount; return this; }
        public Builder viewCount(Integer viewCount) { this.viewCount = viewCount; return this; }
        public Builder isFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder comments(List<Comment> comments) { this.comments = comments; return this; }
        
        public Work build() {
            Work work = new Work();
            work.id = this.id;
            work.title = this.title;
            work.description = this.description;
            work.content = this.content;
            work.imageUrl = this.imageUrl;
            work.user = this.user;
            work.userId = this.userId;
            work.status = this.status;
            work.reviewStatus = this.reviewStatus;
            work.reviewer = this.reviewer;
            work.reviewerId = this.reviewerId;
            work.reviewNotes = this.reviewNotes;
            work.reviewTime = this.reviewTime;
            work.likeCount = this.likeCount;
            work.commentCount = this.commentCount;
            work.viewCount = this.viewCount;
            work.isFeatured = this.isFeatured;
            work.createdAt = this.createdAt;
            work.updatedAt = this.updatedAt;
            work.comments = this.comments;
            return work;
        }
    }
}


