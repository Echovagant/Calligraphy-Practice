package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.FrontendUserDto;
import com.calligraphy.platform.dto.FrontendUserUpdateDto;
import com.calligraphy.platform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;

public interface UserService {

    User createUser(User user);

    User updateUser(String userId, FrontendUserUpdateDto updateRequest);

    void deleteUser(String userId);

    User findById(String userId);

    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // DTO鐩稿叧鏂规硶
    List<FrontendUserDto> getAllUsers();

    Page<FrontendUserDto> getUsers(Pageable pageable);

    Page<FrontendUserDto> searchUsers(String keyword, Pageable pageable);

    // 鐢ㄦ埛鐘舵€佺鐞?    void updateLastLoginInfo(String username, String ipAddress);

    void updateEmailVerificationStatus(String email, boolean verified);

    void updateTwoFactorStatus(String username, boolean enabled);

    // 鐢ㄦ埛缁熻
    Map<String, Long> getUserStatistics();

    List<User> getUsersByRole(String role);

    long getTotalUserCount();

    long getActiveUserCount();

    long getVerifiedUserCount();
}


