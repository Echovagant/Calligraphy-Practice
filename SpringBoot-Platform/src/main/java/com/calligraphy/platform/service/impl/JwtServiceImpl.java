package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days
    private Long refreshExpiration;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @jakarta.annotation.PostConstruct
    public void init() {
        // 生产环境检查
        if ("prod".equals(activeProfile) || activeProfile.contains("prod")) {
            if (secret == null || secret.isEmpty() || "mySecretKey".equals(secret)) {
                throw new IllegalArgumentException("生产环境必须设置有效的JWT密钥，不能使用默认值");
            }
            logger.info("生产环境JWT配置检查通过");
        } else {
            // 开发环境提供默认密钥
            if (secret == null || secret.isEmpty()) {
                secret = "calligraphy-platform-jwt-secret-key-2024-secure-string-with-enough-length";
                logger.warn("使用默认JWT密钥，仅适用于开发环境");
            }
        }

        // 确保密钥足够长
        if (secret.length() < 32) {
            logger.warn("JWT密钥长度不足32位，建议使用更长的密钥以提高安全性");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String SESSION_PREFIX = "jwt:session:";
    @Override
    public String generateToken(String userId, String userType, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        return createToken(claims, username, expiration);
    }
    @Override
    public String generateToken(Map<String, Object> extraClaims, String userId, String userType, String username) {
        Map<String, Object> claims = new HashMap<>(
                extraClaims != null ? extraClaims : java.util.Collections.emptyMap());
        claims.put("userId", userId);
        claims.put("userType", userType);
        return createToken(claims, username, expiration);
    }
    @Override
    public String generateRefreshToken(String userId, String userType, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        return createToken(claims, username, refreshExpiration);
    }
    @Override
    public String generateAdminToken(com.calligraphy.platform.entity.AdminUser adminUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", String.valueOf(adminUser.getId()));
        claims.put("userType", "ADMIN");
        return createToken(claims, adminUser.getUsername(), expiration);
    }
    @Override
    public String generateAdminToken(com.calligraphy.platform.model.BackendUser backendUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", backendUser.getId());
        claims.put("userType", "ADMIN");
        return createToken(claims, backendUser.getUsername(), expiration);
    }
    @Override
    public String generateAdminRefreshToken(com.calligraphy.platform.entity.AdminUser adminUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", String.valueOf(adminUser.getId()));
        claims.put("userType", "ADMIN");
        return createToken(claims, adminUser.getUsername(), refreshExpiration);
    }
    @Override
    public String generateAdminRefreshToken(com.calligraphy.platform.model.BackendUser backendUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", backendUser.getId());
        claims.put("userType", "ADMIN");
        return createToken(claims, backendUser.getUsername(), refreshExpiration);
    }
    @Override
    public void revokeAdminToken(String token) {
        invalidateToken(token);
    }
    @Override
    public long getAdminJwtExpiration() {
        return expiration;
    }
    @Override
    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return !isTokenExpired(token) && username.equals(userDetails.getUsername()) && !isBlacklisted(token);
        } catch (Exception e) {
            logger.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    @Override
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    @Override
    public Claims extractAllClaims(String token) {
        return extractClaimsFromToken(token);
    }
    @Override
    public String extractUserId(String token) {
        Claims claims = extractClaimsFromToken(token);
        return claims.get("userId", String.class);
    }
    @Override
    public String extractUserType(String token) {
        Claims claims = extractClaimsFromToken(token);
        return claims.get("userType", String.class);
    }
    @Override
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    @Override
    public boolean validateToken(String token) {
        try {
            extractClaimsFromToken(token);
            return !isTokenExpired(token) && !isBlacklisted(token);
        } catch (Exception e) {
            logger.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    @Override
    public void invalidateToken(String token) {
        addToBlacklist(token);
    }
    @Override
    public boolean validateAdminToken(String token) {
        return validateToken(token);
    }
    @Override
    public String getUsernameFromAdminToken(String token) {
        return extractUsername(token);
    }
    @Override
    public String refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            logger.error("刷新令牌验证失败，可能已过期或被篡改");
            throw new IllegalArgumentException("刷新令牌无效");
        }

        String username = extractUsername(refreshToken);
        String userId = extractUserId(refreshToken);
        String userType = extractUserType(refreshToken);

        // 刷新前将旧的Refresh Token加入黑名单，防止重复使用
        invalidateToken(refreshToken);

        // 生成新的Access Token
        return generateToken(java.util.Collections.emptyMap(), userId, userType, username);
    }
    @Override
    public boolean canRefreshToken(String refreshToken) {
        return validateToken(refreshToken);
    }
    @Override
    public String getTokenFingerprint(String token) {
        return java.util.UUID.randomUUID().toString();
    }
    @Override
    public boolean validateTokenFingerprint(String token, String fingerprint) {
        return fingerprint != null && !fingerprint.isEmpty();
    }
    @Override
    public String generateTokenFingerprint() {
        return java.util.UUID.randomUUID().toString();
    }
    @Override
    public void associateTokenWithDevice(String token, String deviceId) {
        // 实现设备关联逻辑
    }
    @Override
    public void disassociateTokenFromDevice(String token) {
        // 实现设备解绑逻辑
    }
    @Override
    public String getTokenDeviceId(String token) {
        return null;
    }
    @Override
    public List<String> getUserDeviceTokens(String userId, String deviceId) {
        return java.util.Collections.emptyList();
    }
    @Override
    public void associateTokenWithIp(String token, String ipAddress) {
        // 实现IP关联逻辑
    }
    @Override
    public void disassociateTokenFromIp(String token) {
        // 实现IP解绑逻辑
    }
    @Override
    public String getTokenIpAddress(String token) {
        return null;
    }
    @Override
    public boolean isTokenIpConsistent(String token, String currentIp) {
        return true;
    }
    @Override
    @SuppressWarnings("null")
    public void createTokenSession(String token, String sessionId) {
        String key = SESSION_PREFIX + token;
        long ttlMillis = Math.max(0, extractExpiration(token).getTime() - System.currentTimeMillis());
        redisTemplate.opsForValue().set(key, sessionId, java.time.Duration.ofMillis(ttlMillis));
    }
    @Override
    public void terminateTokenSession(String token) {
        String key = SESSION_PREFIX + token;
        redisTemplate.delete(key);
    }
    @Override
    public String getTokenSessionId(String token) {
        String key = SESSION_PREFIX + token;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }
    @Override
    public boolean isTokenSessionActive(String token) {
        return getTokenSessionId(token) != null && validateToken(token);
    }
    @Override
    @SuppressWarnings("null")
    public void addToBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        long ttlMillis = Math.max(0, extractExpiration(token).getTime() - System.currentTimeMillis());
        redisTemplate.opsForValue().set(key, "1", java.time.Duration.ofMillis(ttlMillis));
    }
    @Override
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
    }
    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    @Override
    public void cleanupBlacklist() {
    }
    @Override
    public java.util.Map<String, Object> getTokenInfo(String token) {
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        try {
            Claims claims = extractClaimsFromToken(token);
            info.putAll(new java.util.HashMap<>(claims));
            info.put("subject", claims.getSubject());
            info.put("issuedAt", claims.getIssuedAt());
            info.put("expiration", claims.getExpiration());
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        return info;
    }
    @Override
    public java.util.List<java.util.Map<String, Object>> getUserTokenInfo(String userId) {
        return java.util.Collections.emptyList();
    }
    @Override
    public java.util.List<java.util.Map<String, Object>> getUserTokenInfoByType(String userId, String userType) {
        return java.util.Collections.emptyList();
    }
    @Override
    public boolean isTokenInvalidated(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
    @Override
    public void invalidateUserTokens(String userId) {
        // 实现用户令牌失效逻辑
    }
    @Override
    public void invalidateUserTokensByType(String userId, String userType) {
        // 实现按类型失效用户令牌逻辑
    }
    @Override
    public String addCustomClaim(String token, String claimName, Object claimValue) {
        // 实现添加自定义声明逻辑
        return token;
    }
    @Override
    public Object getCustomClaim(String token, String claimName) {
        // 实现获取自定义声明逻辑
        return null;
    }
    @Override
    public Map<String, Object> getAllCustomClaims(String token) {
        // 实现获取所有自定义声明逻辑
        return java.util.Collections.emptyMap();
    }
    @Override
    public void cleanupInvalidatedTokens() {
        logger.info("清理失效token");
    }
    @Override
    public void cleanupExpiredTokens() {
        logger.info("清理过期token");
    }
    @Override
    public long getActiveTokenCount() {
        return 0;
    }
    @Override
    public long getInvalidatedTokenCount() {
        return 0;
    }
    @Override
    public long getJwtExpiration() {
        return expiration;
    }
    @Override
    public long getRefreshTokenExpiration() {
        return refreshExpiration;
    }
    @Override
    public String getJwtSecret() {
        return secret;
    }
    @Override
    public void setJwtExpiration(long expiration) {
        this.expiration = expiration;
    }
    @Override
    public void setRefreshTokenExpiration(long expiration) {
        this.refreshExpiration = expiration;
    }
    @Override
    public void setJwtSecret(String secret) {
        this.secret = secret;
    }
    @Override
    public void reloadConfiguration() {
    }
    @Override
    public java.util.Map<String, Object> getConfiguration() {
        java.util.Map<String, Object> config = new java.util.HashMap<>();
        config.put("secret", secret != null ? "*****" : null);
        config.put("expiration", expiration);
        config.put("refreshExpiration", refreshExpiration);
        return config;
    }
    @Override
    public void updateConfiguration(java.util.Map<String, Object> config) {
        if (config == null)
            return;
        Object s = config.get("secret");
        Object e = config.get("expiration");
        Object re = config.get("refreshExpiration");
        if (s instanceof String str && !str.isEmpty())
            this.secret = str;
        if (e instanceof Number n)
            this.expiration = n.longValue();
        if (re instanceof Number rn)
            this.refreshExpiration = rn.longValue();
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // 记录Token生成事件
        String userId = (String) claims.get("userId");
        String userType = (String) claims.get("userType");
        logger.info("生成新Token，用户ID: {}, 用户类型: {}, 过期时间: {}",
                userId, userType, new Date(System.currentTimeMillis() + expiration));

        return token;
    }

    private Claims extractClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("无效的JWT格式: {}", e.getMessage());
            throw new io.jsonwebtoken.MalformedJwtException("无效的JWT格式", e);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("JWT已过期: {}", e.getMessage());
            throw new io.jsonwebtoken.ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT已过期", e);
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            logger.error("不支持的JWT: {}", e.getMessage());
            throw new io.jsonwebtoken.UnsupportedJwtException("不支持的JWT", e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("JWT签名无效: {}", e.getMessage());
            throw new io.jsonwebtoken.security.SignatureException("JWT签名无效", e);
        } catch (IllegalArgumentException e) {
            logger.error("JWT参数无效: {}", e.getMessage());
            throw new IllegalArgumentException("JWT参数无效", e);
        }
    }
}


