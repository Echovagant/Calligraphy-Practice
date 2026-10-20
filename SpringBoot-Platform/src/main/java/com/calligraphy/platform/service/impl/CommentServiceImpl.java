package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.CommentDto;
import com.calligraphy.platform.dto.CommentCreateDto;
import com.calligraphy.platform.dto.CommentUpdateDto;
import com.calligraphy.platform.model.Comment;
import com.calligraphy.platform.model.FrontendUser;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.mapper.CommentMapper;
import com.calligraphy.platform.repository.CommentRepository;
import com.calligraphy.platform.service.CommentService;
import com.calligraphy.platform.service.FrontendUserService;
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
    private final FrontendUserService userService;
    private final WorkService workService;
    private final CommentMapper commentMapper;
    @Override
    @Transactional
    @SuppressWarnings("null")
    public CommentDto createComment(String workId, String userId, CommentCreateDto createDto) {
        log.info("鍒涘缓璇勮: 作品ID={}, 用户ID={}", workId, userId);

        FrontendUser author = userService.getUserEntityById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + userId));
        Work work = workService.getWorkEntityById(Long.valueOf(workId))
                .orElseThrow(() -> new ResourceNotFoundException("浣滃搧涓嶅瓨鍦? " + workId));

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

        log.info("璇勮创建成功: ID={}", savedComment.getId());
        return commentMapper.toDto(savedComment);
    }
    @Override
    @SuppressWarnings("null")
    public CommentDto getCommentById(String commentId) {
        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("璇勮涓嶅瓨鍦? " + commentId));

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
        // 鍋囪reviewStatus就是status瀛楁
        return getCommentsByStatus(reviewStatus, pageable);
    }
    @Override
    public List<CommentDto> getCommentsByReviewStatus(Comment.CommentStatus reviewStatus) {
        // 鍋囪reviewStatus就是status瀛楁
        return getCommentsByStatus(reviewStatus);
    }
    @Override
    public Page<CommentDto> getPublicComments( Pageable pageable) {
        // 鍏紑璇勮鍗充负宸叉壒鍑嗙殑璇勮
        return getCommentsByStatus(Comment.CommentStatus.APPROVED, pageable);
    }
    @Override
    public List<CommentDto> getPublicComments() {
        // 鍏紑璇勮鍗充负宸叉壒鍑嗙殑璇勮
        return getCommentsByStatus(Comment.CommentStatus.APPROVED);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public CommentDto updateComment(String commentId, String userId, CommentUpdateDto updateDto) {
        log.info("鏇存柊璇勮: ID={}, 用户ID={}", commentId, userId);

        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("璇勮涓嶅瓨鍦? " + commentId));

        // 妫€鏌ユ潈闄?- 鍙湁浣滆€呮垨绠＄悊鍛樺彲浠ユ洿鏂
        FrontendUser currentUser = userService.getUserEntityById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + userId));
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("娌℃湁鏉冮檺鏇存柊姝よ瘎璁");
        }

        if (updateDto.getContent() != null) {
            comment.setContent(updateDto.getContent());
        }

        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("璇勮更新成功: ID={}", updatedComment.getId());

        return commentMapper.toDto(updatedComment);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public CommentDto updateCommentStatus(String commentId, String reviewerId, Comment.CommentStatus status) {
        log.info("鏇存柊璇勮鐘舵€? ID={}, 瀹℃牳鍛業D={}, 鐘舵€?{}", commentId, reviewerId, status);

        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("璇勮涓嶅瓨鍦? " + commentId));

        comment.setStatus(status);
        comment.setReviewNotes("鐘舵€佹洿鏂颁负: " + status);
        comment.setReviewTime(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("璇勮鐘舵€佹洿鏂版垚鍔? ID={}", updatedComment.getId());

        return commentMapper.toDto(updatedComment);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public CommentDto updateCommentReviewStatus(String commentId, String reviewerId, Comment.CommentStatus reviewStatus,
            String reviewComment) {
        log.info("鏇存柊璇勮瀹℃牳鐘舵€? ID={}, 瀹℃牳鍛業D={}, 鐘舵€?{}, 备注={}", commentId, reviewerId, reviewStatus, reviewComment);

        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("璇勮涓嶅瓨鍦? " + commentId));

        comment.setStatus(reviewStatus);
        comment.setReviewNotes(reviewComment);
        comment.setReviewTime(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("璇勮瀹℃牳鐘舵€佹洿鏂版垚鍔? ID={}", updatedComment.getId());

        return commentMapper.toDto(updatedComment);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public void deleteComment(String commentId, String userId) {
        log.info("鍒犻櫎璇勮: ID={}, 用户ID={}", commentId, userId);

        Comment comment = commentRepository.findById(Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("璇勮涓嶅瓨鍦? " + commentId));

        // 妫€鏌ユ潈闄?- 鍙湁浣滆€呮垨绠＄悊鍛樺彲浠ュ垹闄
        FrontendUser currentUser = userService.getUserEntityById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + userId));
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("娌℃湁鏉冮檺鍒犻櫎姝よ瘎璁");
        }

        // 杞垹闄?- 鏍囪涓哄凡鍒犻櫎鑰屼笉鏄墿鐞嗗垹闄
        comment.setStatus(Comment.CommentStatus.DELETED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        // 更新作品的评论数
        workService.updateCommentCount(comment.getWorkId(), -1);

        log.info("璇勮删除成功: ID={}", commentId);
    }
    @Override
    @Transactional
    public void incrementLikeCount(String commentId) {
        Comment comment = commentRepository.findById(( Long) Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("璇勮涓嶅瓨鍦? " + commentId));

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
    }
    @Override
    @Transactional
    public void decrementLikeCount(String commentId) {
        Comment comment = commentRepository.findById(( Long) Long.valueOf(commentId))
                .orElseThrow(() -> new ResourceNotFoundException("璇勮涓嶅瓨鍦? " + commentId));

        if (comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentRepository.save(comment);
        }
    }
    @Override
    public boolean isLikedByUser(String commentId, String userId) {
        // 杩欓噷闇€要实现用户点赞状态的逻辑
        // 由于当前Comment瀹炰綋娌℃湁璁板綍鐐硅禐鐢ㄦ埛鍒楄〃锛岃繑鍥瀎alse浣滀负榛樿实现
        return false;
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
        // 杩欓噷闇€瑕佸疄鐜版悳绱㈤€昏緫锛屾殏鏃惰繑鍥炵┖结果
        return Page.empty(pageable);
    }
    @Override
    public List<CommentDto> searchComments(String keyword) {
        // 杩欓噷闇€瑕佸疄鐜版悳绱㈤€昏緫锛屾殏鏃惰繑鍥炵┖结果
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
        // 鍋囪reviewStatus就是status瀛楁
        return getCommentCountByStatus(reviewStatus);
    }
    @Override
    public long getCommentCountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        // 杩欓噷闇€瑕佸疄鐜版寜鏃ユ湡鑼冨洿缁熻鐨勯€昏緫锛屾殏鏃惰繑鍥?
        return 0;
    }
    @Override
    public List<CommentDto> getCommentsPendingReview() {
        return getCommentsByStatus(Comment.CommentStatus.PENDING);
    }
    @Override
    public List<CommentDto> getCommentsByReviewerId(String reviewerId) {
        // 杩欓噷闇€瑕佸疄鐜版寜瀹℃牳鍛業D鏌ヨ鐨勯€昏緫锛屾殏鏃惰繑鍥炵┖列表
        return List.of();
    }
    @Override
    public long getPendingReviewCount() {
        return getCommentCountByStatus(Comment.CommentStatus.PENDING);
    }
    @Override
    public List<CommentDto> getTopLikedComments(int limit) {
        // 杩欓噷闇€瑕佸疄鐜版寜鐐硅禐鏁版帓搴忕殑閫昏緫锛屾殏鏃惰繑鍥炵┖列表
        return List.of();
    }
    @Override
    public List<CommentDto> getRecentComments(int limit) {
        // 杩欓噷闇€瑕佸疄鐜拌幏鍙栨渶杩戣瘎璁虹殑閫昏緫锛屾殏鏃惰繑鍥炵┖列表
        return List.of();
    }
    @Override
    @Transactional
    public void bulkDeleteComments(List<String> commentIds) {
        for (String commentId : commentIds) {
            try {
                deleteComment(commentId, "SYSTEM");
            } catch (Exception e) {
                log.error("鎵归噺鍒犻櫎璇勮失败: ID={}", commentId, e);
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
                log.error("鎵归噺鏇存柊璇勮鐘舵€佸け璐? ID={}", commentId, e);
            }
        }
    }
    @Override
    @Transactional
    public void bulkUpdateReviewStatus(List<String> commentIds, Comment.CommentStatus reviewStatus, String reviewerId) {
        for (String commentId : commentIds) {
            try {
                updateCommentReviewStatus(commentId, reviewerId, reviewStatus, "鎵归噺瀹℃牳");
            } catch (Exception e) {
                log.error("鎵归噺鏇存柊璇勮瀹℃牳鐘舵€佸け璐? ID={}", commentId, e);
            }
        }
    }
    @Override
    @Transactional
    public CommentDto createReply(String parentCommentId, String userId, CommentCreateDto createDto) {
        log.info("鍒涘缓鍥炲: 鐖惰瘎璁篒D={}, 用户ID={}", parentCommentId, userId);

        // 棣栧厛鑾峰彇鐖惰瘎璁
        Comment parentComment = commentRepository.findById(( Long) Long.valueOf(parentCommentId))
                .orElseThrow(() -> new ResourceNotFoundException("父评论不存在: " + parentCommentId));

        FrontendUser author = userService.getUserEntityById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + userId));
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

        log.info("鍥炲创建成功: ID={}", savedReply.getId());
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
        // 杩欓噷闇€瑕佸疄鐜版晱鎰熷唴瀹规娴嬮€昏緫锛屾殏鏃惰繑鍥瀎alse
        return false;
    }
    @Override
    @Transactional
    public void autoModerateComment(String commentId) {
        try {
            Comment comment = commentRepository.findById(( Long) Long.valueOf(commentId))
                    .orElseThrow(() -> new ResourceNotFoundException("璇勮涓嶅瓨鍦? " + commentId));

            // 绠€鍗曠殑鑷姩瀹℃牳閫昏緫锛氭鏌ユ槸鍚﹀寘鍚晱鎰熷唴瀹
            if (containsSensitiveContent(comment.getContent())) {
                comment.setStatus(Comment.CommentStatus.REJECTED);
                comment.setReviewNotes("鑷姩瀹℃牳锛氬寘鍚晱鎰熷唴瀹");
            } else {
                comment.setStatus(Comment.CommentStatus.APPROVED);
                comment.setReviewNotes("鑷姩瀹℃牳閫氳繃");
            }

            comment.setReviewTime(LocalDateTime.now());
            commentRepository.save(comment);

            log.info("璇勮鑷姩瀹℃牳瀹屾垚: ID={}, 结果={}", commentId, comment.getStatus());
        } catch (Exception e) {
            log.error("鑷姩瀹℃牳璇勮失败: ID={}", commentId, e);
        }
    }
}


