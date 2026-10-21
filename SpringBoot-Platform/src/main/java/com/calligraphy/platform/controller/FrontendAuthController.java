package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.AuthUserDto;
import com.calligraphy.platform.dto.LoginRequestDto;
import com.calligraphy.platform.dto.LoginResponseDto;
import com.calligraphy.platform.dto.LogoutRequestDto;
import com.calligraphy.platform.dto.RefreshTokenRequestDto;
import com.calligraphy.platform.dto.RegisterRequestDto;
import com.calligraphy.platform.dto.RegisterResponseDto;
import com.calligraphy.platform.exception.UnauthorizedException;
import com.calligraphy.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * 前端用户认证接口，对应契约的 frontendAuthRegister / frontendAuthLogin /
 * frontendAuthProfile / frontendAuthRefresh / frontendAuthLogout 五个 operation。
 *
 * <p>成功响应一律是裸资源，不带 success / data / code 外壳。
 */
@RestController
@RequestMapping("/api/frontend/auth")
@RequiredArgsConstructor
public class FrontendAuthController {

    private final AuthService authService;

    /**
     * POST /api/frontend/auth/register —— 201 RegisterResponse
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        RegisterResponseDto registerResponse = authService.register(registerRequest);
        AuthUserDto user = authService.getCurrentUserProfile(registerRequest.getUsername());
        Map<String, Object> body = tokenResponse(
                registerResponse.getAccessToken(),
                registerResponse.getRefreshToken(),
                registerResponse.getTokenType(),
                registerResponse.getExpiresIn());
        body.put("user", user);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /**
     * POST /api/frontend/auth/login —— 200 LoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto loginResponse = authService.login(loginRequest);
        AuthUserDto user = authService.getCurrentUserProfile(loginRequest.getUsername());
        Map<String, Object> body = tokenResponse(
                loginResponse.getAccessToken(),
                loginResponse.getRefreshToken(),
                loginResponse.getTokenType(),
                loginResponse.getExpiresIn());
        body.put("user", user);
        return ResponseEntity.ok(body);
    }

    /**
     * GET /api/frontend/auth/profile —— 200 AuthUser（裸资源，不是 {user:...} 包装）
     */
    @GetMapping("/profile")
    public ResponseEntity<AuthUserDto> profile() {
        return ResponseEntity.ok(authService.getCurrentUserProfile(currentUsername()));
    }

    /**
     * POST /api/frontend/auth/refresh —— 200 TokenPair
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequestDto refreshRequest) {
        LoginResponseDto loginResponse = authService.refreshToken(refreshRequest.getRefreshToken());
        return ResponseEntity.ok(tokenResponse(
                loginResponse.getAccessToken(),
                loginResponse.getRefreshToken(),
                loginResponse.getTokenType(),
                loginResponse.getExpiresIn()));
    }

    /**
     * POST /api/frontend/auth/logout —— 204 无响应体
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
}
