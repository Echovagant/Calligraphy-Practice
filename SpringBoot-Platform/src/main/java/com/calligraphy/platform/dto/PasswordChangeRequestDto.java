package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 已登录用户修改自己的密码的请求体，对应契约的 {@code PasswordChangeRequest} schema。
 *
 * <p>这是「用旧密码换新密码」，**不是**「忘记密码重置」——重置需要邮箱验证码，
 * 契约的端点清单里没有那个端点。
 *
 * <p>长度类的问题走 400（Bean Validation）；「两次不一致 / 新旧相同 / 复杂度不足」
 * 属于业务校验，由 UserService 抛 BusinessException → 422。
 */
@Data
public class PasswordChangeRequestDto {

    @NotBlank(message = "旧密码不能为空")
    @Size(min = 6, max = 100, message = "旧密码长度必须在6-100个字符之间")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度必须在6-100个字符之间")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Size(min = 6, max = 100, message = "确认密码长度必须在6-100个字符之间")
    private String confirmPassword;
}
