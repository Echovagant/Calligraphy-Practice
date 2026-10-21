package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户活动日志创建数据传输对象")
public class UserActivityLogCreateDto {
    
    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "user-id-123")
    private String userId;
    
    @NotBlank(message = "活动类型不能为空")
    @Size(max = 50, message = "活动类型长度不能超过50个字符")
    @Schema(description = "活动类型", example = "LOGIN")
    private String activityType;
    
    @Size(max = 500, message = "活动描述长度不能超过500个字符")
    @Schema(description = "活动描述", example = "用户登录成功")
    private String description;
    
    @Size(max = 50, message = "IP地址长度不能超过50个字符")
    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ipAddress;
    
    @Size(max = 200, message = "用户代理长度不能超过200个字符")
    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;
    
    @Size(max = 100, message = "设备信息长度不能超过100个字符")
    @Schema(description = "设备信息", example = "Windows 10, Chrome")
    private String deviceInfo;
    
    @Schema(description = "响应状态", example = "SUCCESS")
    private String responseStatus;
    
    @Size(max = 1000, message = "请求参数长度不能超过1000个字符")
    @Schema(description = "请求参数", example = "{\"username\":\"test\"}")
    private String requestParams;
}


