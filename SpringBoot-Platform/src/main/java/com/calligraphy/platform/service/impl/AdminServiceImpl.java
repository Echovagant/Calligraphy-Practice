package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.AdminLoginRequestDto;
import com.calligraphy.platform.dto.AdminLoginResponseDto;
import com.calligraphy.platform.dto.AdminRegisterRequestDto;
import com.calligraphy.platform.dto.AdminRegisterResponseDto;
import com.calligraphy.platform.entity.AdminUser;
import com.calligraphy.platform.exception.AdminUserAlreadyExistsException;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.repository.AdminUserRepository;
import com.calligraphy.platform.service.JwtService;
import com.calligraphy.platform.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminRegisterResponseDto registerAdmin(AdminRegisterRequestDto registerRequest) {
        log.info("娉ㄥ唽绠＄悊鍛樿处鎴? {}", registerRequest.getUsername());

        // 妫€鏌ョ敤鎴峰悕鏄惁宸插瓨鍦?        if (adminUserRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AdminUserAlreadyExistsException("绠＄悊鍛樼敤鎴峰悕宸插瓨鍦? " + registerRequest.getUsername());
        }

        // 妫€鏌ラ偖绠辨槸鍚﹀凡瀛樺湪
        if (adminUserRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AdminUserAlreadyExistsException("绠＄悊鍛橀偖绠卞凡瀛樺湪: " + registerRequest.getEmail());
        }

        AdminUser adminUser = AdminUser.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nickname(registerRequest.getNickname())
                .roles("ROLE_ADMIN")
                .permissions(Map.of("all", true))
                .isActive(true)
                .isEmailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        AdminUser savedAdmin = adminUserRepository.save(adminUser);

        // 鐢熸垚JWT浠ょ墝
        String token = jwtService.generateAdminToken(savedAdmin);
        String refreshToken = jwtService.generateAdminRefreshToken(savedAdmin);

        log.info("绠＄悊鍛樿处鎴锋敞鍐屾垚鍔? ID={}", savedAdmin.getId());

        return AdminRegisterResponseDto.builder()
                .id(savedAdmin.getId())
                .username(savedAdmin.getUsername())
                .email(savedAdmin.getEmail())
                .nickname(savedAdmin.getNickname())
                .roles(savedAdmin.getRoles())
                .permissions(savedAdmin.getPermissions() != null ? savedAdmin.getPermissions().toString() : "")
                .isActive(savedAdmin.getIsActive())
                .isEmailVerified(false)
                .createdAt(savedAdmin.getCreatedAt())
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAdminJwtExpiration())
                .build();
    }
    @Override
    @Transactional
    public AdminLoginResponseDto loginAdmin(AdminLoginRequestDto loginRequest) {
        log.info("绠＄悊鍛樼櫥褰? {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            AdminUser adminUser = adminUserRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("绠＄悊鍛樼敤鎴蜂笉瀛樺湪"));

            // 妫€鏌ヨ处鎴风姸鎬?            if (!adminUser.getIsActive()) {
                throw new SecurityException("绠＄悊鍛樿处鎴峰凡琚鐢?);
            }

            // 鏇存柊鏈€鍚庣櫥褰曟椂闂?            adminUser.setLastLogin(LocalDateTime.now());
            adminUserRepository.save(adminUser);

            // 鐢熸垚JWT浠ょ墝
            String token = jwtService.generateAdminToken(adminUser);
            String refreshToken = jwtService.generateAdminRefreshToken(adminUser);

            log.info("绠＄悊鍛樼櫥褰曟垚鍔? ID={}", adminUser.getId());

            return AdminLoginResponseDto.builder()
                    .id(adminUser.getId())
                    .username(adminUser.getUsername())
                    .email(adminUser.getEmail())
                    .nickname(adminUser.getNickname())
                    .roles(adminUser.getRoles())
                    .permissions(adminUser.getRoles())
                    .isActive(adminUser.getIsActive())
                    .isEmailVerified(false)
                    .lastLoginAt(adminUser.getLastLogin())
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getAdminJwtExpiration())
                    .build();

        } catch (Exception e) {
            log.error("绠＄悊鍛樼櫥褰曞け璐? {}", e.getMessage());
            throw new SecurityException("绠＄悊鍛樼櫥褰曞け璐? " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public AdminLoginResponseDto refreshAdminToken(String refreshToken) {
        log.info("鍒锋柊绠＄悊鍛樹护鐗?);

        if (!jwtService.validateAdminToken(refreshToken)) {
            throw new SecurityException("鏃犳晥鐨勫埛鏂颁护鐗?);
        }

        String username = jwtService.getUsernameFromAdminToken(refreshToken);
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("绠＄悊鍛樼敤鎴蜂笉瀛樺湪"));

        // 妫€鏌ヨ处鎴风姸鎬?        if (!adminUser.getIsActive()) {
            throw new SecurityException("绠＄悊鍛樿处鎴峰凡琚鐢?);
        }

        // 鐢熸垚鏂扮殑璁块棶浠ょ墝
        String newToken = jwtService.generateAdminToken(adminUser);
        String newRefreshToken = jwtService.generateAdminRefreshToken(adminUser);

        // 鏇存柊鏈€鍚庣櫥褰曟椂闂?        adminUser.setLastLogin(LocalDateTime.now());
        adminUserRepository.save(adminUser);

        log.info("绠＄悊鍛樹护鐗屽埛鏂版垚鍔? ID={}", adminUser.getId());

        return AdminLoginResponseDto.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .email(adminUser.getEmail())
                .nickname(adminUser.getNickname())
                .roles(adminUser.getRoles())
                .permissions(adminUser.getRoles())
                .isActive(adminUser.getIsActive())
                .isEmailVerified(false)
                .lastLoginAt(adminUser.getLastLogin())
                .accessToken(newToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAdminJwtExpiration())
                .build();
    }
    @Override
    @Transactional
    public void logoutAdmin(String token) {
        log.info("绠＄悊鍛樼櫥鍑?);

        if (jwtService.validateAdminToken(token)) {
            // 灏嗕护鐗屽姞鍏ラ粦鍚嶅崟
            jwtService.revokeAdminToken(token);
            log.info("绠＄悊鍛樹护鐗屽凡鎾ら攢");
        }
    }
    @Override
    public boolean existsByUsername(String username) {
        return adminUserRepository.existsByUsername(username);
    }
    @Override
    public boolean existsByEmail(String email) {
        return adminUserRepository.existsByEmail(email);
    }
    @Override
    public AdminUser getCurrentAdmin(String username) {
        return adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("绠＄悊鍛樼敤鎴蜂笉瀛樺湪"));
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminUser updateAdminProfile(Long adminId, String nickname, String email) {
        log.info("鏇存柊绠＄悊鍛樿祫鏂? ID={}", adminId);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("绠＄悊鍛樼敤鎴蜂笉瀛樺湪"));

        if (nickname != null && !nickname.trim().isEmpty()) {
            adminUser.setNickname(nickname.trim());
        }

        if (email != null && !email.trim().isEmpty()) {
            String trimmedEmail = email.trim();
            // 妫€鏌ラ偖绠辨槸鍚﹀凡琚叾浠栫鐞嗗憳浣跨敤
            if (!adminUser.getEmail().equals(trimmedEmail) && adminUserRepository.existsByEmail(trimmedEmail)) {
                throw new AdminUserAlreadyExistsException("閭宸茶鍏朵粬绠＄悊鍛樹娇鐢?);
            }
            adminUser.setEmail(trimmedEmail);
        }

        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("绠＄悊鍛樿祫鏂欐洿鏂版垚鍔? ID={}", adminId);

        return updatedAdmin;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public void updateAdminPassword(Long adminId, String oldPassword, String newPassword) {
        log.info("鏇存柊绠＄悊鍛樺瘑鐮? ID={}", adminId);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("绠＄悊鍛樼敤鎴蜂笉瀛樺湪"));

        // 楠岃瘉褰撳墠瀵嗙爜
        if (!passwordEncoder.matches(oldPassword, adminUser.getPassword())) {
            throw new IllegalArgumentException("褰撳墠瀵嗙爜閿欒");
        }

        // 楠岃瘉鏂板瘑鐮佸己搴?        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("鏂板瘑鐮侀暱搴﹁嚦灏戜负8浣?);
        }

        // 妫€鏌ユ柊瀵嗙爜鏄惁涓庡綋鍓嶅瘑鐮佺浉鍚?        if (passwordEncoder.matches(newPassword, adminUser.getPassword())) {
            throw new IllegalArgumentException("鏂板瘑鐮佷笉鑳戒笌褰撳墠瀵嗙爜鐩稿悓");
        }

        // 鏇存柊瀵嗙爜
        adminUser.setPassword(passwordEncoder.encode(newPassword));
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(adminUser);

        log.info("绠＄悊鍛樺瘑鐮佹洿鏂版垚鍔? ID={}", adminId);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminUser updateAdminRole(Long adminId, String role) {
        log.info("鏇存柊绠＄悊鍛樿鑹? ID={}, role={}", adminId, role);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("绠＄悊鍛樼敤鎴蜂笉瀛樺湪"));

        // 楠岃瘉瑙掕壊鍊?        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("瑙掕壊涓嶈兘涓虹┖");
        }

        String trimmedRole = role.trim();
        adminUser.setRoles(trimmedRole);
        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("绠＄悊鍛樿鑹叉洿鏂版垚鍔? ID={}, role={}", adminId, trimmedRole);

        return updatedAdmin;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminUser updateAdminPermissions(Long adminId, Map<String, Boolean> permissions) {
        log.info("鏇存柊绠＄悊鍛樻潈闄? ID={}, permissions={}", adminId, permissions);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("绠＄悊鍛樼敤鎴蜂笉瀛樺湪"));

        // 楠岃瘉鏉冮檺鍊?        if (permissions != null) {
            adminUser.setPermissions(permissions);
        } else {
            adminUser.setPermissions(new HashMap<>());
        }
        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("绠＄悊鍛樻潈闄愭洿鏂版垚鍔? ID={}, permissions={}", adminId, permissions);

        return updatedAdmin;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminUser toggleAdminStatus(Long adminId) {
        log.info("鍒囨崲绠＄悊鍛樼姸鎬? ID={}", adminId);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("绠＄悊鍛樼敤鎴蜂笉瀛樺湪"));

        Boolean currentStatus = adminUser.getIsActive();
        Boolean newStatus = (currentStatus == null) ? true : !currentStatus;
        adminUser.setIsActive(newStatus);
        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("绠＄悊鍛樼姸鎬佸垏鎹㈡垚鍔? ID={}, 鏂扮姸鎬?{}", adminId, newStatus);

        return updatedAdmin;
    }

}


