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

    // DTO相关方法
    List<FrontendUserDto> getAllUsers();

    Page<FrontendUserDto> getUsers(Pageable pageable);

    Page<FrontendUserDto> searchUsers(String keyword, Pageable pageable);

    // 用户状态管理
    void updateLastLoginInfo(String username, String ipAddress);

    void updateEmailVerificationStatus(String email, boolean verified);

    void updateTwoFactorStatus(String username, boolean enabled);

    // 用户统计
    Map<String, Long> getUserStatistics();

    List<User> getUsersByRole(String role);

    long getTotalUserCount();

    long getActiveUserCount();

    long getVerifiedUserCount();

    /**
     * 修改当前用户的密码。
     *
     * <p>校验旧密码必须与库里的哈希匹配；新密码不能与旧密码相同。
     *
     * @param userId 用户 id
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @throws com.calligraphy.platform.exception.BusinessException 旧密码不对或新密码与旧密码相同
     */
    void changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 用户的公开统计：作品数、评论数、练习数、累计获赞与累计浏览。
     *
     * @param userId 用户 id
     * @return 键为 {@code workCount}/{@code commentCount}/{@code practiceCount}/
     *         {@code totalLikesReceived}/{@code totalViewsReceived} 的映射
     */
    Map<String, Long> getUserCounts(String userId);
}


