package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.Comment;
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
public class CommentDto {
    
    private String commentId;
    private String workId;
    private String workTitle;
    private String userId;
    private String username;
    private String userNickname;
    private String userAvatar;
    private String parentCommentId;
    private String content;
    private Comment.CommentStatus status;
    private String reviewerId;
    private String reviewerComment;
    private long likeCount;
    private boolean isLikedByCurrentUser;

    public boolean isLikedByCurrentUser() {
        return isLikedByCurrentUser;
    }
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 鍥炲淇℃伅
    private List<CommentDto> replies;
    private long replyCount;
    private boolean hasReplies;
    
    // 瀹℃牳鑰呬俊鎭?    private String reviewerUsername;
    private String reviewerNickname;
}


