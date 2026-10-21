package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_reviews", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_uploaded_by", columnList = "uploaded_by"),
    @Index(name = "idx_reviewer", columnList = "reviewer_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_file_user_status", columnList = "uploaded_by, status")
})
public class FileReview {
    
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;
    
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;
    
    @NotBlank(message = "文件路径不能为空")
    @Size(max = 500, message = "文件路径长度不能超过500个字符")
    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;
    
    @NotBlank(message = "文件类型不能为空")
    @Size(max = 50, message = "文件类型长度不能超过50个字符")
    @Column(name = "file_type", length = 50, nullable = false)
    private String fileType;
    
    @Min(value = 0, message = "文件大小不能为负数")
    @Column(name = "file_size", nullable = false)
    private Long fileSize; // 文件大小（字节）
    
    @Size(max = 100, message = "MIME类型长度不能超过100个字符")
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonIgnore
    private FrontendUser uploadedBy;
    
    @Column(name = "uploaded_by", length = 36, nullable = false, insertable = false, updatable = false)
    private String uploadedById;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    @JsonIgnore
    private BackendUser reviewer;
    
    @Column(name = "reviewer_id", length = 36, insertable = false, updatable = false)
    private String reviewerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id")
    @JsonIgnore
    private Work work;

    @Column(name = "work_id", insertable = false, updatable = false)
    private Long workId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FileReviewStatus status = FileReviewStatus.PENDING;
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;
    
    @Column(name = "upload_time", nullable = false, updatable = false)
    private LocalDateTime uploadTime;
    
    @Column(name = "review_time")
    private LocalDateTime reviewTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 状态枚举
    public enum FileReviewStatus {
        PENDING, APPROVED, REJECTED, NEEDS_REVISION
    }
    
    // 无参构造器
    public FileReview() {
    }
    
    // 全参构造器
    public FileReview(String id, String fileName, String filePath, String fileType, Long fileSize, String mimeType, 
                     FrontendUser uploadedBy, String uploadedById, BackendUser reviewer, String reviewerId, 
                     Work work, Long workId, FileReviewStatus status, String reviewNotes, LocalDateTime uploadTime, 
                     LocalDateTime reviewTime, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.uploadedBy = uploadedBy;
        this.uploadedById = uploadedById;
        this.reviewer = reviewer;
        this.reviewerId = reviewerId;
        this.work = work;
        this.workId = workId;
        this.status = status;
        this.reviewNotes = reviewNotes;
        this.uploadTime = uploadTime;
        this.reviewTime = reviewTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (uploadTime == null) {
            uploadTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 业务方法
    public void approve(BackendUser reviewer, String notes) {
        this.reviewer = reviewer;
        this.reviewerId = reviewer.getId();
        this.status = FileReviewStatus.APPROVED;
        this.reviewNotes = notes;
        this.reviewTime = LocalDateTime.now();
    }
    
    public void reject(BackendUser reviewer, String notes) {
        this.reviewer = reviewer;
        this.reviewerId = reviewer.getId();
        this.status = FileReviewStatus.REJECTED;
        this.reviewNotes = notes;
        this.reviewTime = LocalDateTime.now();
    }
    
    public void requestRevision(BackendUser reviewer, String notes) {
        this.reviewer = reviewer;
        this.reviewerId = reviewer.getId();
        this.status = FileReviewStatus.NEEDS_REVISION;
        this.reviewNotes = notes;
        this.reviewTime = LocalDateTime.now();
    }
    
    public boolean isApproved() {
        return FileReviewStatus.APPROVED.equals(this.status);
    }
    
    public boolean isRejected() {
        return FileReviewStatus.REJECTED.equals(this.status);
    }
    
    public boolean isPending() {
        return FileReviewStatus.PENDING.equals(this.status);
    }
    
    public boolean canBeReviewed() {
        return FileReviewStatus.PENDING.equals(this.status) || 
               FileReviewStatus.NEEDS_REVISION.equals(this.status);
    }
    
    public String getFileSizeDisplay() {
        if (fileSize == null || fileSize <= 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSize;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    public String getStatusDisplay() {
        switch (status) {
            case PENDING:
                return "待审核";
            case APPROVED:
                return "已通过";
            case REJECTED:
                return "已拒绝";
            case NEEDS_REVISION:
                return "需要修改";
            default:
                return "未知";
        }
    }
    
    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public FrontendUser getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(FrontendUser uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    
    public String getUploadedById() {
        return uploadedById;
    }
    
    public void setUploadedById(String uploadedById) {
        this.uploadedById = uploadedById;
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
    
    public FileReviewStatus getStatus() {
        return status;
    }
    
    public void setStatus(FileReviewStatus status) {
        this.status = status;
    }
    
    public String getReviewNotes() {
        return reviewNotes;
    }
    
    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public LocalDateTime getReviewTime() {
        return reviewTime;
    }
    
    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
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
    
    // Builder 类
    public static class Builder {
        private String id;
        private String fileName;
        private String filePath;
        private String fileType;
        private Long fileSize;
        private String mimeType;
        private FrontendUser uploadedBy;
        private String uploadedById;
        private BackendUser reviewer;
        private String reviewerId;
        private Work work;
        private Long workId;
        private FileReviewStatus status = FileReviewStatus.PENDING;
        private String reviewNotes;
        private LocalDateTime uploadTime;
        private LocalDateTime reviewTime;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        
        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }
        
        public Builder fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }
        
        public Builder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }
        
        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }
        
        public Builder uploadedBy(FrontendUser uploadedBy) {
            this.uploadedBy = uploadedBy;
            return this;
        }
        
        public Builder uploadedById(String uploadedById) {
            this.uploadedById = uploadedById;
            return this;
        }
        
        public Builder reviewer(BackendUser reviewer) {
            this.reviewer = reviewer;
            return this;
        }
        
        public Builder reviewerId(String reviewerId) {
            this.reviewerId = reviewerId;
            return this;
        }
        
        public Builder work(Work work) {
            this.work = work;
            return this;
        }
        
        public Builder workId(Long workId) {
            this.workId = workId;
            return this;
        }
        
        public Builder status(FileReviewStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder reviewNotes(String reviewNotes) {
            this.reviewNotes = reviewNotes;
            return this;
        }
        
        public Builder uploadTime(LocalDateTime uploadTime) {
            this.uploadTime = uploadTime;
            return this;
        }
        
        public Builder reviewTime(LocalDateTime reviewTime) {
            this.reviewTime = reviewTime;
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
        
        public FileReview build() {
            return new FileReview(id, fileName, filePath, fileType, fileSize, mimeType, uploadedBy, uploadedById, 
                                 reviewer, reviewerId, work, workId, status, reviewNotes, uploadTime, reviewTime, 
                                 createdAt, updatedAt);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


