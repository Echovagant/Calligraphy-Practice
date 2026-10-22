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
    /** 用户身份的短 TTL 缓存 —— 改密码后要把旧身份打掉。 */
    private final com.calligraphy.platform.security.UserDetailsCache userDetailsCache;
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminRegisterResponseDto registerAdmin(AdminRegisterRequestDto registerRequest) {
        log.info("注册管理员账户: {}", registerRequest.getUsername());

        // 检查用户名是否已存在
        if (adminUserRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AdminUserAlreadyExistsException("管理员用户名已存在: " + registerRequest.getUsername());
        }

        // 检查邮箱是否已存在
        if (adminUserRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AdminUserAlreadyExistsException("管理员邮箱已存在: " + registerRequest.getEmail());
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

        // 生成JWT令牌
        String token = jwtService.generateAdminToken(savedAdmin);
        String refreshToken = jwtService.generateAdminRefreshToken(savedAdmin);

        log.info("管理员账户注册成功: ID={}", savedAdmin.getId());

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
        log.info("管理员登录: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            AdminUser adminUser = adminUserRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("管理员用户不存在"));

            // 检查账户状态
            if (!adminUser.getIsActive()) {
                throw new SecurityException("管理员账户已被禁用");
            }

            // 更新最后登录时间
            adminUser.setLastLogin(LocalDateTime.now());
            adminUserRepository.save(adminUser);

            // 生成JWT令牌
            String token = jwtService.generateAdminToken(adminUser);
            String refreshToken = jwtService.generateAdminRefreshToken(adminUser);

            log.info("管理员登录成功: ID={}", adminUser.getId());

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
            log.error("管理员登录失败: {}", e.getMessage());
            throw new SecurityException("管理员登录失败: " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public AdminLoginResponseDto refreshAdminToken(String refreshToken) {
        log.info("刷新管理员令牌");

        if (!jwtService.validateAdminToken(refreshToken)) {
            throw new SecurityException("无效的刷新令牌");
        }

        String username = jwtService.getUsernameFromAdminToken(refreshToken);
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("管理员用户不存在"));

        // 检查账户状态
        if (!adminUser.getIsActive()) {
            throw new SecurityException("管理员账户已被禁用");
        }

        // 生成新的访问令牌
        String newToken = jwtService.generateAdminToken(adminUser);
        String newRefreshToken = jwtService.generateAdminRefreshToken(adminUser);

        // 更新最后登录时间
        adminUser.setLastLogin(LocalDateTime.now());
        adminUserRepository.save(adminUser);

        log.info("管理员令牌刷新成功: ID={}", adminUser.getId());

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
        log.info("管理员登出");

        if (jwtService.validateAdminToken(token)) {
            // 将令牌加入黑名单
            jwtService.revokeAdminToken(token);
            log.info("管理员令牌已撤销");
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
                .orElseThrow(() -> new ResourceNotFoundException("管理员用户不存在"));
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminUser updateAdminProfile(Long adminId, String nickname, String email) {
        log.info("更新管理员资料: ID={}", adminId);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("管理员用户不存在"));

        if (nickname != null && !nickname.trim().isEmpty()) {
            adminUser.setNickname(nickname.trim());
        }

        if (email != null && !email.trim().isEmpty()) {
            String trimmedEmail = email.trim();
            // 检查邮箱是否已被其他管理员使用
            if (!adminUser.getEmail().equals(trimmedEmail) && adminUserRepository.existsByEmail(trimmedEmail)) {
                throw new AdminUserAlreadyExistsException("邮箱已被其他管理员使用");
            }
            adminUser.setEmail(trimmedEmail);
        }

        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("管理员资料更新成功: ID={}", adminId);

        return updatedAdmin;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public void updateAdminPassword(Long adminId, String oldPassword, String newPassword) {
        log.info("更新管理员密码: ID={}", adminId);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("管理员用户不存在"));

        // 验证当前密码
        if (!passwordEncoder.matches(oldPassword, adminUser.getPassword())) {
            throw new IllegalArgumentException("当前密码错误");
        }

        // 验证新密码强度
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("新密码长度至少为8位");
        }

        // 检查新密码是否与当前密码相同
        if (passwordEncoder.matches(newPassword, adminUser.getPassword())) {
            throw new IllegalArgumentException("新密码不能与当前密码相同");
        }

        // 更新密码
        adminUser.setPassword(passwordEncoder.encode(newPassword));
        adminUser.setUpdatedAt(LocalDateTime.now());
        adminUserRepository.save(adminUser);

        // 旧身份（含旧密码哈希）不能继续留在缓存里
        userDetailsCache.invalidate(adminUser.getUsername());

        log.info("管理员密码更新成功: ID={}", adminId);
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminUser updateAdminRole(Long adminId, String role) {
        log.info("更新管理员角色: ID={}, role={}", adminId, role);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("管理员用户不存在"));

        // 验证角色值
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("角色不能为空");
        }

        String trimmedRole = role.trim();
        adminUser.setRoles(trimmedRole);
        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("管理员角色更新成功: ID={}, role={}", adminId, trimmedRole);

        return updatedAdmin;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminUser updateAdminPermissions(Long adminId, Map<String, Boolean> permissions) {
        log.info("更新管理员权限: ID={}, permissions={}", adminId, permissions);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("管理员用户不存在"));

        // 验证权限值
        if (permissions != null) {
            adminUser.setPermissions(permissions);
        } else {
            adminUser.setPermissions(new HashMap<>());
        }
        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("管理员权限更新成功: ID={}, permissions={}", adminId, permissions);

        return updatedAdmin;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public AdminUser toggleAdminStatus(Long adminId) {
        log.info("切换管理员状态: ID={}", adminId);

        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("管理员用户不存在"));

        Boolean currentStatus = adminUser.getIsActive();
        Boolean newStatus = (currentStatus == null) ? true : !currentStatus;
        adminUser.setIsActive(newStatus);
        adminUser.setUpdatedAt(LocalDateTime.now());

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("管理员状态切换成功: ID={}, 新状态={}", adminId, newStatus);

        return updatedAdmin;
    }

}


