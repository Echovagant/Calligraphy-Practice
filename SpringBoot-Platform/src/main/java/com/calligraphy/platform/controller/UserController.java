package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.FileUploadResponseDto;
import com.calligraphy.platform.dto.FrontendUserUpdateDto;
import com.calligraphy.platform.dto.PasswordChangeRequestDto;
import com.calligraphy.platform.dto.UserProfileResponseDto;
import com.calligraphy.platform.dto.UserProfileUpdateRequestDto;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.service.FileUploadService;
import com.calligraphy.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户资料接口，对应契约的 userUpdateProfile / userUploadAvatar /
 * userChangePassword / userGetById 四个 operation。
 *
 * <p>成功响应一律是裸资源（主键字段名是 {@code id}，不是 {@code userId}），
 * 失败响应由 exception.GlobalExceptionHandler 统一转成 {code, message}。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileUploadService fileUploadService;

    /**
     * PUT /api/users/profile —— 200 裸 FrontendUser
     *
     * <p>用户 id 从令牌里取，不在请求体里传。
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequestDto request) {
        String userId = currentUserId();
        User updated = userService.updateUser(userId, toUpdateDto(request));
        return ResponseEntity.ok(toProfileResponse(updated));
    }

    /**
     * POST /api/users/avatar —— 200 FileUploadResult
     *
     * <p>multipart 字段名是 {@code avatar}（前端就是这么发的）。
     * FileUploadServiceImpl.uploadAvatar 内部已经把头像写进用户资料。
     */
    @PostMapping("/avatar")
    public ResponseEntity<FileUploadResponseDto> uploadAvatar(
            @RequestParam("avatar") MultipartFile avatar) {
        return ResponseEntity.ok(fileUploadService.uploadAvatar(avatar, currentUsername()));
    }

    /**
     * PUT /api/users/password —— 204 无响应体
     *
     * <p>已登录用户改自己的密码，用旧密码校验，不需要邮箱验证码。
     */
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequestDto request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        userService.changePassword(currentUserId(), request.getOldPassword(),
                request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/users/{userId} —— 200 裸 FrontendUser
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDto> getById(@PathVariable String userId) {
        return ResponseEntity.ok(toProfileResponse(userService.findById(userId)));
    }

    // ------------------------------------------------------------ 内部工具

    /** 当前登录用户名（JwtAuthenticationFilter 放入的 principal 是 UserDetails）。 */
    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new com.calligraphy.platform.exception.AuthenticationException("未认证或令牌无效");
        }
        return authentication.getName();
    }

    /** 当前登录用户 id。 */
    private String currentUserId() {
        return userService.findByUsername(currentUsername()).getId();
    }

    /**
     * 把请求 DTO 映射成后端 UserService 用的 FrontendUserUpdateDto。
     *
     * <p>不传 status —— 用户不能自己改账户状态（status 是枚举，
     * 传错值会让后端抛 IllegalArgumentException 变成 500）。
     * gender / birthday 在 model/User 上没有对应列，忽略。
     */
    private FrontendUserUpdateDto toUpdateDto(UserProfileUpdateRequestDto request) {
        return FrontendUserUpdateDto.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .avatar(request.getAvatar())
                .bio(request.getBio())
                .build();
    }

    /** 实体 → 契约的 FrontendUser 形状（主键叫 id）。 */
    private UserProfileResponseDto toProfileResponse(User user) {
        Map<String, Long> counts = userService.getUserCounts(user.getId());
        return UserProfileResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .phone(user.getPhone())
                .bio(user.getBio())
                .preferences(user.getPreferences())
                .status(user.getStatus())
                .verified(Boolean.TRUE.equals(user.getEmailVerified()))
                .twoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()))
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .failedLoginAttempts(user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts())
                .lockedUntil(user.getAccountLockedUntil())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .practiceCount(counts.getOrDefault("practiceCount", 0L))
                .workCount(counts.getOrDefault("workCount", 0L))
                .commentCount(counts.getOrDefault("commentCount", 0L))
                .totalLikesReceived(counts.getOrDefault("totalLikesReceived", 0L))
                .totalViewsReceived(counts.getOrDefault("totalViewsReceived", 0L))
                .roles(user.getRole() == null ? List.of() : List.of("ROLE_" + user.getRole().name()))
                .build();
    }
}
