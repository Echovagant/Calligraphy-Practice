package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "浣滃搧鍒涘缓鏁版嵁浼犺緭瀵硅薄")
public class WorkCreateDto {
    
    @NotBlank(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", example = "user-id-123")
    private String userId;
    
    @NotBlank(message = "浣滃搧鏍囬涓嶈兘涓虹┖")
    @Size(min = 2, max = 100, message = "浣滃搧鏍囬闀垮害蹇呴』鍦?-100涓瓧绗︿箣闂?)
    @Schema(description = "浣滃搧鏍囬", example = "鎴戠殑涔︽硶浣滃搧")
    private String title;
    
    @Size(max = 500, message = "浣滃搧鎻忚堪闀垮害涓嶈兘瓒呰繃500涓瓧绗?)
    @Schema(description = "浣滃搧鎻忚堪", example = "杩欐槸涓€骞呮シ涔︿綔鍝?)
    private String description;
    
    @NotBlank(message = "瀛椾綋绫诲瀷涓嶈兘涓虹┖")
    @Size(max = 50, message = "瀛椾綋绫诲瀷闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    @Schema(description = "瀛椾綋绫诲瀷", example = "妤蜂功")
    private String fontType;
    
    @NotBlank(message = "浣滃搧鍐呭涓嶈兘涓虹┖")
    @Size(max = 2000, message = "浣滃搧鍐呭闀垮害涓嶈兘瓒呰繃2000涓瓧绗?)
    @Schema(description = "浣滃搧鍐呭", example = "澶╅亾閰嫟")
    private String content;
    
    @Size(max = 500, message = "鍥剧墖URL闀垮害涓嶈兘瓒呰繃500涓瓧绗?)
    @Schema(description = "浣滃搧鍥剧墖URL", example = "https://example.com/work.jpg")
    private String imageUrl;
    
    @Schema(description = "鏄惁鍏紑", example = "true")
    private Boolean isPublic;
    
    @Size(max = 100, message = "鏍囩闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Schema(description = "浣滃搧鏍囩锛岄€楀彿鍒嗛殧", example = "妤蜂功,浼犵粺,缁忓吀")
    private String tags;
    
    // 鏃犲弬鏋勯€犲櫒
    public WorkCreateDto() {
    }
    
    // 鍏ㄥ弬鏋勯€犲櫒
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
    
    // Getter 鍜?Setter 鏂规硶
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


