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
    
    // 鐢ㄦ埛淇℃伅
    private String userNickname;
    private String userAvatar;
    
    // 瀹℃牳鑰呬俊鎭?    private String reviewerUsername;
    private String reviewerNickname;
    
    // 缁熻淇℃伅
    private long commentCount;
    private double popularityScore;
}


