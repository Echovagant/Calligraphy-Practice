package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.service.AuthService;
import com.calligraphy.platform.security.JwtService;
import com.calligraphy.platform.service.UserService;
import com.calligraphy.platform.dto.FrontendUserDto;
import com.calligraphy.platform.dto.FrontendUserUpdateDto;
import com.calligraphy.platform.dto.LoginRequestDto;
import com.calligraphy.platform.dto.LoginResponseDto;
import com.calligraphy.platform.dto.RegisterRequestDto;
import com.calligraphy.platform.dto.RegisterResponseDto;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.exception.AuthenticationException;
import com.calligraphy.platform.exception.UserAlreadyExistsException;
import com.calligraphy.platform.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Override
    @Transactional
    public Map<String, String> login(String username, String password) {
        log.info("用户登录尝试: {}", username);
        
        try {
            // 验证用户凭据
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    username,
                    password
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("用户未找到: " + username);
            }
            
            // 检查用户状态
            if (!user.isEnabled()) {
                throw new AuthenticationException("用户账户已被禁用");
            }
            
            // 生成token
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // 更新用户登录信息
            user.onSuccessfulLogin();
            userService.updateLastLoginInfo(user.getUsername(), "unknown");
            
            // 记录登录活动日志
            log.info("用户登录成功: {} (ID: {})\n", user.getUsername(), user.getId());
            
            // 返回Map形式的响应
            Map<String, String> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", String.valueOf(jwtService.getJwtExpiration()));
            result.put("username", user.getUsername());
            result.put("userId", user.getId());
            
            return result;
            
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            throw new AuthenticationException("用户名或密码错误");
        }
    }
    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("用户登录尝试: {}", loginRequest.getUsername());
        
        try {
            // 验证用户凭据
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = userService.findByUsername(loginRequest.getUsername());
            if (user == null) {
                throw new UserNotFoundException("用户未找到: " + loginRequest.getUsername());
            }
            
            // 检查用户状态
            if (!user.isEnabled()) {
                throw new AuthenticationException("用户账户已被禁用");
            }
            
            // 生成token
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("deviceInfo", loginRequest.getDeviceInfo());
            extraClaims.put("ipAddress", loginRequest.getIpAddress());
            
            // 获取用户角色和权限
            List<String> roles = List.of("ROLE_" + user.getRole().name());
            List<String> permissions = List.of();
            
            extraClaims.put("roles", roles);
            extraClaims.put("permissions", permissions);
            
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // 更新用户登录信息
            user.setLastLoginIp(loginRequest.getIpAddress());
            user.onSuccessfulLogin();
            // 使用updateLastLoginInfo方法更新登录信息，而不是updateUser
            userService.updateLastLoginInfo(user.getUsername(), loginRequest.getIpAddress());
            
            // 记录登录活动日志
            log.info("用户登录成功: {} (ID: {})", user.getUsername(), user.getId());
            
            // 构建响应
            return LoginResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatarUrl())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .roles(roles)
                .permissions(permissions)
                .userInfo(LoginResponseDto.UserInfoDto.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .avatar(user.getAvatarUrl())
                    .phone(user.getPhone())
                    .bio(user.getBio())
                    .createdAt(user.getCreatedAt())
                    .isVerified(user.isEmailVerified())
                    .isTwoFactorEnabled(user.isTwoFactorEnabled())
                    .build())
                .build();
                
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            throw new AuthenticationException("用户名或密码错误");
        }
    }
    @Override
    @Transactional
    public Map<String, String> refreshTokenMap(String refreshToken) {
        log.info("刷新访问令牌（Map形式）");
        
        try {
            if (!jwtService.validateToken(refreshToken)) {
                throw new AuthenticationException("刷新令牌无效或已过期");
            }
            
            String username = jwtService.extractUsername(refreshToken);
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("用户未找到: " + username);
            }
            
            if (!user.isEnabled()) {
                throw new AuthenticationException("用户账户已被禁用");
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtService.generateToken(user);
            
            // 更新用户登录信息
            user.setLastLoginAt(LocalDateTime.now());
            userService.updateLastLoginInfo(user.getUsername(), "");
            
            log.info("访问令牌刷新成功: {}", username);
            
            // 返回Map形式的响应
            Map<String, String> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", String.valueOf(jwtService.getJwtExpiration()));
            result.put("username", user.getUsername());
            result.put("userId", user.getId());
            
            return result;
            
        } catch (Exception e) {
            log.error("刷新访问令牌失败: {}", e.getMessage());
            throw new AuthenticationException("刷新令牌失败");
        }
    }
    @Override
    @Transactional
    public LoginResponseDto refreshToken(String refreshToken) {
        log.info("刷新访问令牌");
        
        try {
            if (!jwtService.validateToken(refreshToken)) {
                throw new AuthenticationException("刷新令牌无效或已过期");
            }
            
            String username = jwtService.extractUsername(refreshToken);
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("用户未找到: " + username);
            }
            
            if (!user.isEnabled()) {
                throw new AuthenticationException("用户账户已被禁用");
            }
            
            // 获取用户角色和权限
            List<String> roles = List.of("ROLE_" + user.getRole().name());
            List<String> permissions = List.of();
            
            // 生成新的访问令牌
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("roles", roles);
            extraClaims.put("permissions", permissions);
            
            String newAccessToken = jwtService.generateToken(user);
            
            // 更新用户登录信息
            user.setLastLoginAt(LocalDateTime.now());
            userService.updateLastLoginInfo(user.getUsername(), "");
            
            log.info("访问令牌刷新成功: {}", username);
            
            return LoginResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatarUrl())
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .roles(roles)
                .permissions(permissions)
                .userInfo(LoginResponseDto.UserInfoDto.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .avatar(user.getAvatarUrl())
                    .phone(user.getPhone())
                    .bio(user.getBio())
                    .createdAt(user.getCreatedAt())
                    .isVerified(user.isEmailVerified())
                    .isTwoFactorEnabled(user.isTwoFactorEnabled())
                    .build())
                .build();
                
        } catch (Exception e) {
            log.error("刷新访问令牌失败: {}", e.getMessage());
            throw new AuthenticationException("刷新令牌失败");
        }
    }
    @Override
    @Transactional
    public User register(User user) {
        log.info("用户注册尝试: {}", user.getUsername());
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("用户名已存在: " + user.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (userService.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("邮箱已存在: " + user.getEmail());
        }
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 保存用户
        User savedUser = userService.createUser(user);
        log.info("用户注册成功: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        
        return savedUser;
    }
    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto registerRequest) {
        log.info("用户注册尝试: {}", registerRequest.getUsername());
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("用户名已存在: " + registerRequest.getUsername());
        }
        
        // 检查邮箱是否已存在
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("邮箱已存在: " + registerRequest.getEmail());
        }
        
        try {
            // 创建新用户
            User newUser = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nickname(registerRequest.getNickname())
                .phone(registerRequest.getPhone())
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .lastLoginIp(registerRequest.getIpAddress())
                .build();
            
            // 保存用户
            User savedUser = userService.createUser(newUser);
            
            // 生成访问token
            String accessToken = jwtService.generateToken(savedUser);
            String refreshToken = jwtService.generateRefreshToken(savedUser);
            
            // 记录注册活动日志
            log.info("用户注册成功: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
            
            return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .nickname(savedUser.getNickname())
                .email(savedUser.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .verificationRequired(true)
                .emailSent(true)
                .message("注册成功，请检查您的邮箱进行验证")
                .createdAt(savedUser.getCreatedAt())
                .build();
                
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            throw new AuthenticationException("注册失败，请稍后重试");
        }
    }
    @Override
    @Transactional
    public void logout(String token) {
        log.info("用户登出");
        
        try {
            if (jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);
                jwtService.revokeToken(token);
                log.info("用户登出成功: {}", username);
            }
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
        }
    }
    @Override
    @Transactional
    public void logout(String token, String refreshToken) {
        log.info("用户登出，同时处理访问令牌和刷新令牌");
        
        try {
            // 处理访问令牌
            if (token != null && !token.isEmpty()) {
                if (jwtService.validateToken(token)) {
                    String username = jwtService.extractUsername(token);
                    jwtService.revokeToken(token);
                    log.info("访问令牌已失效: {}", username);
                }
            }
            
            // 处理刷新令牌
            if (refreshToken != null && !refreshToken.isEmpty()) {
                if (jwtService.validateToken(refreshToken)) {
                    String username = jwtService.extractUsername(refreshToken);
                    jwtService.revokeToken(refreshToken);
                    log.info("刷新令牌已失效: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
        }
    }

    private LoginResponseDto.UserInfoDto convertToDto(User user) {
        return LoginResponseDto.UserInfoDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatarUrl())
                .phone(user.getPhone())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .isVerified(user.isEmailVerified())
                .isTwoFactorEnabled(user.isTwoFactorEnabled())
                .build();
    }
}


