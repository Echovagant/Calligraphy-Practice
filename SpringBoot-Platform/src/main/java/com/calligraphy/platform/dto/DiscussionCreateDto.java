package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发帖请求体。对应契约的 {@code DiscussionCreate} schema。
 *
 * <p>契约里只有 {@code title} 与 {@code content} 是必填；{@code category} 可选，
 * 落库时由服务层补默认分类（discussions.category 列是 NOT NULL）。
 * {@code tags} 是逗号分隔的字符串。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionCreateDto {

    @NotBlank(message = "讨论标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "讨论内容不能为空")
    private String content;

    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;
}
