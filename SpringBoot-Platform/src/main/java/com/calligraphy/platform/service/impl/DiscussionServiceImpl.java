package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.DiscussionDto;
import com.calligraphy.platform.dto.DiscussionCreateDto;
import com.calligraphy.platform.dto.DiscussionUpdateDto;
import com.calligraphy.platform.dto.DiscussionReplyDto;
import com.calligraphy.platform.dto.DiscussionReplyCreateDto;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.exception.UnauthorizedException;
import com.calligraphy.platform.mapper.DiscussionMapper;
import com.calligraphy.platform.mapper.DiscussionReplyMapper;
import com.calligraphy.platform.model.Discussion;
import com.calligraphy.platform.model.DiscussionReply;
import com.calligraphy.platform.model.FrontendUser;
import com.calligraphy.platform.repository.DiscussionRepository;
import com.calligraphy.platform.repository.DiscussionReplyRepository;
import com.calligraphy.platform.repository.FrontendUserRepository;
import com.calligraphy.platform.service.DiscussionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// @Service
@Transactional
public class DiscussionServiceImpl implements DiscussionService {

    private static final Logger log = LoggerFactory.getLogger(DiscussionServiceImpl.class);

    private final DiscussionRepository discussionRepository;
    private final DiscussionReplyRepository discussionReplyRepository;
    private final FrontendUserRepository frontendUserRepository;
    private final DiscussionMapper discussionMapper;
    private final DiscussionReplyMapper discussionReplyMapper;

    public DiscussionServiceImpl(DiscussionRepository discussionRepository,
            DiscussionReplyRepository discussionReplyRepository,
            FrontendUserRepository frontendUserRepository,
            DiscussionMapper discussionMapper,
            DiscussionReplyMapper discussionReplyMapper) {
        this.discussionRepository = discussionRepository;
        this.discussionReplyRepository = discussionReplyRepository;
        this.frontendUserRepository = frontendUserRepository;
        this.discussionMapper = discussionMapper;
        this.discussionReplyMapper = discussionReplyMapper;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public DiscussionDto createDiscussion(String userId, DiscussionCreateDto createDto) {
        log.info("创建讨论: 用户ID={}, 标题={}, 分类={}", userId, createDto.getTitle(), createDto.getCategory());

        FrontendUser user = frontendUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        Discussion discussion = new Discussion();
        discussion.setTitle(createDto.getTitle());
        discussion.setContent(createDto.getContent());
        discussion.setCategory(createDto.getCategory());
        discussion.setUser(user);
        discussion.setUserId(user.getId());
        discussion.setUsername(user.getUsername());
        discussion.setUserAvatar(user.getAvatarUrl());
        discussion.setStatus(Discussion.DiscussionStatus.ACTIVE);
        discussion.setViewCount(0);
        discussion.setLikeCount(0);
        discussion.setReplyCount(0);
        discussion.setIsPinned(false);
        discussion.setIsFeatured(false);
        discussion.setTags(createDto.getTags());

        Discussion savedDiscussion = discussionRepository.save(discussion);
        log.info("讨论创建成功: ID={}", savedDiscussion.getId());

        return discussionMapper.toDto(savedDiscussion);
    }
    @Override
    @Transactional(readOnly = true)
    public DiscussionDto getDiscussionById(String discussionId) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.incrementViewCount();
        discussionRepository.save(discussion);

        return discussionMapper.toDto(discussion);
    }
    @Override
    @Transactional(readOnly = true)
    public DiscussionDto getDiscussionByIdWithReplies(String discussionId, Pageable replyPageable) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.incrementViewCount();
        discussionRepository.save(discussion);

        DiscussionDto discussionDto = discussionMapper.toDto(discussion);

        Page<DiscussionReply> replies = discussionReplyRepository.findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(
                discussion.getId(), DiscussionReply.ReplyStatus.DELETED, replyPageable);

        discussionDto.setReplies(discussionReplyMapper.toDtoList(replies.getContent()));

        return discussionDto;
    }
    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> getAllDiscussions(Pageable pageable) {
        Page<Discussion> discussions = discussionRepository.findByStatusNotOrderByCreatedAtDesc(
                Discussion.DiscussionStatus.DELETED, pageable);
        return discussions.map(discussionMapper::toDto);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> getAllDiscussions() {
        List<Discussion> discussions = discussionRepository.findByStatusNotOrderByCreatedAtDesc(
                Discussion.DiscussionStatus.DELETED);
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> getDiscussionsByCategory(String category, Pageable pageable) {
        Page<Discussion> discussions = discussionRepository.findByCategoryAndStatusNotOrderByCreatedAtDesc(
                category, Discussion.DiscussionStatus.DELETED, pageable);
        return discussions.map(discussionMapper::toDto);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> getDiscussionsByCategory(String category) {
        List<Discussion> discussions = discussionRepository.findByCategoryAndStatusNotOrderByCreatedAtDesc(
                category, Discussion.DiscussionStatus.DELETED);
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> getDiscussionsByUserId(String userId, Pageable pageable) {
        Page<Discussion> discussions = discussionRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(
                userId, Discussion.DiscussionStatus.DELETED, pageable);
        return discussions.map(discussionMapper::toDto);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> getDiscussionsByUserId(String userId) {
        List<Discussion> discussions = discussionRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(
                userId, Discussion.DiscussionStatus.DELETED);
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> searchDiscussions(String keyword, Pageable pageable) {
        Page<Discussion> discussions = discussionRepository.searchDiscussions(
                keyword, Discussion.DiscussionStatus.DELETED, pageable);
        return discussions.map(discussionMapper::toDto);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> searchDiscussions(String keyword) {
        List<Discussion> discussions = discussionRepository.searchDiscussions(
                keyword, Discussion.DiscussionStatus.DELETED);
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    public DiscussionDto updateDiscussion(String discussionId, String userId, DiscussionUpdateDto updateDto) {
        log.info("更新讨论: ID={}, 用户ID={}", discussionId, userId);

        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        if (!discussion.getUserId().equals(userId)) {
            throw new UnauthorizedException("您没有权限更新此讨论");
        }

        if (updateDto.getTitle() != null) {
            discussion.setTitle(updateDto.getTitle());
        }
        if (updateDto.getContent() != null) {
            discussion.setContent(updateDto.getContent());
        }
        if (updateDto.getCategory() != null) {
            discussion.setCategory(updateDto.getCategory());
        }
        if (updateDto.getTags() != null) {
            discussion.setTags(updateDto.getTags());
        }

        Discussion updatedDiscussion = discussionRepository.save(discussion);
        log.info("讨论更新成功: ID={}", updatedDiscussion.getId());

        return discussionMapper.toDto(updatedDiscussion);
    }
    @Override
    public void deleteDiscussion(String discussionId, String userId) {
        log.info("删除讨论: ID={}, 用户ID={}", discussionId, userId);

        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        if (!discussion.getUserId().equals(userId)) {
            throw new UnauthorizedException("您没有权限删除此讨论");
        }

        discussion.setStatus(Discussion.DiscussionStatus.DELETED);
        discussionRepository.save(discussion);

        log.info("讨论删除成功: ID={}", discussionId);
    }
    @Override
    public void incrementViewCount(String discussionId) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.incrementViewCount();
        discussionRepository.save(discussion);
    }
    @Override
    public void incrementLikeCount(String discussionId) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.incrementLikeCount();
        discussionRepository.save(discussion);
    }
    @Override
    public void decrementLikeCount(String discussionId) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.decrementLikeCount();
        discussionRepository.save(discussion);
    }
    @Override
    @Transactional(readOnly = true)
    public boolean isLikedByUser(String discussionId, String userId) {
        // 由于当前Discussion实体没有记录点赞用户列表，返回false作为默认实现
        return false;
    }
    @Override
    public void addLike(String discussionId, String userId) {
        incrementLikeCount(discussionId);
    }
    @Override
    public void removeLike(String discussionId, String userId) {
        decrementLikeCount(discussionId);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public DiscussionReplyDto createReply(String discussionId, String userId, DiscussionReplyCreateDto createDto) {
        log.info("创建回复: 讨论ID={}, 用户ID={}", discussionId, userId);

        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        FrontendUser user = frontendUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        DiscussionReply reply = new DiscussionReply();
        reply.setContent(createDto.getContent());
        reply.setDiscussion(discussion);
        reply.setDiscussionId(discussion.getId());
        reply.setUser(user);
        reply.setUserId(user.getId());
        reply.setUsername(user.getUsername());
        reply.setUserAvatar(user.getAvatarUrl());
        reply.setParentId(createDto.getParentId() != null ? Long.valueOf(createDto.getParentId()) : null);
        reply.setReplyToUserId(createDto.getReplyToUserId());
        reply.setReplyToUsername(createDto.getReplyToUsername());
        reply.setStatus(DiscussionReply.ReplyStatus.ACTIVE);
        reply.setLikeCount(0);

        DiscussionReply savedReply = discussionReplyRepository.save(reply);

        discussion.incrementReplyCount();
        discussionRepository.save(discussion);

        log.info("回复创建成功: ID={}", savedReply.getId());

        return discussionReplyMapper.toDto(savedReply);
    }
    @Override
    public DiscussionReplyDto updateReply(String replyId, String userId, DiscussionReplyCreateDto updateDto) {
        log.info("更新回复: ID={}, 用户ID={}", replyId, userId);

        DiscussionReply reply = discussionReplyRepository.findById(( Long) Long.valueOf(replyId))
                .orElseThrow(() -> new ResourceNotFoundException("回复不存在: " + replyId));

        if (!reply.getUserId().equals(userId)) {
            throw new UnauthorizedException("您没有权限更新此回复");
        }

        reply.setContent(updateDto.getContent());
        reply.setUpdatedAt(LocalDateTime.now());

        DiscussionReply updatedReply = discussionReplyRepository.save(reply);
        log.info("回复更新成功: ID={}", updatedReply.getId());

        return discussionReplyMapper.toDto(updatedReply);
    }
    @Override
    public void deleteReply(String replyId, String userId) {
        log.info("删除回复: ID={}, 用户ID={}", replyId, userId);

        DiscussionReply reply = discussionReplyRepository.findById(( Long) Long.valueOf(replyId))
                .orElseThrow(() -> new ResourceNotFoundException("回复不存在: " + replyId));

        if (!reply.getUserId().equals(userId)) {
            throw new UnauthorizedException("您没有权限删除此回复");
        }

        reply.setStatus(DiscussionReply.ReplyStatus.DELETED);
        discussionReplyRepository.save(reply);

        Discussion discussion = discussionRepository.findById(( Long) reply.getDiscussionId())
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + reply.getDiscussionId()));
        discussion.decrementReplyCount();
        discussionRepository.save(discussion);

        log.info("回复删除成功: ID={}", replyId);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionReplyDto> getRepliesByDiscussionId(String discussionId) {
        List<DiscussionReply> replies = discussionReplyRepository.findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(
                ( Long) Long.valueOf(discussionId), DiscussionReply.ReplyStatus.DELETED);
        return discussionReplyMapper.toDtoList(replies);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionReplyDto> getRepliesByDiscussionId(String discussionId,  Pageable pageable) {
        Page<DiscussionReply> replies = discussionReplyRepository.findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(
                ( Long) Long.valueOf(discussionId), DiscussionReply.ReplyStatus.DELETED, pageable);
        return replies.map(discussionReplyMapper::toDto);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionReplyDto> getRepliesByDiscussionIdAndParentId(String discussionId, String parentId) {
        List<DiscussionReply> replies = discussionReplyRepository
                .findByDiscussionIdAndParentIdAndStatusNotOrderByCreatedAtAsc(
                        ( Long) Long.valueOf(discussionId), ( Long) Long.valueOf(parentId),
                        DiscussionReply.ReplyStatus.DELETED);
        return discussionReplyMapper.toDtoList(replies);
    }
    @Override
    @Transactional(readOnly = true)
    public long getTotalDiscussionCount() {
        return discussionRepository.countByStatusNot(Discussion.DiscussionStatus.DELETED);
    }
    @Override
    @Transactional(readOnly = true)
    public long getDiscussionCountByCategory(String category) {
        return discussionRepository.countByCategoryAndStatusNot(category, Discussion.DiscussionStatus.DELETED);
    }
    @Override
    @Transactional(readOnly = true)
    public long getDiscussionCountByUserId(String userId) {
        return discussionRepository.countByUserIdAndStatusNot(userId, Discussion.DiscussionStatus.DELETED);
    }
    @Override
    @Transactional(readOnly = true)
    public long getDiscussionCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return discussionRepository.findByDateRange(startDate, endDate, Discussion.DiscussionStatus.DELETED)
                .size();
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> getTopLikedDiscussions(int limit) {
        List<Discussion> discussions = discussionRepository.findTopLikedDiscussions(
                Discussion.DiscussionStatus.DELETED, org.springframework.data.domain.PageRequest.of(0, limit));
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> getTopViewedDiscussions(int limit) {
        List<Discussion> discussions = discussionRepository.findTopViewedDiscussions(
                Discussion.DiscussionStatus.DELETED, org.springframework.data.domain.PageRequest.of(0, limit));
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> getTopRepliedDiscussions(int limit) {
        List<Discussion> discussions = discussionRepository.findTopRepliedDiscussions(
                Discussion.DiscussionStatus.DELETED, org.springframework.data.domain.PageRequest.of(0, limit));
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> getPinnedDiscussions() {
        List<Discussion> discussions = discussionRepository.findByIsPinnedTrueAndStatusNotOrderByCreatedAtDesc(
                Discussion.DiscussionStatus.DELETED);
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    @Transactional(readOnly = true)
    public List<DiscussionDto> getFeaturedDiscussions() {
        List<Discussion> discussions = discussionRepository.findByIsFeaturedTrueAndStatusNotOrderByCreatedAtDesc(
                Discussion.DiscussionStatus.DELETED);
        return discussionMapper.toDtoList(discussions);
    }
    @Override
    public DiscussionDto pinDiscussion(String discussionId, String adminId) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.setIsPinned(true);
        Discussion updatedDiscussion = discussionRepository.save(discussion);

        log.info("讨论置顶成功: ID={}, 管理员ID={}", discussionId, adminId);
        return discussionMapper.toDto(updatedDiscussion);
    }
    @Override
    public DiscussionDto unpinDiscussion(String discussionId, String adminId) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.setIsPinned(false);
        Discussion updatedDiscussion = discussionRepository.save(discussion);

        log.info("讨论取消置顶成功: ID={}, 管理员ID={}", discussionId, adminId);
        return discussionMapper.toDto(updatedDiscussion);
    }
    @Override
    public DiscussionDto featureDiscussion(String discussionId, String adminId) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.setIsFeatured(true);
        Discussion updatedDiscussion = discussionRepository.save(discussion);

        log.info("讨论加精成功: ID={}, 管理员ID={}", discussionId, adminId);
        return discussionMapper.toDto(updatedDiscussion);
    }
    @Override
    public DiscussionDto unfeatureDiscussion(String discussionId, String adminId) {
        Discussion discussion = discussionRepository
                .findByIdAndStatusNot(Long.valueOf(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));

        discussion.setIsFeatured(false);
        Discussion updatedDiscussion = discussionRepository.save(discussion);

        log.info("讨论取消加精成功: ID={}, 管理员ID={}", discussionId, adminId);
        return discussionMapper.toDto(updatedDiscussion);
    }
}


