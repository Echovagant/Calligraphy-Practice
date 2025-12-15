package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.LoginDto;
import com.calligraphy.platform.dto.LoginResponseDto;
import com.calligraphy.platform.dto.RegisterDto;
import com.calligraphy.platform.dto.PasswordResetDto;
import com.calligraphy.platform.dto.RefreshTokenDto;
import java.util.List;

public interface AuthenticationService {
    
    // 鍓嶇鐢ㄦ埛璁よ瘉
    LoginResponseDto loginFrontendUser(LoginDto loginDto);
    
    LoginResponseDto registerFrontendUser(RegisterDto registerDto);
    
    LoginResponseDto refreshFrontendUserToken(RefreshTokenDto refreshTokenDto);
    
    void logoutFrontendUser(String userId);
    
    void logoutAllFrontendUserDevices(String userId);
    
    // 鍚庣绠＄悊鍛樿璇?    LoginResponseDto loginBackendUser(LoginDto loginDto);
    
    LoginResponseDto registerBackendUser(RegisterDto registerDto);
    
    LoginResponseDto refreshBackendUserToken(RefreshTokenDto refreshTokenDto);
    
    void logoutBackendUser(String userId);
    
    void logoutAllBackendUserDevices(String userId);
    
    // 閫氱敤璁よ瘉鏂规硶
    boolean validateToken(String token);
    
    String extractUserIdFromToken(String token);
    
    String extractUserTypeFromToken(String token);
    
    boolean isTokenExpired(String token);
    
    // 瀵嗙爜绠＄悊
    void resetPassword(String email);
    
    void resetPasswordWithToken(PasswordResetDto passwordResetDto);
    
    void changePassword(String userId, String oldPassword, String newPassword);
    
    boolean checkPassword(String userId, String password);
    
    // 鍙屽洜绱犺璇?    void enableTwoFactorAuth(String userId);
    
    void disableTwoFactorAuth(String userId);
    
    boolean isTwoFactorAuthEnabled(String userId);
    
    String generateTwoFactorAuthSecret(String userId);
    
    boolean verifyTwoFactorAuthCode(String userId, String code);
    
    // 璐︽埛閿佸畾
    void lockAccount(String userId);
    
    void unlockAccount(String userId);
    
    boolean isAccountLocked(String userId);
    
    // 鐧诲綍灏濊瘯
    void recordFailedLoginAttempt(String identifier);
    
    void resetFailedLoginAttempts(String userId);
    
    int getFailedLoginAttempts(String userId);
    
    boolean isAccountLockedDueToFailedAttempts(String identifier);
    
    // 浼氳瘽绠＄悊
    List<String> getActiveSessions(String userId);
    
    void terminateSession(String sessionId);
    
    void terminateAllUserSessions(String userId);
    
    // 瀹夊叏瀹¤
    void recordLoginActivity(String userId, String ipAddress, String userAgent, boolean success);
    
    void recordLogoutActivity(String userId, String ipAddress, String userAgent);
    
    void recordPasswordChangeActivity(String userId, String ipAddress, String userAgent);
    
    // 鐢ㄦ埛鐘舵€佹鏌?    boolean isUserActive(String userId);
    
    boolean isUserVerified(String userId);
    
    boolean isUserDeleted(String userId);
    
    // 鏉冮檺妫€鏌?    boolean hasPermission(String userId, String permissionCode);
    
    boolean hasAnyPermission(String userId, List<String> permissionCodes);
    
    boolean hasAllPermissions(String userId, List<String> permissionCodes);
    
    boolean hasRole(String userId, String roleCode);
    
    boolean hasAnyRole(String userId, List<String> roleCodes);
    
    boolean hasAllRoles(String userId, List<String> roleCodes);
    
    // 鐢ㄦ埛绫诲瀷鍒ゆ柇
    boolean isFrontendUser(String userId);
    
    boolean isBackendUser(String userId);
    
    // 璁惧绠＄悊
    void registerDevice(String userId, String deviceId, String deviceInfo);
    
    void unregisterDevice(String userId, String deviceId);
    
    List<String> getUserDevices(String userId);
    
    boolean isDeviceRegistered(String userId, String deviceId);
    
    // IP鍦板潃绠＄悊
    void addTrustedIp(String userId, String ipAddress);
    
    void removeTrustedIp(String userId, String ipAddress);
    
    List<String> getTrustedIps(String userId);
    
    boolean isTrustedIp(String userId, String ipAddress);
    
    // 瀹夊叏璁剧疆
    void updateSecuritySettings(String userId, boolean requireTwoFactorAuth, boolean requireStrongPassword, int maxFailedAttempts);
    
    java.util.Map<String, Object> getSecuritySettings(String userId);
    
    // 瀹¤鏃ュ織
    List<com.calligraphy.platform.dto.UserActivityLogDto> getUserLoginHistory(String userId, int limit);
    
    List<com.calligraphy.platform.dto.UserActivityLogDto> getUserSecurityEvents(String userId, int limit);
    
    // 绯荤粺瀹夊叏缁熻
    java.util.Map<String, Object> getSystemSecurityStats();
    
    java.util.Map<String, Object> getUserSecurityStats(String userId);
}


