package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

       Optional<Permission> findByCode(String code);

       boolean existsByCode(String code);

       @Query("SELECT p FROM Permission p WHERE p.name = :name")
       Optional<Permission> findByName(@Param("name") String name);

       @Query("SELECT p FROM Permission p WHERE p.permissionType = :type")
       List<Permission> findByType(@Param("type") Permission.PermissionType type);

       @Query("SELECT p FROM Permission p WHERE p.module = :module")
       List<Permission> findByModule(@Param("module") String module);

       @Query("SELECT p FROM Permission p WHERE p.permissionType = :type AND p.module = :module")
       List<Permission> findByTypeAndModule(@Param("type") Permission.PermissionType type,
                     @Param("module") String module);

       @Query("SELECT p FROM Permission p WHERE p.code LIKE 'SYSTEM_%'")
       List<Permission> findSystemPermissions();

       @Query("SELECT p FROM Permission p WHERE p.isActive = true")
       List<Permission> findActivePermissions();

       @Query("SELECT p FROM Permission p WHERE p.isActive = true AND p.permissionType = :type")
       List<Permission> findActivePermissionsByType(@Param("type") Permission.PermissionType type);

       @Query("SELECT p FROM Permission p WHERE p.isActive = true AND p.module = :module")
       List<Permission> findActivePermissionsByModule(@Param("module") String module);

       // 鎼滅储鐩稿叧鏌ヨ
       @Query("SELECT p FROM Permission p WHERE " +
                     "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       List<Permission> searchPermissions(@Param("keyword") String keyword);

       @Query("SELECT p FROM Permission p WHERE p.module LIKE %:module%")
       List<Permission> findByModuleContaining(@Param("module") String module);

       // 鏉冮檺浠ｇ爜鐩稿叧鏌ヨ
       @Query("SELECT p FROM Permission p WHERE p.code LIKE %:code%")
       List<Permission> findByCodeContaining(@Param("code") String code);

       @Query("SELECT p FROM Permission p WHERE p.code LIKE :prefix%")
       List<Permission> findByCodeStartingWith(@Param("prefix") String prefix);

       // 缁熻鐩稿叧鏌ヨ
       @Query("SELECT COUNT(p) FROM Permission p WHERE p.permissionType = :type")
       long countByType(@Param("type") Permission.PermissionType type);

       @Query("SELECT COUNT(p) FROM Permission p WHERE p.module = :module")
       long countByModule(@Param("module") String module);

       @Query("SELECT COUNT(p) FROM Permission p WHERE p.code LIKE 'SYSTEM_%'")
       long countSystemPermissions();

       @Query("SELECT COUNT(p) FROM Permission p WHERE p.isActive = true")
       long countActivePermissions();

       // 妯″潡缁熻
       @Query("SELECT p.module, COUNT(p) FROM Permission p GROUP BY p.module")
       List<Object[]> getModuleStats();

       @Query("SELECT p.module, COUNT(p) FROM Permission p WHERE p.isActive = true GROUP BY p.module")
       List<Object[]> getActiveModuleStats();

       // 鏉冮檺绫诲瀷缁熻
       @Query("SELECT p.permissionType, COUNT(p) FROM Permission p GROUP BY p.permissionType")
       List<Object[]> getTypeStats();

       @Query("SELECT p.permissionType, COUNT(p) FROM Permission p WHERE p.isActive = true GROUP BY p.permissionType")
       List<Object[]> getActiveTypeStats();

       // 棰勫畾涔夋潈闄愭煡璇
       @Query("SELECT p FROM Permission p WHERE p.code IN :codes")
       List<Permission> findByCodes(@Param("codes") List<String> codes);

       // 瑙掕壊鏉冮檺鐩稿叧鏌ヨ
       @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
       List<Permission> findByRoleId(@Param("roleId") Long roleId);

       @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.code = :roleCode")
       List<Permission> findByRoleCode(@Param("roleCode") String roleCode);

       @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r WHERE r.id IN :roleIds")
       List<Permission> findByRoleIds(@Param("roleIds") List<Long> roleIds);

       @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r WHERE r.code IN :roleCodes")
       List<Permission> findByRoleCodes(@Param("roleCodes") List<String> roleCodes);

       // 鏉冮檺妫€鏌
       @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p " +
                     "JOIN p.roles r WHERE p.code = :permissionCode AND r.code = :roleCode")
       boolean hasPermission(@Param("roleCode") String roleCode, @Param("permissionCode") String permissionCode);

       // 鎺掑簭鏌ヨ
       @Query("SELECT p FROM Permission p ORDER BY p.code")
       List<Permission> findAllOrderByCode();

       @Query("SELECT p FROM Permission p ORDER BY p.module, p.permissionType, p.code")
       List<Permission> findAllOrderByModuleAndTypeAndCode();
}


