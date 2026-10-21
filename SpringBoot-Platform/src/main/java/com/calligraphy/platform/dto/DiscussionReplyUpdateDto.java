package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 编辑回复请求体。对应契约的 {@code DiscussionReplyUpdate} schema：
 * 只有一个字段 {@code content}。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionReplyUpdateDto {

    private String content;
}
