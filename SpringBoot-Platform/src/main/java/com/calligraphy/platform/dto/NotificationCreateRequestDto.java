package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NotificationCreateRequestDto {
    
    @NotBlank(message = "接收者ID不能为空")
    private String recipientId;
    
    @NotBlank(message = "通知内容不能为空")
    private String content;
    
    @NotBlank(message = "通知类型不能为空")
    private String type;
    
    private String title;
    
    private String relatedId;
    
    private String relatedType;
    
    // 无参构造器
    public NotificationCreateRequestDto() {
    }
    
    // 全参构造器
    public NotificationCreateRequestDto(String recipientId, String content, String type, String title, String relatedId, String relatedType) {
        this.recipientId = recipientId;
        this.content = content;
        this.type = type;
        this.title = title;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
    }
    
    // Getter 和 Setter 方法
    public String getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getRelatedId() {
        return relatedId;
    }
    
    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }
    
    public String getRelatedType() {
        return relatedType;
    }
    
    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }
}


