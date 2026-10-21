package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.CommentDto;
import com.calligraphy.platform.dto.CommentCreateDto;
import com.calligraphy.platform.dto.CommentUpdateDto;
import com.calligraphy.platform.model.Comment;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.mapper.CommentMapper;
import com.calligraphy.platform.repository.CommentRepository;
import com.calligraphy.platform.service.CommentService;
import com.calligraphy.platform.service.UserService;
import com.calligraphy.platform.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final WorkService workService;
    private final CommentMapper commentMapper;
    @Override
    @Transactional
    @SuppressWarnings("null")
    public CommentDto createComment(String workId, String userId, CommentCreateDto createDto) {
        log.info("创建评论: 作品ID={}, 用户ID={}", workId, userId);

        User author = userService.findById(userId);
        Work work = workService.getWorkEntityById(Long.valueOf(workId))
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        Comment comment = Comment.builder()
                .content(createDto.getContent())
                .user(author)
                .work(work)
                .parentId(createDto.getParentId())
                .likeCount(0)
                .status(Comment.CommentStatus.PENDING)
                .isFeatured(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 更新作品的评论数
        workService.updateCommentCount(work.getId(), 1);

        log.info("评论创建成功: ID={}", savedComment.getId());
        return commentMapper.toDto(savedComment);
    }
    @Override
    @SuppressWarnings("null")
    public CommentDto getCommentById(String commentId) {
        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        return commentMapper.toDto(comment);
    }
    @Override
    public Optional<Comment> getCommentEntityById(String commentId) {
        return commentRepository.findById(( Long) Long.valueOf(commentId));
    }
    @Override
    public Page<CommentDto> getAllComments( Pageable pageable) {
        Page<Comment> comments = commentRepository.findAll(pageable);
        return comments.map(commentMapper::toDto);
    }
    @Override
    public List<CommentDto> getAllComments() {
        List<Comment> comments = commentRepository.findAll();
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    @SuppressWarnings("null")
    public Page<CommentDto> getCommentsByWorkId(String workId,  Pageable pageable) {
        Page<Comment> comments = commentRepository.findByWorkIdAndStatusNotOrderByCreatedAtDesc(
                ( Long) Long.valueOf(workId),
                Comment.CommentStatus.DELETED.name(), pageable);
        return comments.map(commentMapper::toDto);
    }
    @Override
    public List<CommentDto> getCommentsByWorkId(String workId) {
        List<Comment> comments = commentRepository.findByWorkIdAndStatusNotOrderByCreatedAtDesc(
                ( Long) Long.valueOf(workId),
                Comment.CommentStatus.DELETED.name());
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<CommentDto> getCommentsByUserId(String userId,  Pageable pageable) {
        Page<Comment> comments = commentRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId,
                Comment.CommentStatus.DELETED.name(), pageable);
        return comments.map(commentMapper::toDto);
    }
    @Override
    public List<CommentDto> getCommentsByUserId(String userId) {
        Page<Comment> comments = commentRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId,
                Comment.CommentStatus.DELETED.name(),
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE));
        return comments.getContent().stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<CommentDto> getCommentsByStatus(Comment.CommentStatus status,  Pageable pageable) {
        Page<Comment> comments = commentRepository.findByStatus(status.name(), pageable);
        return comments.map(commentMapper::toDto);
    }
    @Override
    public List<CommentDto> getCommentsByStatus(Comment.CommentStatus status) {
        List<Comment> comments = commentRepository.findByStatus(status.name());
        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<CommentDto> getCommentsByReviewStatus(Comment.CommentStatus reviewStatus,  Pageable pageable) {
        // 假设reviewStatus就是status字段
        return getCommentsByStatus(reviewStatus, pageable);
    }
    @Override
    public List<CommentDto> getCommentsByReviewStatus(Comment.CommentStatus reviewStatus) {
        // 假设reviewStatus就是status字段
        return getCommentsByStatus(reviewStatus);
    }
    @Override
    public Page<CommentDto> getPublicComments( Pageable pageable) {
        // 公开评论即为已批准的评论
        return getCommentsByStatus(Comment.CommentStatus.APPROVED, pageable);
    }
    @Override
    public List<CommentDto> getPublicComments() {
        // 公开评论即为已批准的评论
        return getCommentsByStatus(Comment.CommentStatus.APPROVED);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public CommentDto updateComment(String commentId, String userId, CommentUpdateDto updateDto) {
        log.info("更新评论: ID={}, 用户ID={}", commentId, userId);

        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        // 检查权限 - 只有作者或管理员可以更新
        User currentUser = userService.findById(userId);
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("没有权限更新此评论");
        }

        if (updateDto.getContent() != null) {
            comment.setContent(updateDto.getContent());
        }

        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("评论更新成功: ID={}", updatedComment.getId());

        return commentMapper.toDto(updatedComment);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public CommentDto updateCommentStatus(String commentId, String reviewerId, Comment.CommentStatus status) {
        log.info("更新评论状态: ID={}, 审核员ID={}, 状态:{}", commentId, reviewerId, status);

        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        comment.setStatus(status);
        comment.setReviewNotes("状态更新为: " + status);
        comment.setReviewTime(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("评论状态更新成功: ID={}", updatedComment.getId());

        return commentMapper.toDto(updatedComment);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public CommentDto updateCommentReviewStatus(String commentId, String reviewerId, Comment.CommentStatus reviewStatus,
            String reviewComment) {
        log.info("更新评论审核状态: ID={}, 审核员ID={}, 状态:{}, 备注={}", commentId, reviewerId, reviewStatus, reviewComment);

        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        comment.setStatus(reviewStatus);
        comment.setReviewNotes(reviewComment);
        comment.setReviewTime(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("评论审核状态更新成功: ID={}", updatedComment.getId());

        return commentMapper.toDto(updatedComment);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public void deleteComment(String commentId, String userId) {
        log.info("删除评论: ID={}, 用户ID={}", commentId, userId);

        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        // 检查权限 - 只有作者或管理员可以删除
        User currentUser = userService.findById(userId);
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("没有权限删除此评论");
        }

        // 软删除 - 标记为已删除而不是物理删除
        comment.setStatus(Comment.CommentStatus.DELETED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        // 更新作品的评论数
        workService.updateCommentCount(comment.getWorkId(), -1);

        log.info("评论删除成功: ID={}", commentId);
    }
    @Override
    @Transactional
    public void incrementLikeCount(String commentId) {
        Comment comment = commentRepository.findById(( Long) Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }
    @Override
    @Transactional
    public void decrementLikeCount(String commentId) {
        Comment comment = commentRepository.findById(( Long) Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        if (comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentRepository.save(comment);
        }
    }
    @Override
    @Transactional
    public void addLike(String commentId, String userId) {
        incrementLikeCount(commentId);
    }
    @Override
    @Transactional
    public void removeLike(String commentId, String userId) {
        decrementLikeCount(commentId);
    }
    @Override
    public Page<CommentDto> searchComments(String keyword,  Pageable pageable) {
        // 这里需要实现搜索逻辑，暂时返回空结果
        return Page.empty(pageable);
    }
    @Override
    public List<CommentDto> searchComments(String keyword) {
        // 这里需要实现搜索逻辑，暂时返回空结果
        return List.of();
    }
    @Override
    public long getTotalCommentCount() {
        return commentRepository.count();
    }
    @Override
    public long getCommentCountByWorkId(String workId) {
        return commentRepository.countByWorkIdAndStatusNot(Long.valueOf(workId), Comment.CommentStatus.DELETED.name());
    }
    @Override
    public long getCommentCountByUserId(String userId) {
        return commentRepository.countByUserIdAndStatusNot(userId, Comment.CommentStatus.DELETED.name());
    }
    @Override
    public long getCommentCountByStatus(Comment.CommentStatus status) {
        return commentRepository.countByStatus(status.name());
    }
    @Override
    public long getCommentCountByReviewStatus(Comment.CommentStatus reviewStatus) {
        // 假设reviewStatus就是status字段
        return getCommentCountByStatus(reviewStatus);
    }
    @Override
    public long getCommentCountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        // 这里需要实现按日期范围统计的逻辑，暂时返回0
        return 0;
    }
    @Override
    public List<CommentDto> getCommentsPendingReview() {
        return getCommentsByStatus(Comment.CommentStatus.PENDING);
    }
    @Override
    public List<CommentDto> getCommentsByReviewerId(String reviewerId) {
        // 这里需要实现按审核员ID查询的逻辑，暂时返回空列表
        return List.of();
    }
    @Override
    public long getPendingReviewCount() {
        return getCommentCountByStatus(Comment.CommentStatus.PENDING);
    }
    @Override
    public List<CommentDto> getTopLikedComments(int limit) {
        // 这里需要实现按点赞数排序的逻辑，暂时返回空列表
        return List.of();
    }
    @Override
    public List<CommentDto> getRecentComments(int limit) {
        // 这里需要实现获取最近评论的逻辑，暂时返回空列表
        return List.of();
    }
    @Override
    @Transactional
    public void bulkDeleteComments(List<String> commentIds) {
        for (String commentId : commentIds) {
            try {
                deleteComment(commentId, "SYSTEM");
            } catch (Exception e) {
                log.error("批量删除评论失败: ID={}", commentId, e);
            }
        }
    }
    @Override
    @Transactional
    public void bulkUpdateStatus(List<String> commentIds, Comment.CommentStatus status) {
        for (String commentId : commentIds) {
            try {
                updateCommentStatus(commentId, "SYSTEM", status);
            } catch (Exception e) {
                log.error("批量更新评论状态失败: ID={}", commentId, e);
            }
        }
    }
    @Override
    @Transactional
    public void bulkUpdateReviewStatus(List<String> commentIds, Comment.CommentStatus reviewStatus, String reviewerId) {
        for (String commentId : commentIds) {
            try {
                updateCommentReviewStatus(commentId, reviewerId, reviewStatus, "批量审核");
            } catch (Exception e) {
                log.error("批量更新评论审核状态失败: ID={}", commentId, e);
            }
        }
    }
    @Override
    @Transactional
    public CommentDto createReply(String parentCommentId, String userId, CommentCreateDto createDto) {
        log.info("创建回复: 父评论ID={}, 用户ID={}", parentCommentId, userId);

        // 首先获取父评论
        Comment parentComment = commentRepository.findById(( Long) Long.valueOf(parentCommentId))
                .orElseThrow(() -> new ResourceNotFoundException("父评论不存在: " + parentCommentId));

        User author = userService.findById(userId);
        Work work = parentComment.getWork();

        Comment reply = Comment.builder()
                .content(createDto.getContent())
                .user(author)
                .work(work)
                .parentId(( Long) Long.valueOf(parentCommentId))
                .likeCount(0)
                .status(Comment.CommentStatus.PENDING)
                .isFeatured(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment savedReply = commentRepository.save(( Comment) reply);

        // 更新作品的评论数
        workService.updateCommentCount(work.getId(), 1);

        log.info("回复创建成功: ID={}", savedReply.getId());
        return commentMapper.toDto(savedReply);
    }
    @Override
    public List<CommentDto> getRepliesByCommentId(String commentId) {
        List<Comment> replies = commentRepository.findByParentIdAndStatusNotOrderByCreatedAtAsc(
                ( Long) Long.valueOf(commentId),
                Comment.CommentStatus.DELETED.name());
        return replies.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public long getReplyCountByCommentId(String commentId) {
        return commentRepository.countByParentIdAndStatusNot(( Long) Long.valueOf(commentId),
                Comment.CommentStatus.DELETED.name());
    }
    @Override
    public boolean hasReplies(String commentId) {
        return getReplyCountByCommentId(commentId) > 0;
    }
    @Override
    public boolean containsSensitiveContent(String content) {
        // 这里需要实现敏感内容检测逻辑，暂时返回false
        return false;
    }
    @Override
    @Transactional
    public void autoModerateComment(String commentId) {
        try {
            Comment comment = commentRepository.findById(( Long) Long.valueOf(commentId))
                    .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

            // 简单的自动审核逻辑：检查是否包含敏感内容
            if (containsSensitiveContent(comment.getContent())) {
                comment.setStatus(Comment.CommentStatus.REJECTED);
                comment.setReviewNotes("自动审核：包含敏感内容");
            } else {
                comment.setStatus(Comment.CommentStatus.APPROVED);
                comment.setReviewNotes("自动审核通过");
            }

            comment.setReviewTime(LocalDateTime.now());
            commentRepository.save(comment);

            log.info("评论自动审核完成: ID={}, 结果={}", commentId, comment.getStatus());
        } catch (Exception e) {
            log.error("自动审核评论失败: ID={}", commentId, e);
        }
    }
}


