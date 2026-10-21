package com.calligraphy.platform.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class WorkUpdateRequestDto {
    
    @Size(max = 200, message = "作品标题长度不能超过200位")
    private String title;
    
    @Size(max = 1000, message = "作品描述长度不能超过1000位")
    private String description;
    
    @Size(max = 10000, message = "作品内容长度不能超过10000位")
    private String content;
    
    @Size(max = 50, message = "作品分类长度不能超过50位")
    private String category;
    
    private List<@Size(max = 50, message = "标签长度不能超过50位") String> tags;
    
    private List<String> fileUrls;
    
    private String style;
    
    @Size(max = 500, message = "备注长度不能超过500位")
    private String notes;
    
    // 无参构造器
    public WorkUpdateRequestDto() {
    }
    
    // 全参构造器
    public WorkUpdateRequestDto(String title, String description, String content, String category, List<String> tags, 
                               List<String> fileUrls, String style, String notes) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.fileUrls = fileUrls;
        this.style = style;
        this.notes = notes;
    }
    
    // Getter 和 Setter 方法
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public List<String> getFileUrls() {
        return fileUrls;
    }
    
    public void setFileUrls(List<String> fileUrls) {
        this.fileUrls = fileUrls;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // Builder 类
    public static class Builder {
        private String title;
        private String description;
        private String content;
        private String category;
        private List<String> tags;
        private List<String> fileUrls;
        private String style;
        private String notes;
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
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
        
        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }
        
        public Builder fileUrls(List<String> fileUrls) {
            this.fileUrls = fileUrls;
            return this;
        }
        
        public Builder style(String style) {
            this.style = style;
            return this;
        }
        
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }
        
        public WorkUpdateRequestDto build() {
            return new WorkUpdateRequestDto(title, description, content, category, tags, fileUrls, style, notes);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}


