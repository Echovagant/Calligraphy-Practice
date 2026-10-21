package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.PermissionDto;
import com.calligraphy.platform.dto.PermissionCreateDto;
import com.calligraphy.platform.dto.PermissionUpdateDto;
import com.calligraphy.platform.model.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PermissionService {
    
    PermissionDto createPermission(PermissionCreateDto createDto);
    
    PermissionDto getPermissionById(String permissionId);
    
    Optional<Permission> getPermissionEntityById(String permissionId);
    
    Optional<Permission> getPermissionByCode(String permissionCode);
    
    Page<PermissionDto> getAllPermissions(Pageable pageable);
    
    List<PermissionDto> getAllPermissions();
    
    Page<PermissionDto> getPermissionsByType(Permission.PermissionType type, Pageable pageable);
    
    List<PermissionDto> getPermissionsByType(Permission.PermissionType type);
    
    Page<PermissionDto> getPermissionsByModule(String module, Pageable pageable);
    
    List<PermissionDto> getPermissionsByModule(String module);
    
    Page<PermissionDto> getPermissionsByCategory(String category, Pageable pageable);
    
    List<PermissionDto> getPermissionsByCategory(String category);
    
    PermissionDto updatePermission(String permissionId, PermissionUpdateDto updateDto);
    
    void deletePermission(String permissionId);
    
    boolean existsById(String permissionId);
    
    boolean existsByCode(String permissionCode);
    
    // 搜索和筛选
    Page<PermissionDto> searchPermissions(String keyword, Pageable pageable);
    
    List<PermissionDto> searchPermissions(String keyword);
    
    // 统计相关
    long getTotalPermissionCount();
    
    long getPermissionCountByType(Permission.PermissionType type);
    
    long getPermissionCountByModule(String module);
    
    long getPermissionCountByCategory(String category);
    
    // 角色权限管理
    List<PermissionDto> getPermissionsByRoleId(String roleId);
    
    List<PermissionDto> getPermissionsByRoleCode(String roleCode);
    
    void assignPermissionToRole(String roleId, String permissionId);
    
    void removePermissionFromRole(String roleId, String permissionId);
    
    void bulkAssignPermissionsToRole(String roleId, List<String> permissionIds);
    
    void bulkRemovePermissionsFromRole(String roleId, List<String> permissionIds);
    
    boolean hasPermission(String roleId, String permissionCode);
    
    boolean hasAnyPermission(String roleId, List<String> permissionCodes);
    
    boolean hasAllPermissions(String roleId, List<String> permissionCodes);
    
    // 用户权限管理
    List<PermissionDto> getPermissionsByUserId(String userId);
    
    List<PermissionDto> getPermissionsByUsername(String username);
    
    boolean userHasPermission(String userId, String permissionCode);
    
    boolean userHasAnyPermission(String userId, List<String> permissionCodes);
    
    boolean userHasAllPermissions(String userId, List<String> permissionCodes);
    
    // 权限检查
    void checkPermission(String userId, String permissionCode);
    
    void checkAnyPermission(String userId, List<String> permissionCodes);
    
    void checkAllPermissions(String userId, List<String> permissionCodes);
    
    // 权限分组
    java.util.Map<String, List<PermissionDto>> getPermissionsGroupedByModule();
    
    java.util.Map<String, List<PermissionDto>> getPermissionsGroupedByCategory();
    
    java.util.Map<Permission.PermissionType, List<PermissionDto>> getPermissionsGroupedByType();
    
    // 预定义权限
    List<PermissionDto> getPredefinedPermissions();
    
    void initializePredefinedPermissions();
    
    // 权限验证
    boolean validatePermissionCode(String permissionCode);
    
    boolean validatePermissionName(String permissionName);
    
    // 权限依赖
    List<PermissionDto> getDependentPermissions(String permissionId);
    
    List<PermissionDto> getPrerequisitePermissions(String permissionId);
    
    void addPermissionDependency(String permissionId, String dependentPermissionId);
    
    void removePermissionDependency(String permissionId, String dependentPermissionId);
    
    // 权限缓存
    void refreshPermissionCache();
    
    void refreshUserPermissionCache(String userId);
    
    void refreshRolePermissionCache(String roleId);
    
    // 权限审计
    java.util.Map<String, Object> getPermissionUsageStats();
    
    java.util.Map<String, Object> getUserPermissionStats(String userId);
    
    java.util.Map<String, Object> getRolePermissionStats(String roleId);
    
    // 权限导出
    byte[] exportPermissions(String format);
    
    byte[] exportRolePermissions(String roleId, String format);
    
    byte[] exportUserPermissions(String userId, String format);
}


