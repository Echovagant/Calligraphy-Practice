package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "后端用户更新请求DTO")
public class BackendUserUpdateDto {
    
    @NotBlank(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "昵称", example = "用户昵称")
    private String nickname;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;
    
    @Schema(description = "手机号码", example = "13800138000")
    private String phone;
    
    @Schema(description = "部门", example = "技术部")
    private String department;
    
    @Schema(description = "职位", example = "开发工程师")
    private String position;
    
    @Schema(description = "状态", example = "active")
    private String status;
    
    @Schema(description = "角色ID列表", example = "[1, 2, 3]")
    private java.util.List<Long> roleIds;
}