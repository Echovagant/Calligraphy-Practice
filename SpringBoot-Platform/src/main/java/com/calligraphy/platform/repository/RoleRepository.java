package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

       Optional<Role> findByCode(String code);

       Optional<Role> findByName(String name);

       boolean existsByCode(String code);

       boolean existsByName(String name);

       @Query("SELECT r FROM Role r WHERE r.roleType = :type")
       List<Role> findByType(@Param("type") Role.RoleType type);

       @Query("SELECT r FROM Role r WHERE r.code LIKE 'SYSTEM_%'")
       List<Role> findSystemRoles();

       @Query("SELECT r FROM Role r WHERE r.isActive = true")
       List<Role> findActiveRoles();

       @Query("SELECT r FROM Role r WHERE r.isActive = true AND r.roleType = :type")
       List<Role> findActiveRolesByType(@Param("type") Role.RoleType type);

       @Query("SELECT r FROM Role r WHERE r.code = 'FRONTEND_USER'")
       List<Role> findDefaultRoles();

       // 鎼滅储鐩稿叧鏌ヨ
       @Query("SELECT r FROM Role r WHERE " +
                     "LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       List<Role> searchRoles(@Param("keyword") String keyword);

       @Query("SELECT r FROM Role r WHERE r.code LIKE %:code%")
       List<Role> findByCodeContaining(@Param("code") String code);

       @Query("SELECT r FROM Role r WHERE r.name LIKE %:name%")
       List<Role> findByNameContaining(@Param("name") String name);

       // 缁熻鐩稿叧鏌ヨ
       @Query("SELECT COUNT(r) FROM Role r WHERE r.roleType = :type")
       long countByType(@Param("type") Role.RoleType type);

       @Query("SELECT COUNT(r) FROM Role r WHERE r.isSystem = true")
       long countSystemRoles();

       @Query("SELECT COUNT(r) FROM Role r WHERE r.isActive = true")
       long countActiveRoles();

       @Query("SELECT COUNT(r) FROM Role r WHERE r.code = 'FRONTEND_USER'")
       long countDefaultRoles();

       // 瑙掕壊绫诲瀷缁熻
       @Query("SELECT r.roleType, COUNT(r) FROM Role r GROUP BY r.roleType")
       List<Object[]> getTypeStats();

       @Query("SELECT r.roleType, COUNT(r) FROM Role r WHERE r.isActive = true GROUP BY r.roleType")
       List<Object[]> getActiveTypeStats();

       @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
       List<Role> findByPermissionId(@Param("permissionId") Long permissionId);

       @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.code = :permissionCode")
       List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);

       @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.code IN :permissionCodes")
       List<Role> findByPermissionCodes(@Param("permissionCodes") List<String> permissionCodes);

       // 鏉冮檺妫€鏌
       @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r " +
                     "JOIN r.permissions p WHERE r.code = :roleCode AND p.code = :permissionCode")
       boolean hasPermission(@Param("roleCode") String roleCode, @Param("permissionCode") String permissionCode);

       // 鎺掑簭鏌ヨ
       @Query("SELECT r FROM Role r ORDER BY r.code")
       List<Role> findAllOrderByCode();

       @Query("SELECT r FROM Role r ORDER BY r.roleType, r.code")
       List<Role> findAllOrderByTypeAndCode();

       @Query("SELECT r FROM Role r ORDER BY r.name")
       List<Role> findAllOrderByName();

       // 棰勫畾涔夎鑹叉煡璇
       @Query("SELECT r FROM Role r WHERE r.code IN :codes")
       List<Role> findByCodes(@Param("codes") List<String> codes);

       // 鎵归噺鎿嶄綔妫€鏌
       @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.code LIKE 'SYSTEM_%' AND r.code IN :codes")
       boolean existsSystemRoles(@Param("codes") List<String> codes);
}


