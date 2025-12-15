package com.calligraphy.platform.security;

import com.calligraphy.platform.entity.AdminUser;
import com.calligraphy.platform.model.BackendUser;
import com.calligraphy.platform.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret:mySecretKey}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        // 灏哠et<String>杞崲涓篖ist<String>锛屽洜涓篔WT搴撳彲鑳戒笉鏀寔Set绫诲瀷鐨刢laim
        claims.put("roles", new java.util.ArrayList<>(user.getRoles()));
        claims.put("type", "user");
        return createToken(claims, user.getUsername(), jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("type", "refresh");
        return createToken(claims, user.getUsername(), refreshExpiration);
    }

    public String generateAdminToken(AdminUser adminUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", adminUser.getId());
        claims.put("username", adminUser.getUsername());
        claims.put("roles", adminUser.getRoles());
        claims.put("type", "admin");
        return createToken(claims, adminUser.getUsername(), jwtExpiration);
    }

    public String generateAdminToken(BackendUser backendUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", backendUser.getId());
        claims.put("username", backendUser.getUsername());
        claims.put("roles", backendUser.getRole().name());
        claims.put("type", "admin");
        return createToken(claims, backendUser.getUsername(), jwtExpiration);
    }

    public String generateAdminRefreshToken(AdminUser adminUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", adminUser.getId());
        claims.put("type", "admin_refresh");
        return createToken(claims, adminUser.getUsername(), refreshExpiration);
    }

    public String generateAdminRefreshToken(BackendUser backendUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", backendUser.getId());
        claims.put("type", "admin_refresh");
        return createToken(claims, backendUser.getUsername(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("id", Long.class);
    }

    public String getUserTypeFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("type", String.class);
    }

    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    public Long getAdminJwtExpiration() {
        return jwtExpiration;
    }

    public void revokeToken(String token) {
        // 鍦ㄥ疄闄呭簲鐢ㄤ腑锛岃繖閲屽彲浠ュ皢浠ょ墝鍔犲叆榛戝悕鍗?        log.info("Token revoked: {}", token);
    }

    public void revokeAdminToken(String token) {
        // 鍦ㄥ疄闄呭簲鐢ㄤ腑锛岃繖閲屽彲浠ュ皢绠＄悊鍛樹护鐗屽姞鍏ラ粦鍚嶅崟
        log.info("Admin token revoked: {}", token);
    }

    public Boolean validateAdminToken(String token) {
        try {
            String userType = getUserTypeFromToken(token);
            return ("admin".equals(userType) || "admin_refresh".equals(userType)) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Admin token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromAdminToken(String token) {
        return extractUsername(token);
    }
}


