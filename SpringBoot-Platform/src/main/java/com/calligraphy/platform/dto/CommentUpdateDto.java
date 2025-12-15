package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "评论更新请求DTO")
public class CommentUpdateDto {

    @NotBlank(message = "评论ID不能为空")
    @Schema(description = "评论ID", example = "1")
    private Long commentId;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500个字符之间")
    @Schema(description = "评论内容", example = "这是一条很好的作品")
    private String content;

    public CommentUpdateDto() {
    }

    public CommentUpdateDto(Long commentId, String content) {
        this.commentId = commentId;
        this.content = content;
    }

    // Getters and Setters
    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}