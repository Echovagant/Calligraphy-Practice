package com.calligraphy.platform.dto;

import jakarta.validation.constraints.Size;

public class DiscussionUpdateDto {
    
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;
    
    private String content;
    
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;
    
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;
    
    // 无参构造器
    public DiscussionUpdateDto() {
    }
    
    // 全参构造器
    public DiscussionUpdateDto(String title, String content, String category, String tags) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
    }
    
    // Getter 和 Setter 方法
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
    
    // Builder 类
    public static class Builder {
        private String title;
        private String content;
        private String category;
        private String tags;
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder tags(String tags) {
            this.tags = tags;
            return this;
        }
        
        public DiscussionUpdateDto build() {
            return new DiscussionUpdateDto(title, content, category, tags);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


