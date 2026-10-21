package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.service.AuthService;
import com.calligraphy.platform.service.JwtService;
import com.calligraphy.platform.service.UserService;
import com.calligraphy.platform.dto.AdminLoginRequestDto;
import com.calligraphy.platform.dto.AuthUserDto;
import com.calligraphy.platform.dto.FrontendUserDto;
import com.calligraphy.platform.dto.FrontendUserUpdateDto;
import com.calligraphy.platform.dto.LoginRequestDto;
import com.calligraphy.platform.dto.LoginResponseDto;
import com.calligraphy.platform.dto.RegisterRequestDto;
import com.calligraphy.platform.dto.RegisterResponseDto;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.exception.AuthenticationException;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.exception.UserAlreadyExistsException;
import com.calligraphy.platform.exception.UserNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    /** 管理员令牌的 userType；前端用户固定为 USER。 */
    private static final String ADMIN_USER_TYPE = "ADMIN";

    /** 验证码存储 key 前缀（Redis），值为 6 位验证码，TTL 10 分钟。 */
    private static final String VERIFICATION_CODE_PREFIX = "auth:verification:code:";

    /** 验证码发送频率限制 key 前缀（Redis），TTL 60 秒。 */
    private static final String VERIFICATION_LIMIT_PREFIX = "auth:verification:limit:";

    private static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(10);
    private static final Duration VERIFICATION_LIMIT_TTL = Duration.ofSeconds(60);

    /** SMTP 连不通时的连接/读写超时（毫秒），避免验证码接口被连接超时拖住。 */
    private static final String MAIL_TIMEOUT_MILLIS = "5000";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Override
    @Transactional
    public Map<String, String> login(String username, String password) {
        log.info("用户登录尝试: {}", username);

        // 锁定状态必须在 authenticate 之前判定：Spring 的
        // DaoAuthenticationProvider.retrieveUser 会把除 UsernameNotFoundException 以外的
        // 异常统一包成 InternalAuthenticationServiceException（消息保留、类型丢失），
        // LockedException 到不了业务层，契约声明的 423 就永远走不到。
        User user = findUserQuietly(username);
        if (user != null && !user.isAccountNonLocked()) {
            throw new LockedException("用户账户已被锁定，请稍后再试");
        }

        try {
            // 验证用户凭据
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    username,
                    password
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (user == null) {
                throw new UserNotFoundException("用户未找到: " + username);
            }

            // 检查用户状态
            if (!user.isEnabled()) {
                throw new AuthenticationException("用户账户已被禁用");
            }

            // 生成token
            String accessToken = jwtService.generateToken(user.getId(), "USER", user.getUsername());
            String refreshToken = jwtService.generateRefreshToken(user.getId(), "USER", user.getUsername());
            
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

        } catch (LockedException e) {
            // 锁定是明确状态 → 423 ACCOUNT_LOCKED，不要被兜底伪装成「用户名或密码错误」
            log.warn("用户账户被锁定: {}", username);
            throw e;
        } catch (Exception e) {
            // 兜底里把被 Spring 包装掉的锁定异常还原：
            // DaoAuthenticationProvider.retrieveUser 会把非 UsernameNotFoundException
            // 的异常包成 InternalAuthenticationServiceException，LockedException 只剩 cause。
            if (e.getCause() instanceof LockedException locked) {
                log.warn("用户账户被锁定: {}", username);
                throw locked;
            }
            log.error("用户登录失败: {}", e.getMessage());
            throw new AuthenticationException("用户名或密码错误");
        }
    }
    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("用户登录尝试: {}", loginRequest.getUsername());

        // 锁定状态必须在 authenticate 之前判定（原因见 login(String, String)）：
        // 让 Spring 先发现锁定，LockedException 会被包成 InternalAuthenticationServiceException，
        // 类型丢失后只能走 401，契约声明的 423 走不到。
        User user = findUserQuietly(loginRequest.getUsername());
        if (user != null && !user.isAccountNonLocked()) {
            throw new LockedException("用户账户已被锁定，请稍后再试");
        }

        try {
            // 验证用户凭据
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

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
            
            String accessToken = jwtService.generateToken(user.getId(), "USER", user.getUsername());
            String refreshToken = jwtService.generateRefreshToken(user.getId(), "USER", user.getUsername());
            
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

        } catch (LockedException e) {
            // 账号被锁定是明确状态 → 423 ACCOUNT_LOCKED，原样透出
            log.warn("用户账户被锁定: {}", loginRequest.getUsername());
            throw e;
        } catch (AuthenticationException e) {
            // 账户被禁用等明确状态，保留自己的消息
            log.warn("用户登录被拒绝: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 兜底里把被 Spring 包装掉的锁定异常还原（原因见 login(String, String)）
            if (e.getCause() instanceof LockedException locked) {
                log.warn("用户账户被锁定: {}", loginRequest.getUsername());
                throw locked;
            }
            // 其余情况（用户不存在、密码错误等）统一成同一句，
            // 不泄露账号是否存在
            log.error("用户登录失败: {}", e.getMessage());
            throw new AuthenticationException("用户名或密码错误");
        }
    }
    @Override
    @Transactional
    public Map<String, String> refreshTokenMap(String refreshToken) {
        log.info("刷新访问令牌（Map形式）");

        String username = null;
        try {
            if (!jwtService.validateToken(refreshToken)) {
                throw new AuthenticationException("刷新令牌无效或已过期");
            }

            username = jwtService.extractUsername(refreshToken);
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("用户未找到: " + username);
            }
            
            if (!user.isAccountNonLocked()) {
                throw new LockedException("用户账户已被锁定，请稍后再试");
            }
            if (!user.isEnabled()) {
                throw new AuthenticationException("用户账户已被禁用");
            }

            // 生成新的访问令牌
            String newAccessToken = jwtService.generateToken(user.getId(), "USER", user.getUsername());

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

        } catch (LockedException e) {
            // 账户被锁定 → 423 ACCOUNT_LOCKED
            log.warn("用户账户被锁定，拒绝刷新令牌: {}", username);
            throw e;
        } catch (Exception e) {
            log.error("刷新访问令牌失败: {}", e.getMessage());
            throw new AuthenticationException("刷新令牌失败");
        }
    }
    @Override
    @Transactional
    public LoginResponseDto refreshToken(String refreshToken) {
        log.info("刷新访问令牌");

        String username = null;
        try {
            if (!jwtService.validateToken(refreshToken)) {
                throw new AuthenticationException("刷新令牌无效或已过期");
            }

            username = jwtService.extractUsername(refreshToken);
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("用户未找到: " + username);
            }
            
            if (!user.isAccountNonLocked()) {
                throw new LockedException("用户账户已被锁定，请稍后再试");
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
            
            String newAccessToken = jwtService.generateToken(user.getId(), "USER", user.getUsername());
            
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

        } catch (LockedException e) {
            // 账户被锁定 → 423 ACCOUNT_LOCKED
            log.warn("用户账户被锁定，拒绝刷新令牌: {}", username);
            throw e;
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
        
        // 密码加密：createUser 内部统一做一次 BCrypt 加密，这里只传明文，不要重复编码

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
            // 密码加密：createUser 内部统一做一次 BCrypt 加密，这里只传明文，不要重复编码
            User newUser = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
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
            String accessToken = jwtService.generateToken(savedUser.getId(), "USER", savedUser.getUsername());
            String refreshToken = jwtService.generateRefreshToken(savedUser.getId(), "USER", savedUser.getUsername());
            
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
                jwtService.invalidateToken(token);
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
                    jwtService.invalidateToken(token);
                    log.info("访问令牌已失效: {}", username);
                }
            }
            
            // 处理刷新令牌
            if (refreshToken != null && !refreshToken.isEmpty()) {
                if (jwtService.validateToken(refreshToken)) {
                    String username = jwtService.extractUsername(refreshToken);
                    jwtService.invalidateToken(refreshToken);
                    log.info("刷新令牌已失效: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
        }
    }

    /**
     * 按用户名查询用户资料，组装成契约 AuthUser 形状。
     */
    @Override
    @Transactional(readOnly = true)
    public AuthUserDto getCurrentUserProfile(String username) {
        if (username == null || username.isBlank()) {
            throw new UserNotFoundException("用户未找到: " + username);
        }
        User user = findUserOrThrow(username);
        return AuthUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatarUrl())
                .role(user.getRole() == null ? null : user.getRole().name())
                .build();
    }
    /**
     * 管理员登录：凭据校验 + 角色校验，令牌以 userType=ADMIN 生成。
     */
    @Override
    @Transactional
    public LoginResponseDto loginAsAdmin(AdminLoginRequestDto loginRequest) {
        log.info("管理员登录尝试: {}", loginRequest.getUsername());

        // 锁定状态必须在 authenticate 之前判定（原因见 login(String, String)）
        User existing = findUserQuietly(loginRequest.getUsername());
        if (existing != null && !existing.isAccountNonLocked()) {
            throw new LockedException("用户账户已被锁定，请稍后再试");
        }

        User user;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            user = findUserOrThrow(loginRequest.getUsername());
        } catch (LockedException e) {
            // 锁定是明确状态 → 423 ACCOUNT_LOCKED（与前端登录一致）
            log.warn("管理员账户被锁定: {}", loginRequest.getUsername());
            throw e;
        } catch (Exception e) {
            // 兜底里把被 Spring 包装掉的锁定异常还原（原因见 login(String, String)）
            if (e.getCause() instanceof LockedException locked) {
                log.warn("管理员账户被锁定: {}", loginRequest.getUsername());
                throw locked;
            }
            // 与前端登录保持一致：不区分「用户不存在」和「密码错误」，统一按认证失败处理。
            log.error("管理员登录失败: {}", e.getMessage());
            throw new AuthenticationException("用户名或密码错误");
        }

        if (!user.isEnabled()) {
            throw new AuthenticationException("用户账户已被禁用");
        }
        if (!isAdmin(user)) {
            throw new AccessDeniedException("该账号不是管理员，无法登录后台");
        }

        String accessToken = jwtService.generateToken(user.getId(), ADMIN_USER_TYPE, user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), ADMIN_USER_TYPE, user.getUsername());

        user.onSuccessfulLogin();
        userService.updateLastLoginInfo(user.getUsername(), "");

        log.info("管理员登录成功: {} (ID: {})", user.getUsername(), user.getId());
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
                .roles(List.of("ROLE_" + user.getRole().name()))
                .permissions(List.of())
                .build();
    }
    /**
     * 管理员刷新令牌：角色校验通过后生成 userType=ADMIN 的新令牌对。
     */
    @Override
    @Transactional
    public LoginResponseDto refreshTokenAsAdmin(String refreshToken) {
        log.info("管理员刷新访问令牌");

        if (refreshToken == null || refreshToken.isBlank() || !jwtService.validateToken(refreshToken)) {
            throw new AuthenticationException("刷新令牌无效或已过期");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = findUserOrThrow(username);
        if (!user.isAccountNonLocked()) {
            throw new LockedException("用户账户已被锁定，请稍后再试");
        }
        if (!user.isEnabled()) {
            throw new AuthenticationException("用户账户已被禁用");
        }
        if (!isAdmin(user)) {
            throw new AccessDeniedException("该账号不是管理员，无法刷新令牌");
        }

        String newAccessToken = jwtService.generateToken(user.getId(), ADMIN_USER_TYPE, user.getUsername());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId(), ADMIN_USER_TYPE, user.getUsername());

        userService.updateLastLoginInfo(user.getUsername(), "");

        log.info("管理员令牌刷新成功: {}", username);
        return LoginResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatarUrl())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .roles(List.of("ROLE_" + user.getRole().name()))
                .permissions(List.of())
                .build();
    }
    /**
     * 发送邮箱验证码。同一邮箱 60 秒内只允许申请一次；发信失败只记日志。
     *
     * @return false 表示触发限流（60 秒内重复请求）
     */
    @Override
    public boolean sendVerificationCode(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("邮箱不能为空");
        }
        String target = email.trim().toLowerCase(Locale.ROOT);

        // 60 秒内同一邮箱重复请求直接判定为限流
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(VERIFICATION_LIMIT_PREFIX + target, "1", VERIFICATION_LIMIT_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            log.warn("验证码请求过于频繁: {}", target);
            return false;
        }

        String code = generateVerificationCode();
        redisTemplate.opsForValue().set(VERIFICATION_CODE_PREFIX + target, code, VERIFICATION_CODE_TTL);
        sendVerificationCodeMail(target, code);
        return true;
    }

    /**
     * 查用户，找不到统一抛项目自己的 UserNotFoundException（契约 404）。
     */
    private User findUserOrThrow(String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("用户未找到: " + username);
            }
            return user;
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            throw new UserNotFoundException("用户未找到: " + username);
        }
    }

    /**
     * 查用户，找不到返回 null 而不抛异常。
     *
     * <p>登录的「锁定前置检查」要用它：未知账号必须继续走认证失败（401
     * 「用户名或密码错误」），不能因为查不到用户就变成 404 —— 那会泄露
     * 账号是否存在。
     */
    private User findUserQuietly(String username) {
        try {
            return userService.findByUsername(username);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return null;
        }
    }

    /** 管理员判定：role ∈ {ADMIN, SUPER_ADMIN}。 */
    private boolean isAdmin(User user) {
        return User.Role.ADMIN.equals(user.getRole()) || User.Role.SUPER_ADMIN.equals(user.getRole());
    }

    /** 生成 6 位数字验证码。 */
    private String generateVerificationCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /**
     * 发送验证码邮件。当前环境 SMTP 是占位配置，发送失败属于预期情况：
     * 只记日志，不向上抛，避免验证码接口返回 5xx。
     */
    private void sendVerificationCodeMail(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("邮箱验证码");
            message.setText("您的验证码是 " + code + "，10 分钟内有效。如非本人操作请忽略本邮件。");
            mailSender.send(message);
            log.info("验证码邮件发送成功");
        } catch (Exception e) {
            log.error("验证码邮件发送失败: {}", e.getMessage());
        }
    }

    /**
     * SMTP 不可达时若不设超时，验证码接口可能被连接超时拖住，这里统一加一个短超时。
     */
    @PostConstruct
    void configureMailTimeout() {
        if (mailSender instanceof JavaMailSenderImpl sender) {
            Properties properties = sender.getJavaMailProperties();
            properties.setProperty("mail.smtp.connectiontimeout", MAIL_TIMEOUT_MILLIS);
            properties.setProperty("mail.smtp.timeout", MAIL_TIMEOUT_MILLIS);
            properties.setProperty("mail.smtp.writetimeout", MAIL_TIMEOUT_MILLIS);
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


