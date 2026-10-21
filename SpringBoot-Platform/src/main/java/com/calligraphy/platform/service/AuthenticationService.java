package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.LoginDto;
import com.calligraphy.platform.dto.LoginResponseDto;
import com.calligraphy.platform.dto.RegisterDto;
import com.calligraphy.platform.dto.PasswordResetDto;
import com.calligraphy.platform.dto.RefreshTokenDto;
import java.util.List;

public interface AuthenticationService {
    
    // 前端用户认证
    LoginResponseDto loginFrontendUser(LoginDto loginDto);
    
    LoginResponseDto registerFrontendUser(RegisterDto registerDto);
    
    LoginResponseDto refreshFrontendUserToken(RefreshTokenDto refreshTokenDto);
    
    void logoutFrontendUser(String userId);
    
    void logoutAllFrontendUserDevices(String userId);
    
    // 后端管理员认证
    LoginResponseDto loginBackendUser(LoginDto loginDto);
    
    LoginResponseDto registerBackendUser(RegisterDto registerDto);
    
    LoginResponseDto refreshBackendUserToken(RefreshTokenDto refreshTokenDto);
    
    void logoutBackendUser(String userId);
    
    void logoutAllBackendUserDevices(String userId);
    
    // 通用认证方法
    boolean validateToken(String token);
    
    String extractUserIdFromToken(String token);
    
    String extractUserTypeFromToken(String token);
    
    boolean isTokenExpired(String token);
    
    // 密码管理
    void resetPassword(String email);
    
    void resetPasswordWithToken(PasswordResetDto passwordResetDto);
    
    void changePassword(String userId, String oldPassword, String newPassword);
    
    boolean checkPassword(String userId, String password);
    
    // 双因素认证
    void enableTwoFactorAuth(String userId);
    
    void disableTwoFactorAuth(String userId);
    
    boolean isTwoFactorAuthEnabled(String userId);
    
    String generateTwoFactorAuthSecret(String userId);
    
    boolean verifyTwoFactorAuthCode(String userId, String code);
    
    // 账户锁定
    void lockAccount(String userId);
    
    void unlockAccount(String userId);
    
    boolean isAccountLocked(String userId);
    
    // 登录尝试
    void recordFailedLoginAttempt(String identifier);
    
    void resetFailedLoginAttempts(String userId);
    
    int getFailedLoginAttempts(String userId);
    
    boolean isAccountLockedDueToFailedAttempts(String identifier);
    
    // 会话管理
    List<String> getActiveSessions(String userId);
    
    void terminateSession(String sessionId);
    
    void terminateAllUserSessions(String userId);
    
    // 安全审计
    void recordLoginActivity(String userId, String ipAddress, String userAgent, boolean success);
    
    void recordLogoutActivity(String userId, String ipAddress, String userAgent);
    
    void recordPasswordChangeActivity(String userId, String ipAddress, String userAgent);
    
    // 用户状态检查
    boolean isUserActive(String userId);
    
    boolean isUserVerified(String userId);
    
    boolean isUserDeleted(String userId);
    
    // 权限检查
    boolean hasPermission(String userId, String permissionCode);
    
    boolean hasAnyPermission(String userId, List<String> permissionCodes);
    
    boolean hasAllPermissions(String userId, List<String> permissionCodes);
    
    boolean hasRole(String userId, String roleCode);
    
    boolean hasAnyRole(String userId, List<String> roleCodes);
    
    boolean hasAllRoles(String userId, List<String> roleCodes);
    
    // 用户类型判断
    boolean isFrontendUser(String userId);
    
    boolean isBackendUser(String userId);
    
    // 设备管理
    void registerDevice(String userId, String deviceId, String deviceInfo);
    
    void unregisterDevice(String userId, String deviceId);
    
    List<String> getUserDevices(String userId);
    
    boolean isDeviceRegistered(String userId, String deviceId);
    
    // IP地址管理
    void addTrustedIp(String userId, String ipAddress);
    
    void removeTrustedIp(String userId, String ipAddress);
    
    List<String> getTrustedIps(String userId);
    
    boolean isTrustedIp(String userId, String ipAddress);
    
    // 安全设置
    void updateSecuritySettings(String userId, boolean requireTwoFactorAuth, boolean requireStrongPassword, int maxFailedAttempts);
    
    java.util.Map<String, Object> getSecuritySettings(String userId);
    
    // 审计日志
    List<com.calligraphy.platform.dto.UserActivityLogDto> getUserLoginHistory(String userId, int limit);
    
    List<com.calligraphy.platform.dto.UserActivityLogDto> getUserSecurityEvents(String userId, int limit);
    
    // 系统安全统计
    java.util.Map<String, Object> getSystemSecurityStats();
    
    java.util.Map<String, Object> getUserSecurityStats(String userId);
}


