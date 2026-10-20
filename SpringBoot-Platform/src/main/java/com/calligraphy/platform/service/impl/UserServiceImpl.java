package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.FrontendUserDto;
import com.calligraphy.platform.dto.FrontendUserRegistrationDto;
import com.calligraphy.platform.dto.FrontendUserUpdateDto;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.exception.UserAlreadyExistsException;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.service.FrontendUserService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public User createUser(User user) {
        log.info("鍒涘缓鏂扮敤鎴? {}", user.getUsername());

        // 妫€鏌ョ敤鎴峰悕鏄惁宸插瓨鍦
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("用户名已存在: " + user.getUsername());
        }

        // 妫€鏌ラ偖绠辨槸鍚﹀凡瀛樺湪
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("閭宸插瓨鍦? " + user.getEmail());
        }

        // 璁剧疆榛樿头像
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
        log.info("鏇存柊鐢ㄦ埛淇℃伅: {} (ID: {})", updateRequest.getNickname(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + userId));

        // 鏇存柊鍩烘湰淇℃伅
        if (updateRequest.getNickname() != null) {
            user.setNickname(updateRequest.getNickname());
        }

        if (updateRequest.getEmail() != null) {
            // 妫€鏌ラ偖绠辨槸鍚﹀凡琚叾浠栫敤鎴蜂娇鐢
            if (!user.getEmail().equals(updateRequest.getEmail()) &&
                    userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new UserAlreadyExistsException("閭宸茶使用: " + updateRequest.getEmail());
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
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("鏃犳晥鐨勭敤鎴风姸鎬? " + updateRequest.getStatus());
            }
        }

        User updatedUser = userRepository.save(user);
        log.info("鐢ㄦ埛淇℃伅鏇存柊鎴愬姛: {} (ID: {})", updatedUser.getUsername(), updatedUser.getId());

        return updatedUser;
    }
    @Override
    @Transactional
    @SuppressWarnings("null")
    public void deleteUser(String userId) {
        log.info("删除用户 (ID: {})", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + userId));

        userRepository.delete(user);
        log.info("用户删除成功: {} (ID: {})", user.getUsername(), userId);
    }
    @Override
    @SuppressWarnings("null")
    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + userId));
    }
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + username));
    }
    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("鐢ㄦ埛涓嶅瓨鍦? " + email));
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
        log.info("鏇存柊鐢ㄦ埛鏈€鍚庣櫥褰曚俊鎭? {} (IP: {})", username, ipAddress);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + username));

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("鐢ㄦ埛鏈€鍚庣櫥褰曚俊鎭洿鏂版垚鍔? {}", username);
    }
    @Override
    @Transactional
    public void updateEmailVerificationStatus(String email, boolean verified) {
        log.info("鏇存柊閭楠岃瘉鐘舵€? {} -> {}", email, verified);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + email));

        user.setEmailVerified(verified);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("閭楠岃瘉鐘舵€佹洿鏂版垚鍔? {}", email);
    }
    @Override
    @Transactional
    public void updateTwoFactorStatus(String username, boolean enabled) {
        log.info("鏇存柊鍙屽洜绱犺璇佺姸鎬? {} -> {}", username, enabled);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("鐢ㄦ埛涓嶅瓨鍦? " + username));

        user.setTwoFactorEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("鍙屽洜绱犺璇佺姸鎬佹洿鏂版垚鍔? {}", username);
    }
    @Override
    public Map<String, Long> getUserStatistics() {
        log.info("鑾峰彇鐢ㄦ埛缁熻淇℃伅");

        Map<String, Long> statistics = new HashMap<>();
        statistics.put("totalUsers", userRepository.count());
        statistics.put("activeUsers", userRepository.countByStatus(User.UserStatus.ACTIVE));
        statistics.put("inactiveUsers", userRepository.countByStatus(User.UserStatus.INACTIVE));
        statistics.put("verifiedUsers", userRepository.count() - userRepository.countUnverifiedUsers());
        statistics.put("adminUsers",
                userRepository.countByRole(User.Role.ADMIN) + userRepository.countByRole(User.Role.SUPER_ADMIN));

        // 浠婃棩鏂板用户
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        statistics.put("todayNewUsers", userRepository.countNewUsersSince(startOfDay));

        log.info("鐢ㄦ埛缁熻淇℃伅鑾峰彇瀹屾垚");
        return statistics;
    }
    @Override
    public List<User> getUsersByRole(String role) {
        log.info("鑾峰彇瑙掕壊涓?{} 鐨勭敤鎴峰垪琛", role);
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            List<User> users = userRepository.findByRole(userRole);
            log.info("鑾峰彇鍒?{} 涓敤鎴", users.size());
            return users;
        } catch (IllegalArgumentException e) {
            log.error("鏃犳晥鐨勮鑹? {}", role);
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
}


