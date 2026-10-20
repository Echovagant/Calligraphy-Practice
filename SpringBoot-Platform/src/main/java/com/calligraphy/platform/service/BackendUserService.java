package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.BackendUserDto;
import com.calligraphy.platform.dto.BackendUserRegistrationDto;
import com.calligraphy.platform.dto.BackendUserUpdateDto;
import com.calligraphy.platform.model.BackendUser;
import com.calligraphy.platform.model.BackendUser.BackendUserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface BackendUserService {
    
    BackendUser registerAdmin(BackendUserRegistrationDto registrationDto);
    
    BackendUserDto getUserById(String userId);
    
    Optional<BackendUser> getUserEntityById(String userId);
    
    Optional<BackendUser> getUserByUsername(String username);
    
    Optional<BackendUser> getUserByEmail(String email);
    
    Optional<BackendUser> getUserByUsernameOrEmail(String identifier);
    
    Page<BackendUserDto> getAllUsers(Pageable pageable);
    
    List<BackendUserDto> getAllUsers();
    
    List<BackendUserDto> getUsersByRole(BackendUserRole role);
    
    List<BackendUserDto> getUsersByDepartment(String department);
    
    BackendUserDto updateUser(String userId, BackendUserUpdateDto updateDto);
    
    BackendUserDto updateUserAvatar(String userId, MultipartFile avatarFile);
    
    BackendUserDto updateUserProfile(String userId, BackendUserUpdateDto updateDto);
    
    void deleteUser(String userId);
    
    void deactivateUser(String userId);
    
    void activateUser(String userId);
    
    boolean isUsernameAvailable(String username);
    
    boolean isEmailAvailable(String email);
    
    boolean existsById(String userId);
    
    long getTotalUserCount();
    
    long getActiveUserCount();
    
    long getAdminCount();
    
    long getSuperAdminCount();
    
    List<BackendUserDto> searchUsers(String keyword);
    
    Page<BackendUserDto> searchUsers(String keyword, Pageable pageable);
    
    // 绠＄悊鍛樼粺璁＄浉鍏虫柟娉
    long countByRole(BackendUserRole role);
    
    long countByDepartment(String department);
    
    long countByRegistrationDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    long countByLastLoginDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    List<BackendUserDto> findTopActiveAdmins(int limit);
    
    List<BackendUserDto> findRecentlyRegisteredAdmins(int limit);
    
    // 绠＄悊鍛樼姸鎬佺鐞
    void lockUser(String userId);
    
    void unlockUser(String userId);
    
    boolean isUserLocked(String userId);
    
    void incrementFailedLoginAttempts(String userId);
    
    void resetFailedLoginAttempts(String userId);
    
    // 绠＄悊鍛樻潈闄愮鐞
    BackendUserDto assignRole(String userId, BackendUserRole role);
    
    BackendUserDto removeRole(String userId, BackendUserRole role);
    
    BackendUserDto updateDepartment(String userId, String department);
    
    BackendUserDto updatePosition(String userId, String position);
    
    // 绯荤粺绠＄悊鍛樻搷浣
    void initializeSuperAdmin();
    
    boolean hasSuperAdmin();
    
    List<BackendUserDto> getSystemAdministrators();
    
    // 安全相关
    void updateLastLogin(String userId, String ipAddress, String userAgent);
    
    void updatePassword(String userId, String newPassword);
    
    boolean checkPassword(String userId, String password);
    
    // 批量操作
    void bulkDeleteUsers(List<String> userIds);
    
    void bulkDeactivateUsers(List<String> userIds);
    
    void bulkActivateUsers(List<String> userIds);
    
    // 閮ㄩ棬绠＄悊
    List<String> getAllDepartments();
    
    List<BackendUserDto> getUsersByDepartmentAndRole(String department, BackendUserRole role);
}


