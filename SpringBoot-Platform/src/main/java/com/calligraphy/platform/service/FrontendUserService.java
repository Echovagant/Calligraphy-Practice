package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.FrontendUserDto;
import com.calligraphy.platform.dto.FrontendUserRegistrationDto;
import com.calligraphy.platform.dto.FrontendUserUpdateDto;
import com.calligraphy.platform.model.FrontendUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface FrontendUserService {
    
    FrontendUser registerUser(FrontendUserRegistrationDto registrationDto);
    
    FrontendUserDto getUserById(String userId);
    
    Optional<FrontendUser> getUserEntityById(String userId);
    
    Optional<FrontendUser> getUserByUsername(String username);
    
    Optional<FrontendUser> getUserByEmail(String email);
    
    Optional<FrontendUser> getUserByUsernameOrEmail(String identifier);
    
    Page<FrontendUserDto> getAllUsers(Pageable pageable);
    
    List<FrontendUserDto> getAllUsers();
    
    FrontendUserDto updateUser(String userId, FrontendUserUpdateDto updateDto);
    
    FrontendUserDto updateUserAvatar(String userId, MultipartFile avatarFile);
    
    FrontendUserDto updateUserProfile(String userId, FrontendUserUpdateDto updateDto);
    
    void deleteUser(String userId);
    
    void deactivateUser(String userId);
    
    void activateUser(String userId);
    
    boolean isUsernameAvailable(String username);
    
    boolean isEmailAvailable(String email);
    
    boolean existsById(String userId);
    
    long getTotalUserCount();
    
    long getActiveUserCount();
    
    long getNewUserCountSinceDays(int days);
    
    List<FrontendUserDto> searchUsers(String keyword);
    
    Page<FrontendUserDto> searchUsers(String keyword, Pageable pageable);
    
    // 用户统计相关方法
    long countByRegistrationDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    long countByLastLoginDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    List<FrontendUserDto> findTopActiveUsers(int limit);
    
    List<FrontendUserDto> findRecentlyRegisteredUsers(int limit);
    
    // 用户状态管理
    void lockUser(String userId);
    
    void unlockUser(String userId);
    
    boolean isUserLocked(String userId);
    
    void incrementFailedLoginAttempts(String userId);
    
    void resetFailedLoginAttempts(String userId);
    
    // 用户偏好设置
    FrontendUserDto updateUserPreferences(String userId, String preferences);
    
    String getUserPreferences(String userId);
    
    // 用户练习统计
    long getUserPracticeCount(String userId);
    
    long getUserWorkCount(String userId);
    
    long getUserCommentCount(String userId);
    
    // 批量操作
    void bulkDeleteUsers(List<String> userIds);
    
    void bulkDeactivateUsers(List<String> userIds);
    
    void bulkActivateUsers(List<String> userIds);
}


