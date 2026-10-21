package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.FileReview;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileReviewDto {
    
    private String reviewId;
    private String fileId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;
    private String userId;
    private String username;
    private String userNickname;
    private String reviewerId;
    private String reviewerUsername;
    private String reviewerNickname;
    private FileReview.FileReviewStatus status;
    private String reviewComment;
    private String feedback;

    public FileReview.FileReviewStatus getStatus() {
        return status;
    }
    private String relatedWorkId;
    private String relatedWorkTitle;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 统计信息
    private long totalReviews;
    private long approvedCount;
    private long rejectedCount;
    private long revisionRequestedCount;
    private double approvalRate;
    private double averageReviewTime; // 平均审核时间（小时）
}


