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
@Schema(description = "密码重置请求DTO")
public class PasswordResetDto {
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "user@example.com")
    private String email;
    
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须为6位")
    @Schema(description = "验证码", example = "123456")
    private String verificationCode;
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "新密码", example = "newPassword123")
    private String newPassword;
    
    @NotBlank(message = "确认密码不能为空")
    @Size(min = 6, max = 20, message = "确认密码长度必须在6-20个字符之间")
    @Schema(description = "确认密码", example = "newPassword123")
    private String confirmPassword;
}


