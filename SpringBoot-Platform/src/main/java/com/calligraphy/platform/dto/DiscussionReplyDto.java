package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 讨论回复。对应契约的 {@code DiscussionReply} schema。
 *
 * <p>字段集：{@code id,content,discussionId,userId,username,userAvatar,parentId,
 * replyToUserId,replyToUsername,status,likeCount,createdAt,updatedAt}。
 * 主键叫 {@code id}（不是 replyId）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionReplyDto {

    /** 回复主键。契约里主键字段一律叫 id。 */
    private String id;

    private String content;

    /** 所属讨论 id（引用别人的 id，保留原名）。 */
    private String discussionId;

    private String userId;
    private String username;
    private String userAvatar;

    /** 父回复 id，顶级回复为 null。 */
    private String parentId;

    private String replyToUserId;
    private String replyToUsername;

    /** ACTIVE / INACTIVE / DELETED。 */
    private String status;

    private Integer likeCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
