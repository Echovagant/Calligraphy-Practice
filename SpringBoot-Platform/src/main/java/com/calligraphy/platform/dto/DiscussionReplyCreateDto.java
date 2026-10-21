package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回复讨论请求体。对应契约的 {@code DiscussionReplyCreate} schema。
 * 只有 {@code content} 必填。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionReplyCreateDto {

    @NotBlank(message = "回复内容不能为空")
    private String content;

    /** 父回复 id，顶级回复留空。 */
    private String parentId;

    private String replyToUserId;
    private String replyToUsername;
}
