package com.calligraphy.platform.service;

import com.calligraphy.platform.entity.AdminUser;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    
    // JWT鐢熸垚
    String generateToken(String userId, String userType, String username);
    
    String generateToken(Map<String, Object> extraClaims, String userId, String userType, String username);
    
    String generateRefreshToken(String userId, String userType, String username);
    
    String generateAdminToken(AdminUser adminUser);
    
    String generateAdminToken(com.calligraphy.platform.model.BackendUser backendUser);
    
    String generateAdminRefreshToken(AdminUser adminUser);
    
    String generateAdminRefreshToken(com.calligraphy.platform.model.BackendUser backendUser);
    
    void revokeAdminToken(String token);
    
    long getAdminJwtExpiration();
    
    // JWT楠岃瘉
    boolean isTokenValid(String token, UserDetails userDetails);
    
    boolean isTokenExpired(String token);
    
    boolean validateToken(String token);
    
    boolean validateAdminToken(String token);
    
    String getUsernameFromAdminToken(String token);
    
    // JWT瑙ｆ瀽
    String extractUserId(String token);
    
    String extractUserType(String token);
    
    String extractUsername(String token);
    
    Date extractExpiration(String token);
    
    Date extractIssuedAt(String token);
    
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    
    Claims extractAllClaims(String token);
    
    // JWT閰嶇疆
    long getJwtExpiration();
    
    long getRefreshTokenExpiration();
    
    String getJwtSecret();
    
    void setJwtExpiration(long expiration);
    
    void setRefreshTokenExpiration(long expiration);
    
    void setJwtSecret(String secret);
    
    // 浠ょ墝绠＄悊
    void invalidateToken(String token);
    
    void invalidateUserTokens(String userId);
    
    void invalidateUserTokensByType(String userId, String userType);
    
    boolean isTokenInvalidated(String token);
    
    // 浠ょ墝鍒锋柊
    String refreshAccessToken(String refreshToken);
    
    boolean canRefreshToken(String refreshToken);
    
    // 鑷畾涔夊０鏄?    String addCustomClaim(String token, String claimName, Object claimValue);
    
    Object getCustomClaim(String token, String claimName);
    
    Map<String, Object> getAllCustomClaims(String token);
    
    // 瀹夊叏鐩稿叧
    String getTokenFingerprint(String token);
    
    boolean validateTokenFingerprint(String token, String fingerprint);
    
    String generateTokenFingerprint();
    
    // 璁惧绠＄悊
    void associateTokenWithDevice(String token, String deviceId);
    
    void disassociateTokenFromDevice(String token);
    
    String getTokenDeviceId(String token);
    
    List<String> getUserDeviceTokens(String userId, String deviceId);
    
    // IP鍦板潃绠＄悊
    void associateTokenWithIp(String token, String ipAddress);
    
    void disassociateTokenFromIp(String token);
    
    String getTokenIpAddress(String token);
    
    boolean isTokenIpConsistent(String token, String currentIp);
    
    // 浼氳瘽绠＄悊
    void createTokenSession(String token, String sessionId);
    
    void terminateTokenSession(String token);
    
    String getTokenSessionId(String token);
    
    boolean isTokenSessionActive(String token);
    
    // 榛戝悕鍗曠鐞?    void addToBlacklist(String token);
    
    void removeFromBlacklist(String token);
    
    boolean isBlacklisted(String token);
    
    void cleanupBlacklist();
    
    // 浠ょ墝瀹¤
    java.util.Map<String, Object> getTokenInfo(String token);
    
    java.util.List<java.util.Map<String, Object>> getUserTokenInfo(String userId);
    
    java.util.List<java.util.Map<String, Object>> getUserTokenInfoByType(String userId, String userType);
    
    // 鎬ц兘浼樺寲
    void cleanupExpiredTokens();
    
    void cleanupInvalidatedTokens();
    
    long getActiveTokenCount();
    
    long getInvalidatedTokenCount();
    
    // 閰嶇疆绠＄悊
    void reloadConfiguration();
    
    java.util.Map<String, Object> getConfiguration();
    
    void updateConfiguration(java.util.Map<String, Object> config);
}


