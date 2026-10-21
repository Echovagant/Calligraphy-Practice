package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.Work;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkDto {
    
    private String workId;
    private String userId;
    private String username;
    private String title;
    private String description;
    private String content;
    private String category;
    private List<String> tags;
    private List<String> fileUrls;
    private Work.WorkStatus status;
    private Work.ReviewStatus reviewStatus;
    private String reviewerId;
    private String reviewerComment;
    private long viewCount;
    private long likeCount;
    private boolean isLikedByCurrentUser;
    private boolean isFeatured;
    private LocalDateTime publishedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 用户信息
    private String userNickname;
    private String userAvatar;
    
    // 审核者信息
    private String reviewerUsername;
    private String reviewerNickname;
    
    // 统计信息
    private long commentCount;
    private double popularityScore;
}


