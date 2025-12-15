package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.RoleDto;
import com.calligraphy.platform.dto.RoleCreateDto;
import com.calligraphy.platform.dto.RoleUpdateDto;
import com.calligraphy.platform.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    
    RoleDto createRole(RoleCreateDto createDto);
    
    RoleDto getRoleById(String roleId);
    
    Optional<Role> getRoleEntityById(String roleId);
    
    Optional<Role> getRoleByCode(String roleCode);
    
    Optional<Role> getRoleByName(String roleName);
    
    Page<RoleDto> getAllRoles(Pageable pageable);
    
    List<RoleDto> getAllRoles();
    
    Page<RoleDto> getRolesByType(Role.RoleType type, Pageable pageable);
    
    List<RoleDto> getRolesByType(Role.RoleType type);
    
    Page<RoleDto> getRolesByLevel(Integer level, Pageable pageable);
    
    List<RoleDto> getRolesByLevel(Integer level);
    
    RoleDto updateRole(String roleId, RoleUpdateDto updateDto);
    
    void deleteRole(String roleId);
    
    boolean existsById(String roleId);
    
    boolean existsByCode(String roleCode);
    
    boolean existsByName(String roleName);
    
    // 鎼滅储鍜岀瓫閫?    Page<RoleDto> searchRoles(String keyword, Pageable pageable);
    
    List<RoleDto> searchRoles(String keyword);
    
    // 缁熻鐩稿叧
    long getTotalRoleCount();
    
    long getRoleCountByType(Role.RoleType type);
    
    long getRoleCountByLevel(Integer level);
    
    // 鐢ㄦ埛瑙掕壊绠＄悊
    List<RoleDto> getRolesByUserId(String userId);
    
    List<RoleDto> getRolesByUsername(String username);
    
    void assignRoleToUser(String userId, String roleId);
    
    void removeRoleFromUser(String userId, String roleId);
    
    void bulkAssignRolesToUser(String userId, List<String> roleIds);
    
    void bulkRemoveRolesFromUser(String userId, List<String> roleIds);
    
    boolean userHasRole(String userId, String roleCode);
    
    boolean userHasAnyRole(String userId, List<String> roleCodes);
    
    boolean userHasAllRoles(String userId, List<String> roleCodes);
    
    // 瑙掕壊鏉冮檺绠＄悊
    List<RoleDto> getRolesByPermissionId(String permissionId);
    
    List<RoleDto> getRolesByPermissionCode(String permissionCode);
    
    void assignPermissionToRole(String roleId, String permissionId);
    
    void removePermissionFromRole(String roleId, String permissionId);
    
    void bulkAssignPermissionsToRole(String roleId, List<String> permissionIds);
    
    void bulkRemovePermissionsFromRole(String roleId, List<String> permissionIds);
    
    boolean roleHasPermission(String roleId, String permissionCode);
    
    boolean roleHasAnyPermission(String roleId, List<String> permissionCodes);
    
    boolean roleHasAllPermissions(String roleId, List<String> permissionCodes);
    
    // 瑙掕壊灞傜骇绠＄悊
    List<RoleDto> getChildRoles(String roleId);
    
    List<RoleDto> getParentRoles(String roleId);
    
    void addRoleHierarchy(String parentRoleId, String childRoleId);
    
    void removeRoleHierarchy(String parentRoleId, String childRoleId);
    
    boolean isRoleAncestor(String ancestorRoleId, String descendantRoleId);
    
    boolean isRoleDescendant(String descendantRoleId, String ancestorRoleId);
    
    // 棰勫畾涔夎鑹?    List<RoleDto> getPredefinedRoles();
    
    void initializePredefinedRoles();
    
    RoleDto getSuperAdminRole();
    
    RoleDto getAdminRole();
    
    RoleDto getUserRole();
    
    RoleDto getGuestRole();
    
    // 瑙掕壊楠岃瘉
    boolean validateRoleCode(String roleCode);
    
    boolean validateRoleName(String roleName);
    
    // 瑙掕壊缂撳瓨
    void refreshRoleCache();
    
    void refreshUserRoleCache(String userId);
    
    void refreshPermissionRoleCache(String permissionId);
    
    // 瑙掕壊瀹¤
    java.util.Map<String, Object> getRoleUsageStats();
    
    java.util.Map<String, Object> getUserRoleStats(String userId);
    
    java.util.Map<String, Object> getPermissionRoleStats(String permissionId);
    
    // 瑙掕壊瀵煎嚭
    byte[] exportRoles(String format);
    
    byte[] exportUserRoles(String userId, String format);
    
    byte[] exportRolePermissions(String roleId, String format);
}


