package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "评论创建请求DTO")
public class CommentCreateDto {

    @NotBlank(message = "作品ID不能为空")
    @Schema(description = "作品ID", example = "1")
    private Long workId;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500个字符之间")
    @Schema(description = "评论内容", example = "这是一条很好的作品")
    private String content;

    @Schema(description = "父评论ID", example = "null")
    private Long parentId;

    @Schema(description = "评论者ID", example = "1")
    private Long userId;

    public CommentCreateDto() {
    }

    public CommentCreateDto(Long workId, String content, Long parentId, Long userId) {
        this.workId = workId;
        this.content = content;
        this.parentId = parentId;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getWorkId() {
        return workId;
    }

    public void setWorkId(Long workId) {
        this.workId = workId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}