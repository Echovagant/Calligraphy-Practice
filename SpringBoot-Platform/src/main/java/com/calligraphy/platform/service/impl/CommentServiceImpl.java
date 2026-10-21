package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.CommentCreateRequestDto;
import com.calligraphy.platform.dto.CommentDto;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.mapper.CommentMapper;
import com.calligraphy.platform.model.Comment;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.CommentRepository;
import com.calligraphy.platform.service.CommentService;
import com.calligraphy.platform.service.UserService;
import com.calligraphy.platform.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论服务实现。
 *
 * <p>可见性规则：
 * <ul>
 *   <li>作品评论列表是公开端点，只返回审核通过（APPROVED）的评论 —— 待审核的评论
 *       不该出现在公开页面上（与 WorkServiceImpl.getPublicWorks 同一套约定）。</li>
 *   <li>用户评论历史返回该用户全部未删除的根评论。</li>
 *   <li>子评论嵌在 {@code replies} 里，不单独占列表条目。</li>
 * </ul>
 *
 * <p>审核流程：发表评论时用敏感词清单做一次自动审核。命中敏感词的评论**不自动放行**，
 * 落到 PENDING 等人工审核，并在响应里如实返回 status；没命中的按自动审核通过处理。
 */
@Service
@RequiredArgsConstructor
// 类级只读事务：映射 DTO 时会访问 comment.getUser() / comment.getWork() 这类 LAZY 关联，
// 在事务之外访问会抛 LazyInitializationException。
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final WorkService workService;
    private final CommentMapper commentMapper;

    /**
     * 敏感词清单（逗号分隔）。
     *
     * <p>唯一的真相源是 application.properties 的 {@code comment.moderation.sensitive-words}，
     * 这里不再写第二份默认值（见 application.properties 里
     * {@code work.upload.allowed.types} 那条同样的告诫）。
     */
    @Value("${comment.moderation.sensitive-words:}")
    private List<String> sensitiveWords;

    @Override
    @Transactional
    public CommentDto createComment(String username, CommentCreateRequestDto createRequest) {
        User author = userService.findByUsername(username);
        Long workId = parseLongId(createRequest.getWorkId(), "作品");
        Work work = workService.getWorkEntityById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));
        Long parentId = resolveParentId(createRequest.getParentCommentId(), workId);

        boolean sensitive = containsSensitiveContent(createRequest.getContent());
        Comment.CommentStatus status = sensitive
                ? Comment.CommentStatus.PENDING
                : Comment.CommentStatus.APPROVED;

        Comment comment = Comment.builder()
                .content(createRequest.getContent())
                .user(author)
                .work(work)
                .parentId(parentId)
                .likeCount(0)
                .status(status)
                .isFeatured(false)
                .build();
        if (sensitive) {
            comment.setReviewNotes("自动审核：内容包含敏感词，已转入人工审核");
        }

        Comment saved = commentRepository.save(comment);
        workService.updateCommentCount(work.getId(), 1);

        log.info("评论创建成功: ID={}, 作品={}, 状态={}", saved.getId(), workId, status);
        return toDto(saved, Collections.emptyList());
    }

    @Override
    public Page<CommentDto> getCommentsByWorkId(Long workId, Pageable pageable) {
        // 作品不存在就是 404（契约给这个端点声明了 404）
        workService.getWorkEntityById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        Page<Comment> page = commentRepository.findByWorkIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(
                workId, Comment.CommentStatus.APPROVED, pageable);
        Map<Long, List<Comment>> replies = repliesByParent(page.getContent(), true);
        return page.map(comment -> toDto(comment, repliesOf(replies, comment.getId())));
    }

    @Override
    public Page<CommentDto> getCommentsByUserId(String userId, Pageable pageable) {
        // 用户不存在就是 404（契约给这个端点声明了 404）
        userService.findById(userId);

        Page<Comment> page = commentRepository.findByUserIdAndParentIdIsNullAndStatusNotOrderByCreatedAtDesc(
                userId, Comment.CommentStatus.DELETED, pageable);
        Map<Long, List<Comment>> replies = repliesByParent(page.getContent(), false);
        return page.map(comment -> toDto(comment, repliesOf(replies, comment.getId())));
    }

    @Override
    @Transactional
    public void deleteComment(String commentId, String username) {
        Long id = parseLongId(commentId, "评论");
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在: " + commentId));

        User current = userService.findByUsername(username);
        boolean isAuthor = comment.getUser() != null
                && comment.getUser().getId().equals(current.getId());
        if (!isAuthor && !current.isAdmin()) {
            throw new AccessDeniedException("没有权限删除此评论");
        }
        if (Comment.CommentStatus.DELETED.equals(comment.getStatus())) {
            // 已经删过了，别再减一次作品的评论数
            return;
        }

        // 软删除：标记为已删除而不是物理删除
        comment.setStatus(Comment.CommentStatus.DELETED);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
        workService.updateCommentCount(comment.getWorkId(), -1);

        log.info("评论删除成功: ID={}", id);
    }

    @Override
    public boolean containsSensitiveContent(String content) {
        if (content == null || content.isBlank() || sensitiveWords == null || sensitiveWords.isEmpty()) {
            return false;
        }
        String haystack = content.toLowerCase();
        for (String word : sensitiveWords) {
            if (word == null) {
                continue;
            }
            String needle = word.trim().toLowerCase();
            if (!needle.isEmpty() && haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------ 内部工具

    /**
     * 契约里的 id 一律是 string，实体主键是 Long。
     * 解析失败是客户端错误，不能变成 500。
     */
    private Long parseLongId(String rawId, String what) {
        if (rawId == null || rawId.isBlank()) {
            throw new BusinessException(what + " ID 不能为空");
        }
        try {
            return Long.valueOf(rawId.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(what + " ID 格式不正确: " + rawId);
        }
    }

    /** 父评论必须存在，而且得是同一个作品下的评论。 */
    private Long resolveParentId(String parentCommentId, Long workId) {
        if (parentCommentId == null || parentCommentId.isBlank()) {
            return null;
        }
        Long parentId = parseLongId(parentCommentId, "父评论");
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("父评论不存在: " + parentId));
        if (!workId.equals(parent.getWorkId())) {
            throw new BusinessException("父评论不属于该作品");
        }
        return parentId;
    }

    /**
     * 一次性把这批根评论的子评论查出来并按父评论分组（避免逐条查的 N+1）。
     *
     * @param approvedOnly true 表示只要审核通过的子评论（公开列表用），
     *                     false 表示要全部未删除的子评论（用户评论历史用）
     */
    private Map<Long, List<Comment>> repliesByParent(List<Comment> roots, boolean approvedOnly) {
        List<Long> parentIds = roots.stream().map(Comment::getId).toList();
        if (parentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Comment> replies = approvedOnly
                ? commentRepository.findByParentIdInAndStatusOrderByCreatedAtAsc(
                        parentIds, Comment.CommentStatus.APPROVED)
                : commentRepository.findByParentIdInAndStatusNotOrderByCreatedAtAsc(
                        parentIds, Comment.CommentStatus.DELETED);

        Map<Long, List<Comment>> grouped = new LinkedHashMap<>();
        for (Comment reply : replies) {
            grouped.computeIfAbsent(reply.getParentId(), key -> new ArrayList<>()).add(reply);
        }
        return grouped;
    }

    private List<Comment> repliesOf(Map<Long, List<Comment>> grouped, Long parentId) {
        return grouped.getOrDefault(parentId, Collections.emptyList());
    }

    /** 实体 → DTO，并补上 replies / replyCount / hasReplies 三个汇总字段。 */
    private CommentDto toDto(Comment comment, List<Comment> replies) {
        CommentDto dto = commentMapper.toDto(comment);
        List<CommentDto> replyDtos = replies.stream().map(commentMapper::toDto).toList();
        dto.setReplies(replyDtos);
        dto.setReplyCount(replyDtos.size());
        dto.setHasReplies(!replyDtos.isEmpty());
        return dto;
    }
}
