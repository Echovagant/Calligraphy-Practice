package com.calligraphy.platform.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 编辑帖子请求体。对应契约的 {@code DiscussionUpdate} schema：字段全部可选，
 * 只更新传了的字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionUpdateDto {

    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    private String content;

    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;
}
