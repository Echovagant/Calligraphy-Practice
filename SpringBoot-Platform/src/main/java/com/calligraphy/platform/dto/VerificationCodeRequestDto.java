package com.calligraphy.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 契约 VerificationCodeRequest：发送邮箱验证码的请求体，只有一个邮箱字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCodeRequestDto {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
