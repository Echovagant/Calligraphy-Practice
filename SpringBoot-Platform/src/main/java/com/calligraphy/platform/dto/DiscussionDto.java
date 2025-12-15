package com.calligraphy.platform.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class DiscussionDto {
    
    private String discussionId;
    private String title;
    private String content;
    private String category;
    private String userId;
    private String username;
    private String userAvatar;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isPinned;
    private Boolean isFeatured;
    private String tags;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private List<DiscussionReplyDto> replies;
    
    // 鏃犲弬鏋勯€犲櫒
    public DiscussionDto() {
    }
    
    // 鍏ㄥ弬鏋勯€犲櫒
    public DiscussionDto(String discussionId, String title, String content, String category, String userId, 
                        String username, String userAvatar, String status, Integer viewCount, Integer likeCount, 
                        Integer replyCount, Boolean isPinned, Boolean isFeatured, String tags, 
                        LocalDateTime createdAt, LocalDateTime updatedAt, List<DiscussionReplyDto> replies) {
        this.discussionId = discussionId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.userId = userId;
        this.username = username;
        this.userAvatar = userAvatar;
        this.status = status;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
        this.isPinned = isPinned;
        this.isFeatured = isFeatured;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.replies = replies;
    }
    
    // Getter 鍜?Setter 鏂规硶
    public String getDiscussionId() {
        return discussionId;
    }
    
    public void setDiscussionId(String discussionId) {
        this.discussionId = discussionId;
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
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserAvatar() {
        return userAvatar;
    }
    
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getViewCount() {
        return viewCount;
    }
    
    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }
    
    public Integer getLikeCount() {
        return likeCount;
    }
    
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }
    
    public Integer getReplyCount() {
        return replyCount;
    }
    
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
    
    public Boolean getIsPinned() {
        return isPinned;
    }
    
    public void setIsPinned(Boolean isPinned) {
        this.isPinned = isPinned;
    }
    
    public Boolean getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<DiscussionReplyDto> getReplies() {
        return replies;
    }
    
    public void setReplies(List<DiscussionReplyDto> replies) {
        this.replies = replies;
    }
    
    // Builder 绫?    public static class Builder {
        private String discussionId;
        private String title;
        private String content;
        private String category;
        private String userId;
        private String username;
        private String userAvatar;
        private String status;
        private Integer viewCount;
        private Integer likeCount;
        private Integer replyCount;
        private Boolean isPinned;
        private Boolean isFeatured;
        private String tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<DiscussionReplyDto> replies;
        
        public Builder discussionId(String discussionId) {
            this.discussionId = discussionId;
            return this;
        }
        
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
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder userAvatar(String userAvatar) {
            this.userAvatar = userAvatar;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder viewCount(Integer viewCount) {
            this.viewCount = viewCount;
            return this;
        }
        
        public Builder likeCount(Integer likeCount) {
            this.likeCount = likeCount;
            return this;
        }
        
        public Builder replyCount(Integer replyCount) {
            this.replyCount = replyCount;
            return this;
        }
        
        public Builder isPinned(Boolean isPinned) {
            this.isPinned = isPinned;
            return this;
        }
        
        public Builder isFeatured(Boolean isFeatured) {
            this.isFeatured = isFeatured;
            return this;
        }
        
        public Builder tags(String tags) {
            this.tags = tags;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder replies(List<DiscussionReplyDto> replies) {
            this.replies = replies;
            return this;
        }
        
        public DiscussionDto build() {
            return new DiscussionDto(discussionId, title, content, category, userId, username, userAvatar, status, 
                                    viewCount, likeCount, replyCount, isPinned, isFeatured, tags, createdAt, 
                                    updatedAt, replies);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


