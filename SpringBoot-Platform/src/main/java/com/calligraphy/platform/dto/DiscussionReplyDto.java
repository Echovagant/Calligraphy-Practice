package com.calligraphy.platform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionReplyDto {
    
    private String replyId;
    private String content;
    private String discussionId;
    private String userId;
    private String username;
    private String userAvatar;
    private String parentId;
    private String replyToUserId;
    private String replyToUsername;
    private String status;
    private Integer likeCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}


