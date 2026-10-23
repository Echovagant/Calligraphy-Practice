package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.DiscussionCreateDto;
import com.calligraphy.platform.dto.DiscussionDto;
import com.calligraphy.platform.dto.DiscussionLikeResultDto;
import com.calligraphy.platform.dto.DiscussionReplyCreateDto;
import com.calligraphy.platform.dto.DiscussionReplyDto;
import com.calligraphy.platform.dto.DiscussionReplyUpdateDto;
import com.calligraphy.platform.dto.DiscussionUpdateDto;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.mapper.DiscussionMapper;
import com.calligraphy.platform.mapper.DiscussionReplyMapper;
import com.calligraphy.platform.model.Discussion;
import com.calligraphy.platform.model.DiscussionLike;
import com.calligraphy.platform.model.DiscussionReply;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.DiscussionLikeRepository;
import com.calligraphy.platform.repository.DiscussionReplyRepository;
import com.calligraphy.platform.repository.DiscussionRepository;
import com.calligraphy.platform.service.DiscussionService;
import com.calligraphy.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 讨论域的服务实现，对应契约的 14 个 operation。
 *
 * <p>类级 {@code @Transactional(readOnly = true)}：映射 DTO 时会读关联字段，
 * 事务外访问 LAZY 关联会抛 LazyInitializationException（P3 踩过的坑）。
 * 写方法自己加 {@code @Transactional}（可写），覆盖类级的只读设置 ——
 * 只读事务里 Hibernate 的 FlushMode 是 MANUAL，改动不会被刷进数据库。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionServiceImpl implements DiscussionService {

    private static final Logger log = LoggerFactory.getLogger(DiscussionServiceImpl.class);

    /** 契约里 category 可选，但 discussions.category 列是 NOT NULL，缺省时用它兜底。 */
    private static final String DEFAULT_CATEGORY = "学习交流";

    /** 详情里内联的回复条数上限；更多回复走 discussionReplyList 端点分页取。 */
    private static final int DETAIL_REPLY_LIMIT = 100;

    private final DiscussionRepository discussionRepository;
    private final DiscussionReplyRepository discussionReplyRepository;
    private final DiscussionLikeRepository discussionLikeRepository;
    private final DiscussionMapper discussionMapper;
    private final DiscussionReplyMapper discussionReplyMapper;
    private final UserService userService;

    // ------------------------------------------------------------------ 列表类

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> getDiscussions(Pageable pageable, String category, String keyword, String sort) {
        Pageable effective = applySort(pageable, sort);
        String cat = trimToNull(category);
        String kw = trimToNull(keyword);

        Page<Discussion> page;
        if (cat != null && kw != null) {
            page = discussionRepository.searchByCategoryAndKeyword(
                    cat, kw, Discussion.DiscussionStatus.DELETED, effective);
        } else if (cat != null) {
            page = discussionRepository.findByCategory(cat, Discussion.DiscussionStatus.DELETED, effective);
        } else if (kw != null) {
            page = discussionRepository.searchByKeyword(kw, Discussion.DiscussionStatus.DELETED, effective);
        } else {
            page = discussionRepository.findByStatus(Discussion.DiscussionStatus.DELETED, effective);
        }
        return toDtoPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> getHotDiscussions(Pageable pageable) {
        return toDtoPage(discussionRepository.findHot(Discussion.DiscussionStatus.DELETED, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> getFeaturedDiscussions(Pageable pageable) {
        return toDtoPage(discussionRepository.findFeatured(Discussion.DiscussionStatus.DELETED, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> getPinnedDiscussions(Pageable pageable) {
        return toDtoPage(discussionRepository.findPinned(Discussion.DiscussionStatus.DELETED, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionDto> searchDiscussions(String keyword, Pageable pageable) {
        String kw = trimToNull(keyword);
        if (kw == null) {
            return toDtoPage(discussionRepository.findByStatus(Discussion.DiscussionStatus.DELETED, pageable));
        }
        return toDtoPage(discussionRepository.searchByKeyword(kw, Discussion.DiscussionStatus.DELETED, pageable));
    }

    // ------------------------------------------------------------------ 写

    @Override
    @Transactional
    public DiscussionDto createDiscussion(String username, DiscussionCreateDto createDto) {
        User user = requireUser(username);

        Discussion discussion = new Discussion();
        discussion.setTitle(createDto.getTitle());
        discussion.setContent(createDto.getContent());
        discussion.setCategory(normalizeCategory(createDto.getCategory()));
        discussion.setTags(createDto.getTags());
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

        Discussion saved = discussionRepository.save(discussion);
        log.info("讨论创建成功: ID={}, 作者={}", saved.getId(), username);

        DiscussionDto dto = discussionMapper.toDto(saved);
        dto.setReplies(List.of());
        // 刚建出来的讨论没人赞过（包括作者自己），不用去查点赞表。
        dto.setLiked(Boolean.FALSE);
        return dto;
    }

    @Override
    @Transactional
    public DiscussionDto updateDiscussion(String discussionId, String username, DiscussionUpdateDto updateDto) {
        Discussion discussion = requireDiscussion(discussionId);
        requireOwnerOrAdmin(discussion.getUserId(), username);

        if (updateDto.getTitle() != null) {
            discussion.setTitle(updateDto.getTitle());
        }
        if (updateDto.getContent() != null) {
            discussion.setContent(updateDto.getContent());
        }
        String category = trimToNull(updateDto.getCategory());
        if (category != null) {
            discussion.setCategory(category);
        }
        if (updateDto.getTags() != null) {
            discussion.setTags(updateDto.getTags());
        }

        Discussion saved = discussionRepository.save(discussion);
        log.info("讨论更新成功: ID={}", saved.getId());

        DiscussionDto dto = discussionMapper.toDto(saved);
        dto.setReplies(List.of());
        dto.setLiked(loadLikedFlags(List.of(saved)).getOrDefault(saved.getId(), Boolean.FALSE));
        return dto;
    }

    @Override
    @Transactional
    public void deleteDiscussion(String discussionId, String username) {
        Discussion discussion = requireDiscussion(discussionId);
        requireOwnerOrAdmin(discussion.getUserId(), username);

        discussion.setStatus(Discussion.DiscussionStatus.DELETED);
        discussionRepository.save(discussion);
        log.info("讨论删除成功: ID={}", discussion.getId());
    }

    @Override
    @Transactional
    public DiscussionLikeResultDto toggleLike(String discussionId, String username) {
        Discussion discussion = requireDiscussion(discussionId);
        User user = requireUser(username);

        // 去重依据 = (讨论, 当前用户)：已经赞过就取消，没赞过就点赞。
        // 同一用户重复点赞不会把 likeCount 一次次往上加 —— 计数不会超过点赞人数。
        Optional<DiscussionLike> existing =
                discussionLikeRepository.findByDiscussionIdAndUserId(discussion.getId(), user.getId());

        boolean liked;
        if (existing.isPresent()) {
            discussionLikeRepository.delete(existing.get());
            discussion.decrementLikeCount();
            liked = false;
        } else {
            discussionLikeRepository.save(DiscussionLike.builder()
                    .discussionId(discussion.getId())
                    .userId(user.getId())
                    .build());
            discussion.incrementLikeCount();
            liked = true;
        }
        discussionRepository.save(discussion);

        return DiscussionLikeResultDto.builder()
                .liked(liked)
                .likeCount(discussion.getLikeCount())
                .build();
    }

    @Override
    @Transactional
    public DiscussionReplyDto createReply(String discussionId, String username, DiscussionReplyCreateDto createDto) {
        Discussion discussion = requireDiscussion(discussionId);
        User user = requireUser(username);

        DiscussionReply reply = new DiscussionReply();
        reply.setContent(createDto.getContent());
        reply.setDiscussion(discussion);
        reply.setDiscussionId(discussion.getId());
        reply.setUser(user);
        reply.setUserId(user.getId());
        reply.setUsername(user.getUsername());
        reply.setUserAvatar(user.getAvatarUrl());
        reply.setParentId(parseOptionalId(createDto.getParentId()));
        reply.setReplyToUserId(createDto.getReplyToUserId());
        reply.setReplyToUsername(createDto.getReplyToUsername());
        reply.setStatus(DiscussionReply.ReplyStatus.ACTIVE);
        reply.setLikeCount(0);

        DiscussionReply saved = discussionReplyRepository.save(reply);

        discussion.incrementReplyCount();
        discussionRepository.save(discussion);

        log.info("回复创建成功: ID={}, 讨论={}", saved.getId(), discussion.getId());
        return discussionReplyMapper.toDto(saved);
    }

    @Override
    @Transactional
    public DiscussionReplyDto updateReply(String replyId, String username, DiscussionReplyUpdateDto updateDto) {
        DiscussionReply reply = requireReply(replyId);
        requireOwnerOrAdmin(reply.getUserId(), username);

        String content = trimToNull(updateDto.getContent());
        if (content != null) {
            reply.setContent(content);
        }

        DiscussionReply saved = discussionReplyRepository.save(reply);
        log.info("回复更新成功: ID={}", saved.getId());
        return discussionReplyMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteReply(String replyId, String username) {
        DiscussionReply reply = requireReply(replyId);
        requireOwnerOrAdmin(reply.getUserId(), username);

        reply.setStatus(DiscussionReply.ReplyStatus.DELETED);
        discussionReplyRepository.save(reply);

        Discussion discussion = discussionRepository.findById(reply.getDiscussionId())
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + reply.getDiscussionId()));
        discussion.decrementReplyCount();
        discussionRepository.save(discussion);

        log.info("回复删除成功: ID={}", reply.getId());
    }

    // ------------------------------------------------------------------ 详情

    @Override
    @Transactional
    public DiscussionDto getDiscussionById(String discussionId) {
        Discussion discussion = requireDiscussion(discussionId);

        // 浏览量是写操作，这里必须是可写事务（只读事务不会把改动刷进数据库）。
        discussion.incrementViewCount();
        discussionRepository.save(discussion);

        DiscussionDto dto = discussionMapper.toDto(discussion);

        List<DiscussionReply> replies = discussionReplyRepository
                .findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(
                        discussion.getId(),
                        DiscussionReply.ReplyStatus.DELETED,
                        PageRequest.of(0, DETAIL_REPLY_LIMIT))
                .getContent();
        dto.setReplies(discussionReplyMapper.toDtoList(replies));
        dto.setLiked(loadLikedFlags(List.of(discussion)).getOrDefault(discussion.getId(), Boolean.FALSE));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiscussionReplyDto> getRepliesByDiscussionId(String discussionId, Pageable pageable) {
        Discussion discussion = requireDiscussion(discussionId);
        return discussionReplyRepository
                .findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(
                        discussion.getId(), DiscussionReply.ReplyStatus.DELETED, pageable)
                .map(discussionReplyMapper::toDto);
    }

    // ------------------------------------------------------------------ 内部工具

    /**
     * 列表里的讨论不内联回复（否则每条都要再查一次回复）。
     *
     * <p>同时把 {@code liked} 填上：当前登录用户赞过哪些讨论。
     */
    private Page<DiscussionDto> toDtoPage(Page<Discussion> page) {
        Map<Long, Boolean> likedFlags = loadLikedFlags(page.getContent());
        return page.map(discussion -> {
            DiscussionDto dto = discussionMapper.toDto(discussion);
            dto.setReplies(List.of());
            dto.setLiked(likedFlags.getOrDefault(discussion.getId(), Boolean.FALSE));
            return dto;
        });
    }

    /**
     * 查「当前用户赞过当前这批讨论里的哪些」。
     *
     * <p><b>必须批量查</b>：先把当前页的讨论 id 收集起来，
     * 一次 {@code findByDiscussionIdInAndUserId} 查完，再在内存里配对。
     * 逐条查的话，一页 20 条就是 20 条 SQL —— 那等于又造一个 N+1。
     *
     * <p>未登录（匿名访问）时直接返回空 Map：没人赞过，{@code liked} 全是 false。
     */
    private Map<Long, Boolean> loadLikedFlags(List<Discussion> discussions) {
        if (discussions == null || discussions.isEmpty()) {
            return Map.of();
        }
        String username = currentUsernameOrNull();
        if (username == null) {
            return Map.of();
        }
        User currentUser = userService.findByUsername(username);
        List<Long> discussionIds = discussions.stream()
                .map(Discussion::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (discussionIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> likedIds = discussionLikeRepository
                .findByDiscussionIdInAndUserId(discussionIds, currentUser.getId())
                .stream()
                .map(DiscussionLike::getDiscussionId)
                .collect(Collectors.toSet());
        return discussionIds.stream()
                .collect(Collectors.toMap(id -> id, likedIds::contains));
    }

    /**
     * 当前登录用户名；没登录（匿名访问）返回 null。
     *
     * <p>匿名请求里 SecurityContext 里放的是 AnonymousAuthenticationToken，
     * 它的 name 是 "anonymousUser" —— 不能拿它去查 users（查不到会抛异常），
     * 所以这里显式把它当成「没登录」。
     */
    private String currentUsernameOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String name = authentication.getName();
        return (name == null || name.isBlank()) ? null : name;
    }

    /** 排序：sort 参数是自由字符串，认不出来的按 latest（最新）处理。 */
    private Pageable applySort(Pageable pageable, String sort) {
        Sort resolved = resolveSort(sort);
        if (resolved.isUnsorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), resolved);
    }

    private Sort resolveSort(String sort) {
        String key = sort == null ? "" : sort.trim().toLowerCase(Locale.ROOT);
        switch (key) {
            case "hot":
                return Sort.by(Sort.Direction.DESC, "likeCount", "replyCount", "viewCount");
            case "mostliked":
                return Sort.by(Sort.Direction.DESC, "likeCount", "createdAt");
            case "mostreplied":
                return Sort.by(Sort.Direction.DESC, "replyCount", "createdAt");
            case "mostviewed":
                return Sort.by(Sort.Direction.DESC, "viewCount", "createdAt");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private Discussion requireDiscussion(String discussionId) {
        return discussionRepository
                .findByIdAndStatusNot(parseId(discussionId), Discussion.DiscussionStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("讨论不存在: " + discussionId));
    }

    private DiscussionReply requireReply(String replyId) {
        return discussionReplyRepository
                .findByIdAndStatusNot(parseId(replyId), DiscussionReply.ReplyStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("回复不存在: " + replyId));
    }

    private User requireUser(String username) {
        return userService.findByUsername(username);
    }

    /** 只有作者本人或管理员能改 / 删，否则 403。 */
    private void requireOwnerOrAdmin(String ownerUserId, String username) {
        User user = requireUser(username);
        if (user.isAdmin()) {
            return;
        }
        if (ownerUserId != null && ownerUserId.equals(user.getId())) {
            return;
        }
        throw new AccessDeniedException("没有权限操作这条讨论");
    }

    /**
     * 路径参数是字符串，实体主键是 Long。解析不了就是「没有这个 id」，
     * 按契约返回 404（讨论 / 回复两个端点的错误响应里只有 404，没有 422）。
     */
    private Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("资源不存在: " + id);
        }
    }

    private Long parseOptionalId(String id) {
        String trimmed = trimToNull(id);
        return trimmed == null ? null : parseId(trimmed);
    }

    private String normalizeCategory(String category) {
        String trimmed = trimToNull(category);
        return trimmed == null ? DEFAULT_CATEGORY : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
