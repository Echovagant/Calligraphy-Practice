package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NotificationUpdateRequestDto {
    
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 200, message = "通知标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "通知内容不能为空")
    @Size(max = 2000, message = "通知内容长度不能超过2000个字符")
    private String content;
    
    @NotBlank(message = "通知类型不能为空")
    @Size(max = 50, message = "通知类型长度不能超过50个字符")
    private String type;
    
    @NotNull(message = "是否已读状态不能为空")
    private Boolean isRead;
    
    private String relatedId;
    
    private String relatedType;
    
    // 无参构造器
    public NotificationUpdateRequestDto() {
    }
    
    // 全参构造器
    public NotificationUpdateRequestDto(String title, String content, String type, Boolean isRead, String relatedId, String relatedType) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.isRead = isRead;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
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


