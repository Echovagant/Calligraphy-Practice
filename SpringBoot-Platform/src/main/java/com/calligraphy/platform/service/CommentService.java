package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.CommentDto;
import com.calligraphy.platform.dto.CommentCreateDto;
import com.calligraphy.platform.dto.CommentUpdateDto;
import com.calligraphy.platform.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    
    CommentDto createComment(String workId, String userId, CommentCreateDto createDto);
    
    CommentDto getCommentById(String commentId);
    
    Optional<Comment> getCommentEntityById(String commentId);
    
    Page<CommentDto> getAllComments(Pageable pageable);
    
    List<CommentDto> getAllComments();
    
    Page<CommentDto> getCommentsByWorkId(String workId, Pageable pageable);
    
    List<CommentDto> getCommentsByWorkId(String workId);
    
    Page<CommentDto> getCommentsByUserId(String userId, Pageable pageable);
    
    List<CommentDto> getCommentsByUserId(String userId);
    
    Page<CommentDto> getCommentsByStatus(Comment.CommentStatus status, Pageable pageable);
    
    List<CommentDto> getCommentsByStatus(Comment.CommentStatus status);
    
    Page<CommentDto> getCommentsByReviewStatus(Comment.CommentStatus reviewStatus, Pageable pageable);
    
    List<CommentDto> getCommentsByReviewStatus(Comment.CommentStatus reviewStatus);
    
    Page<CommentDto> getPublicComments(Pageable pageable);
    
    List<CommentDto> getPublicComments();
    
    CommentDto updateComment(String commentId, String userId, CommentUpdateDto updateDto);
    
    CommentDto updateCommentStatus(String commentId, String reviewerId, Comment.CommentStatus status);
    
    CommentDto updateCommentReviewStatus(String commentId, String reviewerId, Comment.CommentStatus reviewStatus, String reviewComment);
    
    void deleteComment(String commentId, String userId);
    
    void incrementLikeCount(String commentId);
    
    void decrementLikeCount(String commentId);

    void addLike(String commentId, String userId);
    
    void removeLike(String commentId, String userId);
    
    // 搜索和筛选
    Page<CommentDto> searchComments(String keyword, Pageable pageable);
    
    List<CommentDto> searchComments(String keyword);
    
    // 统计相关
    long getTotalCommentCount();
    
    long getCommentCountByWorkId(String workId);
    
    long getCommentCountByUserId(String userId);
    
    long getCommentCountByStatus(Comment.CommentStatus status);
    
    long getCommentCountByReviewStatus(Comment.CommentStatus reviewStatus);
    
    long getCommentCountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 审核相关
    List<CommentDto> getCommentsPendingReview();
    
    List<CommentDto> getCommentsByReviewerId(String reviewerId);
    
    long getPendingReviewCount();
    
    // 热门和推荐
    List<CommentDto> getTopLikedComments(int limit);
    
    List<CommentDto> getRecentComments(int limit);
    
    // 批量操作
    void bulkDeleteComments(List<String> commentIds);
    
    void bulkUpdateStatus(List<String> commentIds, Comment.CommentStatus status);
    
    void bulkUpdateReviewStatus(List<String> commentIds, Comment.CommentStatus reviewStatus, String reviewerId);
    
    // 回复相关
    CommentDto createReply(String parentCommentId, String userId, CommentCreateDto createDto);
    
    List<CommentDto> getRepliesByCommentId(String commentId);
    
    long getReplyCountByCommentId(String commentId);
    
    boolean hasReplies(String commentId);
    
    // 内容审核
    boolean containsSensitiveContent(String content);
    
    void autoModerateComment(String commentId);
}


