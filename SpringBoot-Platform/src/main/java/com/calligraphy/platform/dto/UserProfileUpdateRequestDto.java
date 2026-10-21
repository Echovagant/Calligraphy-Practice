package com.calligraphy.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新当前用户资料的请求体，对应契约的 {@code FrontendUserUpdate} schema。
 *
 * <p>用户 id 从令牌里取，不在请求体里传。契约里另列了 gender / birthday / status，
 * 前端目前不传；这两个字段本地没有对应列，收到也会被忽略。
 */
@Data
public class UserProfileUpdateRequestDto {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Size(max = 500, message = "头像地址长度不能超过500个字符")
    private String avatar;

    @Size(max = 1000, message = "个人简介长度不能超过1000个字符")
    private String bio;

    @Size(max = 20, message = "性别长度不能超过20个字符")
    private String gender;

    private LocalDate birthday;

    @Size(max = 20, message = "状态长度不能超过20个字符")
    private String status;
}
