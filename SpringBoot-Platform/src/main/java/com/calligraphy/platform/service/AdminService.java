package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.AdminLoginRequestDto;
import com.calligraphy.platform.dto.AdminLoginResponseDto;
import com.calligraphy.platform.dto.AdminRegisterRequestDto;
import com.calligraphy.platform.dto.AdminRegisterResponseDto;
import com.calligraphy.platform.entity.AdminUser;

import java.util.Map;

public interface AdminService {

    AdminRegisterResponseDto registerAdmin(AdminRegisterRequestDto registerRequest);
    AdminLoginResponseDto loginAdmin(AdminLoginRequestDto loginRequest);
    AdminLoginResponseDto refreshAdminToken(String refreshToken);
    void logoutAdmin(String token);
    AdminUser getCurrentAdmin(String username);
    AdminUser updateAdminProfile(Long adminId, String nickname, String email);
    void updateAdminPassword(Long adminId, String currentPassword, String newPassword);
    AdminUser updateAdminRole(Long adminId, String role);
    AdminUser updateAdminPermissions(Long adminId, Map<String, Boolean> permissions);
    AdminUser toggleAdminStatus(Long adminId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}


