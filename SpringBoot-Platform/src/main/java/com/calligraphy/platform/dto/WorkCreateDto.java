package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "作品创建数据传输对象")
public class WorkCreateDto {
    
    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "user-id-123")
    private String userId;
    
    @NotBlank(message = "作品标题不能为空")
    @Size(min = 2, max = 100, message = "作品标题长度必须在2-100个字符之间")
    @Schema(description = "作品标题", example = "我的书法作品")
    private String title;
    
    @Size(max = 500, message = "作品描述长度不能超过500个字符")
    @Schema(description = "作品描述", example = "这是一幅楷书作品")
    private String description;
    
    @NotBlank(message = "字体类型不能为空")
    @Size(max = 50, message = "字体类型长度不能超过50个字符")
    @Schema(description = "字体类型", example = "楷书")
    private String fontType;
    
    @NotBlank(message = "作品内容不能为空")
    @Size(max = 2000, message = "作品内容长度不能超过2000个字符")
    @Schema(description = "作品内容", example = "天道酬勤")
    private String content;
    
    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    @Schema(description = "作品图片URL", example = "https://example.com/work.jpg")
    private String imageUrl;
    
    @Schema(description = "是否公开", example = "true")
    private Boolean isPublic;
    
    @Size(max = 100, message = "标签长度不能超过100个字符")
    @Schema(description = "作品标签，逗号分隔", example = "楷书,传统,经典")
    private String tags;
    
    // 无参构造器
    public WorkCreateDto() {
    }
    
    // 全参构造器
    public WorkCreateDto(String userId, String title, String description, String fontType, String content, 
                        String imageUrl, Boolean isPublic, String tags) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.fontType = fontType;
        this.content = content;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
        this.tags = tags;
    }
    
    // Getter 和 Setter 方法
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
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
    
    public String getFontType() {
        return fontType;
    }
    
    public void setFontType(String fontType) {
        this.fontType = fontType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
}


