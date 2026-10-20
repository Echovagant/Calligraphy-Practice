package com.calligraphy.platform.util;

import com.calligraphy.platform.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Autowired
    private JwtProperties jwtProperties;

    // 缂撳瓨绛惧悕瀵嗛挜锛岀‘淇濆簲鐢ㄧ敓鍛藉懆鏈熷唴浣跨敤鍚屼竴涓瘑閽
    private Key signingKey;
    
    // 鍒濆鍖栧瘑閽ワ紝纭繚搴旂敤鍚姩鏃跺彧鐢熸垚涓€娆
    @Override
    public void afterPropertiesSet() {
        try {
            logger.info("鍒濆鍖朖WT签名密钥");
            // 优先使用配置的密钥，如果密钥强度不足则生成新密钥
            if (jwtProperties.getSecret() != null && jwtProperties.getSecret().length() >= 64) { // HS512闇€瑕佽嚦灏?4字节密钥
                signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
                logger.info("浣跨敤閰嶇疆鐨凧WT密钥");
            } else {
                logger.warn("閰嶇疆鐨凧WT瀵嗛挜涓嶅瓨鍦ㄦ垨寮哄害涓嶈冻锛岀敓鎴愭柊鐨勫畨鍏ㄥ瘑閽");
                signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            }
        } catch (Exception e) {
            logger.error("鍒濆鍖朖WT签名密钥失败: {}", e.getMessage());
            signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
    }
    
    // 浣跨敤缂撳瓨鐨勫瘑閽
    private Key getSigningKey() {
        return signingKey;
    }

    // 生成访问令牌
    public String generateAccessToken(Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.info("涓虹敤鎴风敓鎴愯闂护鐗? {}", userDetails.getUsername());
            
            return Jwts.builder()
                    .setId(UUID.randomUUID().toString())
                    .setSubject(userDetails.getUsername())
                    .claim("roles", userDetails.getAuthorities()) // 瀛樺偍鐢ㄦ埛瑙掕壊淇℃伅
                    .claim("type", "ACCESS") // 鏄庣‘鏍囪瘑涓鸿闂护鐗?                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                    .setIssuer(jwtProperties.getIssuer())
                    .setAudience(jwtProperties.getAudience())
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512) // 鏄庣‘指定算法
                    .compact();
        } catch (Exception e) {
            logger.error("生成访问令牌失败: {}", e.getMessage());
            throw new RuntimeException("生成令牌失败", e);
        }
    }

    // 鐢熸垚璁块棶浠ょ墝锛堥噸杞芥柟娉曪紝鏀寔鐩存帴浣跨敤鐢ㄦ埛鍚嶅拰鏉冮檺锛
    public String generateAccessToken(String username, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        return generateToken(username, authorities);
    }
    
    // 生成刷新令牌
    public String generateRefreshToken(Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.info("涓虹敤鎴风敓鎴愬埛鏂颁护鐗? {}", userDetails.getUsername());
            
            return Jwts.builder()
                    .setId(UUID.randomUUID().toString())
                    .setSubject(userDetails.getUsername())
                    .claim("type", "REFRESH") // 鏄庣‘鏍囪瘑涓哄埛鏂颁护鐗?                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
                    .setIssuer(jwtProperties.getIssuer())
                    .setAudience(jwtProperties.getAudience())
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512) // 鏄庣‘指定算法
                    .compact();
        } catch (Exception e) {
            logger.error("生成刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException("生成令牌失败", e);
        }
    }

    // 浠庝护鐗屼腑鑾峰彇鐢ㄦ埛鍚
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            logger.error("从令牌获取用户名失败: {}", e.getMessage());
            throw new RuntimeException("鏃犳硶瑙ｆ瀽浠ょ墝", e);
        }
    }
    
    // 从令牌中获取声明
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("从令牌中获取声明失败: {}", e.getMessage());
            throw new RuntimeException("鏃犳晥鐨勪护鐗", e);
        }
    }

    // 验证令牌
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            logger.error("鏃犳晥鐨凧WT签名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("鏃犳晥鐨凧WT令牌: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT澹版槑瀛楃涓蹭负绌? {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT浠ょ墝宸茶繃鏈? {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT声明验证失败: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT令牌验证失败: {}", e.getMessage());
        }
        return false;
    }

    // 鍒ゆ柇鏄惁涓哄埛鏂颁护鐗
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // 浼樺厛妫€鏌ユ槑纭殑type声明
            Object typeClaim = claims.get("type");
            if (typeClaim != null && "REFRESH".equals(typeClaim.toString())) {
                return true;
            }
            // 鍏煎鏃х殑鍒ゆ柇鏂瑰紡锛屼綔涓哄悗澶囨柟妗
            return !claims.containsKey("roles");
        } catch (Exception e) {
            logger.warn("妫€鏌ヤ护鐗岀被鍨嬪け璐? {}", e.getMessage());
            return false;
        }
    }
    
    // 鍒ゆ柇鏄惁涓鸿闂护鐗
    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            // 浼樺厛妫€鏌ユ槑纭殑type声明
            Object typeClaim = claims.get("type");
            if (typeClaim != null && "ACCESS".equals(typeClaim.toString())) {
                return true;
            }
            // 鍏煎鏃х殑鍒ゆ柇鏂瑰紡锛屼綔涓哄悗澶囨柟妗
            return claims.containsKey("roles");
        } catch (Exception e) {
            logger.warn("妫€鏌ヤ护鐗岀被鍨嬪け璐? {}", e.getMessage());
            return false;
        }
    }

    // 刷新令牌
    public String refreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String username = claims.getSubject();
            logger.info("刷新用户令牌: {}", username);

            // 楠岃瘉鏄惁涓哄埛鏂颁护鐗
            if (!isRefreshToken(refreshToken)) {
                logger.error("鎻愪緵鐨勪笉鏄埛鏂颁护鐗? {}", username);
                throw new RuntimeException("鏃犳晥鐨勫埛鏂颁护鐗");
            }

            // 验证签发者和受众
            String issuer = claims.getIssuer();
            String audience = claims.getAudience();
            
            if (!jwtProperties.getIssuer().equals(issuer)) {
                logger.error("签发者不匹配: 期望={}, 实际={}", jwtProperties.getIssuer(), issuer);
                throw new RuntimeException("鏃犳晥鐨勪护鐗岀鍙戣€");
            }
            
            if (!jwtProperties.getAudience().equals(audience)) {
                logger.error("鍙椾紬涓嶅尮閰? 期望={}, 实际={}", jwtProperties.getAudience(), audience);
                throw new RuntimeException("鏃犳晥鐨勪护鐗屽彈浼");
            }

            return username;
        } catch (ExpiredJwtException e) {
            logger.error("鍒锋柊浠ょ墝宸茶繃鏈? {}", e.getMessage());
            throw new RuntimeException("鍒锋柊浠ょ墝宸茶繃鏈", e);
        } catch (Exception ex) {
            logger.error("无法刷新令牌: {}", ex.getMessage());
            throw new RuntimeException("无法刷新令牌", ex);
        }
    }
    
    // 生成令牌（支持直接使用用户名，不依赖Authentication瀵硅薄锛
    public String generateToken(String username, java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        try {
            logger.info("涓虹敤鎴风敓鎴愪护鐗? {}", username);
            
            return Jwts.builder()
                    .setId(UUID.randomUUID().toString())
                    .setSubject(username)
                    .claim("roles", authorities)
                    .claim("type", "ACCESS") // 鏄庣‘鏍囪瘑涓鸿闂护鐗?                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                    .setIssuer(jwtProperties.getIssuer())
                    .setAudience(jwtProperties.getAudience())
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("生成令牌失败: {}", e.getMessage());
            throw new RuntimeException("生成令牌失败", e);
        }
    }
    
    // 鐢熸垚鍒锋柊浠ょ墝锛堥噸杞芥柟娉曪紝鏀寔鐩存帴浣跨敤鐢ㄦ埛鍚嶏級
    public String generateRefreshToken(String username) {
        try {
            logger.info("涓虹敤鎴风敓鎴愬埛鏂颁护鐗? {}", username);
            
            return Jwts.builder()
                    .setId(UUID.randomUUID().toString())
                    .setSubject(username)
                    .claim("type", "REFRESH") // 鏄庣‘鏍囪瘑涓哄埛鏂颁护鐗?                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
                    .setIssuer(jwtProperties.getIssuer())
                    .setAudience(jwtProperties.getAudience())
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            logger.error("生成刷新令牌失败: {}", e.getMessage());
            throw new RuntimeException("生成刷新令牌失败", e);
        }
    }
    
    // 浠嶢uthorization头中提取令牌
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(jwtProperties.getTokenPrefix())) {
            return null;
        }
        return authHeader.substring(jwtProperties.getTokenPrefix().length());
    }
    
    // 鏃犲紓甯哥殑浠ょ墝楠岃瘉鏂规硶锛岃繑鍥濩laims鎴杗ull锛岄伩鍏嶄笉蹇呰鐨勫紓甯稿鐞
    public Claims validateTokenWithoutException(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.debug("浠ょ墝涓虹┖锛岄獙璇佸け璐");
            return null;
        }
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // 楠岃瘉浠ょ墝鏄惁杩囨湡
            if (claims.getExpiration().before(new Date())) {
                logger.debug("浠ょ墝宸茶繃鏈");
                return null;
            }
            
            // 验证签发者和受众
            String issuer = jwtProperties.getIssuer();
            String audience = jwtProperties.getAudience();
            
            if (issuer != null && !issuer.equals(claims.getIssuer())) {
                logger.debug("令牌签发者不匹配");
                return null;
            }
            
            if (audience != null && !audience.equals(claims.getAudience())) {
                logger.debug("浠ょ墝鍙椾紬涓嶅尮閰");
                return null;
            }
            
            logger.debug("令牌验证成功");
            return claims;
        } catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
            logger.debug("浠ょ墝绛惧悕鏃犳晥鎴栨牸寮忛敊璇? {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.debug("浠ょ墝宸茶繃鏈");
        } catch (UnsupportedJwtException e) {
            logger.debug("不支持的令牌类型: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.debug("令牌参数无效: {}", e.getMessage());
        } catch (Exception e) {
            logger.debug("浠ょ墝楠岃瘉杩囩▼涓彂鐢熸湭鐭ラ敊璇? {}", e.getMessage());
        }
        
        return null;
    }
    
    // 鑾峰彇浠ょ墝鍓╀綑鏈夋晥鏈燂紙姣锛
    public Long getTokenRemainingValidity(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            logger.warn("鑾峰彇浠ょ墝鍓╀綑鏈夋晥鏈熷け璐? {}", e.getMessage());
            return -1L; // 杩斿洖璐熷€艰〃绀轰护鐗屾棤鏁堟垨宸茶繃鏈
        }
    }
    
    // 妫€鏌ヤ护鐗屾槸鍚﹀嵆灏嗚繃鏈燂紙榛樿5分钟内）
    public boolean isTokenExpiringSoon(String token, long thresholdMs) {
        Long remainingValidity = getTokenRemainingValidity(token);
        return remainingValidity > 0 && remainingValidity <= thresholdMs;
    }
    
    // 重载方法，使用默认阈值（5鍒嗛挓锛
    public boolean isTokenExpiringSoon(String token) {
        return isTokenExpiringSoon(token, 5 * 60 * 1000);
    }
    
    /**
     * 浠庡鍚堢敤鎴峰悕涓彁鍙栫敤鎴稩D
     */
    public String getUserIdFromUsername(String compositeUsername) {
        try {
            String[] parts = compositeUsername.split(":");
            if (parts.length >= 1) {
                return parts[0];
            }
        } catch (Exception e) {
            logger.error("瑙ｆ瀽鐢ㄦ埛ID失败: {}", compositeUsername, e);
        }
        return null;
    }
    
    /**
     * 浠庡鍚堢敤鎴峰悕涓彁鍙栫敤鎴风被鍨?     */
    public String getUserTypeFromUsername(String compositeUsername) {
        try {
            String[] parts = compositeUsername.split(":");
            if (parts.length >= 2) {
                return parts[1];
            }
        } catch (Exception e) {
            logger.error("瑙ｆ瀽鐢ㄦ埛绫诲瀷澶辫触: {}", compositeUsername, e);
        }
        return "USER";
    }
}


