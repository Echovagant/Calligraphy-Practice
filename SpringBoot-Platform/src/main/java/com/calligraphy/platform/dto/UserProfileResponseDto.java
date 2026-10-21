package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户资料响应体，对应契约的 {@code FrontendUser} schema。
 *
 * <p>与历史的 {@code FrontendUserDto} 的区别只有两点，都是契约对齐必须的：
 * <ul>
 *   <li>主键叫 {@code id}（前端读 {@code user.id}），不叫 {@code userId}；</li>
 *   <li>布尔字段用 {@code @JsonProperty} 显式固定成 {@code isVerified} /
 *       {@code isTwoFactorEnabled} —— Lombok 生成的是 {@code isVerified()}，
 *       Jackson 默认会把属性名剥成 {@code verified}。</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {

    /** users 表主键（UUID 字符串）。 */
    private String id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String phone;
    private String bio;
    private String preferences;
    private User.UserStatus status;

    @JsonProperty("isVerified")
    private boolean verified;

    @JsonProperty("isTwoFactorEnabled")
    private boolean twoFactorEnabled;

    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 统计信息
    private long practiceCount;
    private long workCount;
    private long commentCount;
    private long totalLikesReceived;
    private long totalViewsReceived;

    // 角色信息
    private List<String> roles;

    // 活动信息
    private LocalDateTime lastActivityAt;
    private String lastActivityType;
}
