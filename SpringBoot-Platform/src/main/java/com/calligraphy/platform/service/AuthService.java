package com.calligraphy.platform.service;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.dto.AdminLoginRequestDto;
import com.calligraphy.platform.dto.AuthUserDto;
import com.calligraphy.platform.dto.LoginRequestDto;
import com.calligraphy.platform.dto.LoginResponseDto;
import com.calligraphy.platform.dto.RegisterRequestDto;
import com.calligraphy.platform.dto.RegisterResponseDto;

import java.util.Map;

public interface AuthService {

    Map<String, String> login(String username, String password);
    Map<String, String> refreshTokenMap(String refreshToken);
    User register(User user);
    
    LoginResponseDto login(LoginRequestDto loginRequest);
    RegisterResponseDto register(RegisterRequestDto registerRequest);
    LoginResponseDto refreshToken(String refreshToken);
    void logout(String token);
    void logout(String token, String refreshToken);

    /**
     * 按用户名查询用户资料，返回契约 AuthUser 形状（id/username/nickname/email/avatar/role）。
     *
     * @throws com.calligraphy.platform.exception.UserNotFoundException 用户不存在
     */
    AuthUserDto getCurrentUserProfile(String username);

    /**
     * 管理员登录：凭据校验通过后还要校验 role ∈ {ADMIN, SUPER_ADMIN}，
     * 令牌以 userType=ADMIN 生成。
     *
     * @throws com.calligraphy.platform.exception.AuthenticationException 凭据错误
     * @throws org.springframework.security.access.AccessDeniedException  角色不是管理员（403）
     */
    LoginResponseDto loginAsAdmin(AdminLoginRequestDto loginRequest);

    /**
     * 管理员刷新令牌：校验 role ∈ {ADMIN, SUPER_ADMIN} 后生成 userType=ADMIN 的新令牌对。
     */
    LoginResponseDto refreshTokenAsAdmin(String refreshToken);

    /**
     * 发送邮箱验证码：Redis 限流（同一邮箱 60 秒一次）+ 验证码保存 10 分钟 + 尝试发信。
     * 发信失败只记日志，不影响调用方。
     *
     * @return false 表示同一邮箱 60 秒内重复请求，调用方应返回 429
     */
    boolean sendVerificationCode(String email);
}


