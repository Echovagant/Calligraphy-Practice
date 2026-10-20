package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户活动日志创建数据传输对象")
public class UserActivityLogCreateDto {
    
    @NotBlank(message = "用户ID涓嶈兘涓虹┖")
    @Schema(description = "用户ID", example = "user-id-123")
    private String userId;
    
    @NotBlank(message = "娲诲姩绫诲瀷涓嶈兘涓虹┖")
    @Size(max = 50, message = "活动类型长度不能超过50涓瓧绗")
    @Schema(description = "活动类型", example = "LOGIN")
    private String activityType;
    
    @Size(max = 500, message = "活动描述长度不能超过500涓瓧绗")
    @Schema(description = "活动描述", example = "用户登录成功")
    private String description;
    
    @Size(max = 50, message = "IP地址长度不能超过50涓瓧绗")
    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;
    
    @Size(max = 200, message = "鐢ㄦ埛浠ｇ悊闀垮害涓嶈兘瓒呰繃200涓瓧绗")
    @Schema(description = "鐢ㄦ埛浠ｇ悊", example = "Mozilla/5.0...")
    private String userAgent;
    
    @Size(max = 100, message = "璁惧淇℃伅闀垮害涓嶈兘瓒呰繃100涓瓧绗")
    @Schema(description = "璁惧淇℃伅", example = "Windows 10, Chrome")
    private String deviceInfo;
    
    @Schema(description = "鍝嶅簲鐘舵€", example = "SUCCESS")
    private String responseStatus;
    
    @Size(max = 1000, message = "请求参数长度不能超过1000涓瓧绗")
    @Schema(description = "请求参数", example = "{\"username\":\"test\"}")
    private String requestParams;
}


