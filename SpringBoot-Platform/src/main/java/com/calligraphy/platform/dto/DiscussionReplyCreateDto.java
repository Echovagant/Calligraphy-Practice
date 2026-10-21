package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class DiscussionReplyCreateDto {
    
    @NotBlank(message = "回复内容不能为空")
    private String content;
    
    private String parentId;
    
    private String replyToUserId;
    
    private String replyToUsername;
    
    // 无参构造器
    public DiscussionReplyCreateDto() {
    }
    
    // 全参构造器
    public DiscussionReplyCreateDto(String content, String parentId, String replyToUserId, String replyToUsername) {
        this.content = content;
        this.parentId = parentId;
        this.replyToUserId = replyToUserId;
        this.replyToUsername = replyToUsername;
    }
    
    // Getter 和 Setter 方法
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public String getReplyToUserId() {
        return replyToUserId;
    }
    
    public void setReplyToUserId(String replyToUserId) {
        this.replyToUserId = replyToUserId;
    }
    
    public String getReplyToUsername() {
        return replyToUsername;
    }
    
    public void setReplyToUsername(String replyToUsername) {
        this.replyToUsername = replyToUsername;
    }
    
    // Builder 类
    public static class Builder {
        private String content;
        private String parentId;
        private String replyToUserId;
        private String replyToUsername;
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }
        
        public Builder replyToUserId(String replyToUserId) {
            this.replyToUserId = replyToUserId;
            return this;
        }
        
        public Builder replyToUsername(String replyToUsername) {
            this.replyToUsername = replyToUsername;
            return this;
        }
        
        public DiscussionReplyCreateDto build() {
            return new DiscussionReplyCreateDto(content, parentId, replyToUserId, replyToUsername);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


