package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.Comment;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论 DTO —— 对应 openapi/contract.yaml 的 {@code Comment} schema。
 *
 * <p>字段集与契约逐字对齐：
 * {@code id, workId, workTitle, userId, username, userNickname, userAvatar,
 * parentCommentId, content, status, reviewerId, reviewerComment, likeCount,
 * reviewedAt, createdAt, updatedAt, replies, replyCount, hasReplies,
 * reviewerUsername, reviewerNickname}。
 *
 * <p>主键叫 {@code id}（前端读的是 {@code comment.id}），不是 {@code commentId}。
 * {@code xxxId} 只出现在「引用别人的 id」的字段上（workId / userId / parentCommentId）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    /** 评论主键。契约里叫 id。 */
    private String id;

    /** 被评论的作品 id（引用别人的 id，保留原名）。 */
    private String workId;

    /** 作品标题（联表取回）。 */
    private String workTitle;

    /** 评论者 id（引用别人的 id，保留原名）。 */
    private String userId;
    private String username;
    private String userNickname;
    private String userAvatar;

    /** 父评论 id；根评论为 null。 */
    private String parentCommentId;

    private String content;

    /** PENDING / APPROVED / REJECTED / DELETED。 */
    private Comment.CommentStatus status;

    /** 审核者 id 与审核意见。 */
    private String reviewerId;
    private String reviewerComment;

    private long likeCount;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 嵌套的子评论（只放子评论本身，不递归）。 */
    private List<CommentDto> replies;

    /** 子评论条数。 */
    private long replyCount;

    /**
     * 是否有子评论。
     *
     * <p>用包装类型 {@code Boolean}：primitive {@code boolean} 会被 Jackson 剥掉
     * {@code is} 前缀，JSON 名会变成 {@code hasReplies} 以外的东西
     * （见 docs/api-contract.md §3.1）。
     */
    private Boolean hasReplies;

    /** 审核者信息。 */
    private String reviewerUsername;
    private String reviewerNickname;
}
