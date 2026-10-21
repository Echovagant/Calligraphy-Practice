package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.AdminLoginRequestDto;
import com.calligraphy.platform.dto.AuthUserDto;
import com.calligraphy.platform.dto.LoginResponseDto;
import com.calligraphy.platform.dto.LogoutRequestDto;
import com.calligraphy.platform.dto.RefreshTokenRequestDto;
import com.calligraphy.platform.exception.UnauthorizedException;
import com.calligraphy.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理员认证接口，对应契约的 backendAuthLogin / backendAuthProfile /
 * backendAuthRefresh / backendAuthLogout 四个 operation。
 *
 * <p>管理员与前端用户同在 users 表，靠 role ∈ {ADMIN, SUPER_ADMIN} 区分，
 * 令牌以 userType=ADMIN 生成。
 */
@RestController
@RequestMapping("/api/backend/auth")
@RequiredArgsConstructor
public class BackendAuthController {

    private final AuthService authService;

    /**
     * POST /api/backend/auth/login —— 200 LoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequestDto loginRequest) {
        LoginResponseDto loginResponse = authService.loginAsAdmin(loginRequest);
        AuthUserDto user = authService.getCurrentUserProfile(loginResponse.getUsername());
        Map<String, Object> body = tokenResponse(
                loginResponse.getAccessToken(),
                loginResponse.getRefreshToken(),
                loginResponse.getTokenType(),
                loginResponse.getExpiresIn());
        body.put("user", user);
        return ResponseEntity.ok(body);
    }

    /**
     * GET /api/backend/auth/profile —— 200 AuthUser（裸资源）
     */
    @GetMapping("/profile")
    public ResponseEntity<AuthUserDto> profile() {
        AuthUserDto user = authService.getCurrentUserProfile(currentUsername());
        if (!isAdminRole(user.getRole())) {
            throw new AccessDeniedException("该账号不是管理员，无法访问后台接口");
        }
        return ResponseEntity.ok(user);
    }

    /**
     * POST /api/backend/auth/refresh —— 200 TokenPair
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequestDto refreshRequest) {
        LoginResponseDto loginResponse = authService.refreshTokenAsAdmin(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(tokenResponse(
                loginResponse.getAccessToken(),
                loginResponse.getRefreshToken(),
                loginResponse.getTokenType(),
                loginResponse.getExpiresIn()));
    }

    /**
     * POST /api/backend/auth/logout —— 204 无响应体
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequestDto logoutRequest,
                                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(
                extractBearerToken(authorization),
                logoutRequest == null ? null : logoutRequest.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    /** 令牌对（accessToken / refreshToken / tokenType / expiresIn）。 */
    private Map<String, Object> tokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accessToken", accessToken);
        body.put("refreshToken", refreshToken);
        body.put("tokenType", tokenType);
        body.put("expiresIn", expiresIn);
        return body;
    }

    /** 当前登录用户名（JwtAuthenticationFilter 放入的 principal 是 UserDetails）。 */
    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new UnauthorizedException("未带令牌或令牌无效");
        }
        return authentication.getName();
    }

    /** 从 Authorization 头里取 Bearer 令牌。 */
    private String extractBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        return null;
    }

    /** 管理员判定：role ∈ {ADMIN, SUPER_ADMIN}。 */
    private boolean isAdminRole(String role) {
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }
}
