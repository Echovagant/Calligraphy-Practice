package com.calligraphy.platform.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@EntityListeners(AuditingEntityListener.class)
public class FileMetadata {
    
    @Id
    @Column(name = "file_id", nullable = false, length = 100)
    private String fileId;
    
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;
    
    @Column(name = "file_type", length = 50)
    private String fileType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_purpose", length = 100)
    private String filePurpose;
    
    @Column(name = "related_entity_id", length = 100)
    private String relatedEntityId;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 无参构造器
    public FileMetadata() {
    }
    
    // 全参构造器
    public FileMetadata(String fileId, String userId, String fileName, String fileUrl, String fileType, 
                      Long fileSize, String filePurpose, String relatedEntityId, String description, 
                      Boolean isPublic, Boolean isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.fileId = fileId;
        this.userId = userId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.filePurpose = filePurpose;
        this.relatedEntityId = relatedEntityId;
        this.description = description;
        this.isPublic = isPublic;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter 和 Setter 方法
    public String getFileId() {
        return fileId;
    }
    
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
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
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFilePurpose() {
        return filePurpose;
    }
    
    public void setFilePurpose(String filePurpose) {
        this.filePurpose = filePurpose;
    }
    
    public String getRelatedEntityId() {
        return relatedEntityId;
    }
    
    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
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
    
    // Builder 类
    public static class Builder {
        private String fileId;
        private String userId;
        private String fileName;
        private String fileUrl;
        private String fileType;
        private Long fileSize;
        private String filePurpose;
        private String relatedEntityId;
        private String description;
        private Boolean isPublic = false;
        private Boolean isDeleted = false;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        
        public Builder fileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
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
        
        public Builder filePurpose(String filePurpose) {
            this.filePurpose = filePurpose;
            return this;
        }
        
        public Builder relatedEntityId(String relatedEntityId) {
            this.relatedEntityId = relatedEntityId;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder isPublic(Boolean isPublic) {
            this.isPublic = isPublic;
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
        
        public FileMetadata build() {
            return new FileMetadata(fileId, userId, fileName, fileUrl, fileType, fileSize, filePurpose, 
                                 relatedEntityId, description, isPublic, isDeleted, createdAt, updatedAt);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


