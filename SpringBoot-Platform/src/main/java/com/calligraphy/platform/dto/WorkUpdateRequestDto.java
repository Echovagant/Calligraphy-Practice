package com.calligraphy.platform.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class WorkUpdateRequestDto {
    
    @Size(max = 200, message = "浣滃搧鏍囬闀垮害涓嶈兘瓒呰繃200浣?)
    private String title;
    
    @Size(max = 1000, message = "浣滃搧鎻忚堪闀垮害涓嶈兘瓒呰繃1000浣?)
    private String description;
    
    @Size(max = 10000, message = "浣滃搧鍐呭闀垮害涓嶈兘瓒呰繃10000浣?)
    private String content;
    
    @Size(max = 50, message = "浣滃搧鍒嗙被闀垮害涓嶈兘瓒呰繃50浣?)
    private String category;
    
    private List<@Size(max = 50, message = "鏍囩闀垮害涓嶈兘瓒呰繃50浣?) String> tags;
    
    private List<String> fileUrls;
    
    private String style;
    
    @Size(max = 500, message = "澶囨敞闀垮害涓嶈兘瓒呰繃500浣?)
    private String notes;
    
    // 鏃犲弬鏋勯€犲櫒
    public WorkUpdateRequestDto() {
    }
    
    // 鍏ㄥ弬鏋勯€犲櫒
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
    
    // Getter 鍜?Setter 鏂规硶
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
    
    // Builder 绫?    public static class Builder {
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


