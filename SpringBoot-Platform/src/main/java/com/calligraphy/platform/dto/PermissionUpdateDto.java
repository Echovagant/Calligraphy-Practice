package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "鏉冮檺鏇存柊璇锋眰DTO")
public class PermissionUpdateDto {
    
    @NotBlank(message = "鏉冮檺ID涓嶈兘涓虹┖")
    @Schema(description = "鏉冮檺ID", example = "1")
    private Long permissionId;
    
    @Size(max = 50, message = "鏉冮檺鍚嶇О闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    @Schema(description = "鏉冮檺鍚嶇О", example = "USER_CREATE")
    private String name;
    
    @Size(max = 200, message = "鏉冮檺鎻忚堪闀垮害涓嶈兘瓒呰繃200涓瓧绗?)
    @Schema(description = "鏉冮檺鎻忚堪", example = "鍒涘缓鐢ㄦ埛鐨勬潈闄?)
    private String description;
    
    @Schema(description = "鏉冮檺缂栫爜", example = "user:create")
    private String code;
    
    @Schema(description = "鐖舵潈闄怚D", example = "1")
    private Long parentId;
    
    @Schema(description = "鏉冮檺绫诲瀷", example = "menu")
    private String type;
    
    @Schema(description = "鏉冮檺璺緞", example = "/users")
    private String path;
    
    @Schema(description = "鏉冮檺鍥炬爣", example = "user-icon")
    private String icon;
    
    @Schema(description = "鎺掑簭鍙?, example = "1")
    private Integer sortOrder;
    
    @Schema(description = "鐘舵€?, example = "active")
    private String status;
}


