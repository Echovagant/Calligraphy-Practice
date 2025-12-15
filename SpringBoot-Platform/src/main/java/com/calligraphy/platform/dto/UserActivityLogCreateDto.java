package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "鐢ㄦ埛娲诲姩鏃ュ織鍒涘缓鏁版嵁浼犺緭瀵硅薄")
public class UserActivityLogCreateDto {
    
    @NotBlank(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", example = "user-id-123")
    private String userId;
    
    @NotBlank(message = "娲诲姩绫诲瀷涓嶈兘涓虹┖")
    @Size(max = 50, message = "娲诲姩绫诲瀷闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    @Schema(description = "娲诲姩绫诲瀷", example = "LOGIN")
    private String activityType;
    
    @Size(max = 500, message = "娲诲姩鎻忚堪闀垮害涓嶈兘瓒呰繃500涓瓧绗?)
    @Schema(description = "娲诲姩鎻忚堪", example = "鐢ㄦ埛鐧诲綍鎴愬姛")
    private String description;
    
    @Size(max = 50, message = "IP鍦板潃闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    @Schema(description = "IP鍦板潃", example = "192.168.1.1")
    private String ipAddress;
    
    @Size(max = 200, message = "鐢ㄦ埛浠ｇ悊闀垮害涓嶈兘瓒呰繃200涓瓧绗?)
    @Schema(description = "鐢ㄦ埛浠ｇ悊", example = "Mozilla/5.0...")
    private String userAgent;
    
    @Size(max = 100, message = "璁惧淇℃伅闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Schema(description = "璁惧淇℃伅", example = "Windows 10, Chrome")
    private String deviceInfo;
    
    @Schema(description = "鍝嶅簲鐘舵€?, example = "SUCCESS")
    private String responseStatus;
    
    @Size(max = 1000, message = "璇锋眰鍙傛暟闀垮害涓嶈兘瓒呰繃1000涓瓧绗?)
    @Schema(description = "璇锋眰鍙傛暟", example = "{\"username\":\"test\"}")
    private String requestParams;
}


