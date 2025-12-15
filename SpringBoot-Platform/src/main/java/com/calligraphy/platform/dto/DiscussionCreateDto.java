package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DiscussionCreateDto {

    @NotBlank(message = "讨论标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "讨论内容不能为空")
    private String content;

    @NotBlank(message = "讨论分类不能为空")
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;

    public DiscussionCreateDto() {
    }

    public DiscussionCreateDto(String title, String content, String category, String tags) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}