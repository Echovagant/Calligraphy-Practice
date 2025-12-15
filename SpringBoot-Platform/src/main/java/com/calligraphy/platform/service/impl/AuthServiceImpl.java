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
        log.info("鐢ㄦ埛鐧诲綍灏濊瘯: {}", username);
        
        try {
            // 楠岃瘉鐢ㄦ埛鍑嵁
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    username,
                    password
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 鑾峰彇鐢ㄦ埛淇℃伅
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("鐢ㄦ埛鏈壘鍒? " + username);
            }
            
            // 妫€鏌ョ敤鎴风姸鎬?            if (!user.isEnabled()) {
                throw new AuthenticationException("鐢ㄦ埛璐︽埛宸茶绂佺敤");
            }
            
            // 鐢熸垚token
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // 鏇存柊鐢ㄦ埛鐧诲綍淇℃伅
            user.onSuccessfulLogin();
            userService.updateLastLoginInfo(user.getUsername(), "unknown");
            
            // 璁板綍鐧诲綍娲诲姩鏃ュ織
            log.info("鐢ㄦ埛鐧诲綍鎴愬姛: {} (ID: {})\n", user.getUsername(), user.getId());
            
            // 杩斿洖Map褰㈠紡鐨勫搷搴?            Map<String, String> result = new HashMap<>();
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", String.valueOf(jwtService.getJwtExpiration()));
            result.put("username", user.getUsername());
            result.put("userId", user.getId());
            
            return result;
            
        } catch (Exception e) {
            log.error("鐢ㄦ埛鐧诲綍澶辫触: {}", e.getMessage());
            throw new AuthenticationException("鐢ㄦ埛鍚嶆垨瀵嗙爜閿欒");
        }
    }
    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        log.info("鐢ㄦ埛鐧诲綍灏濊瘯: {}", loginRequest.getUsername());
        
        try {
            // 楠岃瘉鐢ㄦ埛鍑嵁
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 鑾峰彇鐢ㄦ埛淇℃伅
            User user = userService.findByUsername(loginRequest.getUsername());
            if (user == null) {
                throw new UserNotFoundException("鐢ㄦ埛鏈壘鍒? " + loginRequest.getUsername());
            }
            
            // 妫€鏌ョ敤鎴风姸鎬?            if (!user.isEnabled()) {
                throw new AuthenticationException("鐢ㄦ埛璐︽埛宸茶绂佺敤");
            }
            
            // 鐢熸垚token
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("deviceInfo", loginRequest.getDeviceInfo());
            extraClaims.put("ipAddress", loginRequest.getIpAddress());
            
            // 鑾峰彇鐢ㄦ埛瑙掕壊鍜屾潈闄?            List<String> roles = List.of("ROLE_" + user.getRole().name());
            List<String> permissions = List.of();
            
            extraClaims.put("roles", roles);
            extraClaims.put("permissions", permissions);
            
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            // 鏇存柊鐢ㄦ埛鐧诲綍淇℃伅
            user.setLastLoginIp(loginRequest.getIpAddress());
            user.onSuccessfulLogin();
            // 浣跨敤updateLastLoginInfo鏂规硶鏇存柊鐧诲綍淇℃伅锛岃€屼笉鏄痷pdateUser
            userService.updateLastLoginInfo(user.getUsername(), loginRequest.getIpAddress());
            
            // 璁板綍鐧诲綍娲诲姩鏃ュ織
            log.info("鐢ㄦ埛鐧诲綍鎴愬姛: {} (ID: {})", user.getUsername(), user.getId());
            
            // 鏋勫缓鍝嶅簲
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
            log.error("鐢ㄦ埛鐧诲綍澶辫触: {}", e.getMessage());
            throw new AuthenticationException("鐢ㄦ埛鍚嶆垨瀵嗙爜閿欒");
        }
    }
    @Override
    @Transactional
    public Map<String, String> refreshTokenMap(String refreshToken) {
        log.info("鍒锋柊璁块棶浠ょ墝锛圡ap褰㈠紡锛?);
        
        try {
            if (!jwtService.validateToken(refreshToken)) {
                throw new AuthenticationException("鍒锋柊浠ょ墝鏃犳晥鎴栧凡杩囨湡");
            }
            
            String username = jwtService.extractUsername(refreshToken);
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("鐢ㄦ埛鏈壘鍒? " + username);
            }
            
            if (!user.isEnabled()) {
                throw new AuthenticationException("鐢ㄦ埛璐︽埛宸茶绂佺敤");
            }
            
            // 鐢熸垚鏂扮殑璁块棶浠ょ墝
            String newAccessToken = jwtService.generateToken(user);
            
            // 鏇存柊鐢ㄦ埛鐧诲綍淇℃伅
            user.setLastLoginAt(LocalDateTime.now());
            userService.updateLastLoginInfo(user.getUsername(), "");
            
            log.info("璁块棶浠ょ墝鍒锋柊鎴愬姛: {}", username);
            
            // 杩斿洖Map褰㈠紡鐨勫搷搴?            Map<String, String> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", refreshToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", String.valueOf(jwtService.getJwtExpiration()));
            result.put("username", user.getUsername());
            result.put("userId", user.getId());
            
            return result;
            
        } catch (Exception e) {
            log.error("鍒锋柊璁块棶浠ょ墝澶辫触: {}", e.getMessage());
            throw new AuthenticationException("鍒锋柊浠ょ墝澶辫触");
        }
    }
    @Override
    @Transactional
    public LoginResponseDto refreshToken(String refreshToken) {
        log.info("鍒锋柊璁块棶浠ょ墝");
        
        try {
            if (!jwtService.validateToken(refreshToken)) {
                throw new AuthenticationException("鍒锋柊浠ょ墝鏃犳晥鎴栧凡杩囨湡");
            }
            
            String username = jwtService.extractUsername(refreshToken);
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("鐢ㄦ埛鏈壘鍒? " + username);
            }
            
            if (!user.isEnabled()) {
                throw new AuthenticationException("鐢ㄦ埛璐︽埛宸茶绂佺敤");
            }
            
            // 鑾峰彇鐢ㄦ埛瑙掕壊鍜屾潈闄?            List<String> roles = List.of("ROLE_" + user.getRole().name());
            List<String> permissions = List.of();
            
            // 鐢熸垚鏂扮殑璁块棶浠ょ墝
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("roles", roles);
            extraClaims.put("permissions", permissions);
            
            String newAccessToken = jwtService.generateToken(user);
            
            // 鏇存柊鐢ㄦ埛鐧诲綍淇℃伅
            user.setLastLoginAt(LocalDateTime.now());
            userService.updateLastLoginInfo(user.getUsername(), "");
            
            log.info("璁块棶浠ょ墝鍒锋柊鎴愬姛: {}", username);
            
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
            log.error("鍒锋柊璁块棶浠ょ墝澶辫触: {}", e.getMessage());
            throw new AuthenticationException("鍒锋柊浠ょ墝澶辫触");
        }
    }
    @Override
    @Transactional
    public User register(User user) {
        log.info("鐢ㄦ埛娉ㄥ唽灏濊瘯: {}", user.getUsername());
        
        // 妫€鏌ョ敤鎴峰悕鏄惁宸插瓨鍦?        if (userService.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("鐢ㄦ埛鍚嶅凡瀛樺湪: " + user.getUsername());
        }
        
        // 妫€鏌ラ偖绠辨槸鍚﹀凡瀛樺湪
        if (userService.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("閭宸插瓨鍦? " + user.getEmail());
        }
        
        // 瀵嗙爜鍔犲瘑
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 淇濆瓨鐢ㄦ埛
        User savedUser = userService.createUser(user);
        log.info("鐢ㄦ埛娉ㄥ唽鎴愬姛: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        
        return savedUser;
    }
    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto registerRequest) {
        log.info("鐢ㄦ埛娉ㄥ唽灏濊瘯: {}", registerRequest.getUsername());
        
        // 妫€鏌ョ敤鎴峰悕鏄惁宸插瓨鍦?        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("鐢ㄦ埛鍚嶅凡瀛樺湪: " + registerRequest.getUsername());
        }
        
        // 妫€鏌ラ偖绠辨槸鍚﹀凡瀛樺湪
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("閭宸插瓨鍦? " + registerRequest.getEmail());
        }
        
        try {
            // 鍒涘缓鏂扮敤鎴?            User newUser = User.builder()
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
            
            // 淇濆瓨鐢ㄦ埛
            User savedUser = userService.createUser(newUser);
            
            // 鐢熸垚璁块棶token
            String accessToken = jwtService.generateToken(savedUser);
            String refreshToken = jwtService.generateRefreshToken(savedUser);
            
            // 璁板綍娉ㄥ唽娲诲姩鏃ュ織
            log.info("鐢ㄦ埛娉ㄥ唽鎴愬姛: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
            
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
                .message("娉ㄥ唽鎴愬姛锛岃妫€鏌ユ偍鐨勯偖绠辫繘琛岄獙璇?)
                .createdAt(savedUser.getCreatedAt())
                .build();
                
        } catch (Exception e) {
            log.error("鐢ㄦ埛娉ㄥ唽澶辫触: {}", e.getMessage());
            throw new AuthenticationException("娉ㄥ唽澶辫触锛岃绋嶅悗閲嶈瘯");
        }
    }
    @Override
    @Transactional
    public void logout(String token) {
        log.info("鐢ㄦ埛鐧诲嚭");
        
        try {
            if (jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);
                jwtService.revokeToken(token);
                log.info("鐢ㄦ埛鐧诲嚭鎴愬姛: {}", username);
            }
        } catch (Exception e) {
            log.error("鐢ㄦ埛鐧诲嚭澶辫触: {}", e.getMessage());
        }
    }
    @Override
    @Transactional
    public void logout(String token, String refreshToken) {
        log.info("鐢ㄦ埛鐧诲嚭锛屽悓鏃跺鐞嗚闂护鐗屽拰鍒锋柊浠ょ墝");
        
        try {
            // 澶勭悊璁块棶浠ょ墝
            if (token != null && !token.isEmpty()) {
                if (jwtService.validateToken(token)) {
                    String username = jwtService.extractUsername(token);
                    jwtService.revokeToken(token);
                    log.info("璁块棶浠ょ墝宸插け鏁? {}", username);
                }
            }
            
            // 澶勭悊鍒锋柊浠ょ墝
            if (refreshToken != null && !refreshToken.isEmpty()) {
                if (jwtService.validateToken(refreshToken)) {
                    String username = jwtService.extractUsername(refreshToken);
                    jwtService.revokeToken(refreshToken);
                    log.info("鍒锋柊浠ょ墝宸插け鏁? {}", username);
                }
            }
        } catch (Exception e) {
            log.error("鐢ㄦ埛鐧诲嚭澶辫触: {}", e.getMessage());
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


