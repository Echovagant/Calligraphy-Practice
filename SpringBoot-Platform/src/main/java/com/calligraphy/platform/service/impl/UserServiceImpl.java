package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.FrontendUserDto;
import com.calligraphy.platform.dto.FrontendUserRegistrationDto;
import com.calligraphy.platform.dto.FrontendUserUpdateDto;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.exception.UserAlreadyExistsException;
import com.calligraphy.platform.repository.CommentRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.repository.WorkRepository;
import com.calligraphy.platform.service.PracticeRecordService;
import com.calligraphy.platform.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /** 密码复杂度规则：与 RegisterRequestDto.password 上的 @Pattern 一致。 */
    private static final Pattern PASSWORD_COMPLEXITY =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$");
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorkRepository workRepository;
    private final CommentRepository commentRepository;
    private final PracticeRecordService practiceRecordService;
    /**
     * 用户身份的短 TTL 缓存 —— 改密码 / 改账号状态后要把旧身份打掉，
     * 否则旧令牌还能在 TTL 内继续通行。
     */
    private final com.calligraphy.platform.security.UserDetailsCache userDetailsCache;
    @Override
    @Transactional
    public User createUser(User user) {
        log.info("创建新用户: {}", user.getUsername());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("用户名已存在: " + user.getUsername());
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("邮箱已存在: " + user.getEmail());
        }

        // 设置默认头像
        if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl("/assets/default-avatar.png");
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("用户创建成功: {} (ID: {})", savedUser.getUsername(), savedUser.getId());

        return savedUser;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public User updateUser(String userId, FrontendUserUpdateDto updateRequest) {
        log.info("更新用户信息: {} (ID: {})", updateRequest.getNickname(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        // 更新基本信息
        if (updateRequest.getNickname() != null) {
            user.setNickname(updateRequest.getNickname());
        }

        if (updateRequest.getEmail() != null) {
            // 检查邮箱是否已被其他用户使用
            if (!user.getEmail().equals(updateRequest.getEmail()) &&
                    userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new UserAlreadyExistsException("邮箱已被使用: " + updateRequest.getEmail());
            }
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }

        if (updateRequest.getAvatar() != null) {
            user.setAvatarUrl(updateRequest.getAvatar());
        }

        if (updateRequest.getBio() != null) {
            user.setBio(updateRequest.getBio());
        }

        if (updateRequest.getStatus() != null) {
            try {
                user.setStatus(User.UserStatus.valueOf(updateRequest.getStatus().toUpperCase()));
                // 账号状态变了（被禁用 / 被锁定），缓存里的旧身份必须立刻作废
                userDetailsCache.invalidate(user.getUsername());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的用户状态: " + updateRequest.getStatus());
            }
        }

        User updatedUser = userRepository.save(user);
        log.info("用户信息更新成功: {} (ID: {})", updatedUser.getUsername(), updatedUser.getId());

        return updatedUser;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public void deleteUser(String userId) {
        log.info("删除用户 (ID: {})", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        userRepository.delete(user);
        log.info("用户删除成功: {} (ID: {})", user.getUsername(), userId);
    }
    @Override
    @SuppressWarnings("null")
    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));
    }
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + email));
    }
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    @Override
    public List<FrontendUserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToFrontendUserDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<FrontendUserDto> getUsers( Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToFrontendUserDto);
    }
    @Override
    public Page<FrontendUserDto> searchUsers(String keyword,  Pageable pageable) {
        List<User> list = userRepository.searchUsers(keyword);
        List<FrontendUserDto> dtos = list.stream().map(this::convertToFrontendUserDto).collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, dtos.size());
    }

    private FrontendUserDto convertToFrontendUserDto(User user) {
        return FrontendUserDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatarUrl())
                .phone(user.getPhone())
                .bio(user.getBio())
                .preferences(user.getPreferences())
                .status(user.getStatus())
                .verified(user.isEmailVerified())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(new java.util.ArrayList<>(user.getRoles()))
                .build();
    }
    @Override
    @Transactional
    public void updateLastLoginInfo(String username, String ipAddress) {
        log.info("更新用户最后登录信息: {} (IP: {})", username, ipAddress);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("用户最后登录信息更新成功: {}", username);
    }
    @Override
    @Transactional
    public void updateEmailVerificationStatus(String email, boolean verified) {
        log.info("更新邮箱验证状态: {} -> {}", email, verified);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + email));

        user.setEmailVerified(verified);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("邮箱验证状态更新成功: {}", email);
    }
    @Override
    @Transactional
    public void updateTwoFactorStatus(String username, boolean enabled) {
        log.info("更新双因素认证状态: {} -> {}", username, enabled);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        user.setTwoFactorEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("双因素认证状态更新成功: {}", username);
    }
    @Override
    public Map<String, Long> getUserStatistics() {
        log.info("获取用户统计信息");

        Map<String, Long> statistics = new HashMap<>();
        statistics.put("totalUsers", userRepository.count());
        statistics.put("activeUsers", userRepository.countByStatus(User.UserStatus.ACTIVE));
        statistics.put("inactiveUsers", userRepository.countByStatus(User.UserStatus.INACTIVE));
        statistics.put("verifiedUsers", userRepository.count() - userRepository.countUnverifiedUsers());
        statistics.put("adminUsers",
                userRepository.countByRole(User.Role.ADMIN) + userRepository.countByRole(User.Role.SUPER_ADMIN));

        // 今日新增用户
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        statistics.put("todayNewUsers", userRepository.countNewUsersSince(startOfDay));

        log.info("用户统计信息获取完成");
        return statistics;
    }
    @Override
    public List<User> getUsersByRole(String role) {
        log.info("获取角色为 {} 的用户列表", role);
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<User> users = userRepository.findByRole(userRole);
            log.info("获取到 {} 个用户", users.size());
            return users;
        } catch (IllegalArgumentException e) {
            log.error("无效的角色: {}", role);
            return new ArrayList<>();
        }
    }
    @Override
    public long getTotalUserCount() {
        return userRepository.count();
    }
    @Override
    public long getActiveUserCount() {
        return userRepository.countByStatus(User.UserStatus.ACTIVE);
    }
    @Override
    public long getVerifiedUserCount() {
        return userRepository.count() - userRepository.countUnverifiedUsers();
    }

    @Override
    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        // 改密码要走与注册同一套复杂度规则 —— 注册时拦住了，
        // 改密码却能设一个弱密码，等于留了个绕过口。
        if (!isStrongEnough(newPassword)) {
            throw new BusinessException("新密码强度不足：至少 6 位，且必须同时包含大写字母、小写字母和数字");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        // 旧身份（含旧密码哈希）不能继续留在缓存里
        userDetailsCache.invalidate(user.getUsername());
        log.info("用户密码已修改: {}", user.getUsername());
    }

    /**
     * 密码复杂度判定，与 {@code RegisterRequestDto.password} 上的规则一致：
     * 至少 6 位，且同时包含小写字母、大写字母与数字。
     */
    private boolean isStrongEnough(String password) {
        return password != null
                && password.length() >= 6
                && PASSWORD_COMPLEXITY.matcher(password).matches();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getUserCounts(String userId) {
        // 全部走聚合查询：以前是把该用户的全部作品（含 TEXT content 列）和
        // 全部评论查出来在内存里求和 / size()，复杂度是 O(作品数 + 评论数)；
        // 现在是 3 条固定返回 1 行的 SQL，复杂度 O(1)。
        List<Object[]> workCountRows = workRepository.sumWorkCounts(userId);
        Object[] workCounts = workCountRows.isEmpty() ? new Object[0] : workCountRows.get(0);
        Map<String, Long> counts = new HashMap<>();
        counts.put("workCount", asLong(workCounts, 0));
        counts.put("commentCount", commentRepository.countCommentsByUserId(userId));
        counts.put("practiceCount", practiceRecordService.countByUserId(userId));
        counts.put("totalLikesReceived", asLong(workCounts, 1));
        counts.put("totalViewsReceived", asLong(workCounts, 2));
        return counts;
    }

    /** 取聚合查询结果里的第 index 个数字；空值一律当 0。 */
    private long asLong(Object[] values, int index) {
        if (values == null || values.length <= index || values[index] == null) {
            return 0L;
        }
        return ((Number) values[index]).longValue();
    }
}


