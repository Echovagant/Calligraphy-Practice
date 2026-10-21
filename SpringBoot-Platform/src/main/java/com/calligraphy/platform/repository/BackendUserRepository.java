package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.BackendUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BackendUserRepository extends JpaRepository<BackendUser, String> {
    
    Optional<BackendUser> findByUsername(String username);
    
    Optional<BackendUser> findByEmail(String email);
    
    Optional<BackendUser> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM BackendUser u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<BackendUser> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    @Query("SELECT u FROM BackendUser u WHERE u.role = :role")
    List<BackendUser> findByRole(@Param("role") BackendUser.BackendUserRole role);
    
    @Query("SELECT u FROM BackendUser u WHERE u.department = :department")
    List<BackendUser> findByDepartment(@Param("department") String department);
    
    @Query("SELECT u FROM BackendUser u WHERE u.accountLocked = true")
    List<BackendUser> findLockedUsers();
    
    @Query("SELECT u FROM BackendUser u WHERE u.lastLogin < :date")
    List<BackendUser> findInactiveUsers(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM BackendUser u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<BackendUser> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM BackendUser u WHERE u.passwordChangedAt < :date")
    List<BackendUser> findUsersWithExpiredPasswords(@Param("date") LocalDateTime date);
    
    // 统计相关查询
    @Query("SELECT COUNT(u) FROM BackendUser u WHERE u.role = :role")
    long countByRole(@Param("role") BackendUser.BackendUserRole role);
    
    @Query("SELECT COUNT(u) FROM BackendUser u WHERE u.department = :department")
    long countByDepartment(@Param("department") String department);
    
    @Query("SELECT COUNT(u) FROM BackendUser u WHERE u.createdAt >= :date")
    long countNewUsersSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT u.department, COUNT(u) FROM BackendUser u GROUP BY u.department")
    List<Object[]> getDepartmentStats();
    
    // 搜索相关查询
    @Query("SELECT u FROM BackendUser u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BackendUser> searchUsers(@Param("keyword") String keyword);
    
    // 批量操作
    @Query("UPDATE BackendUser u SET u.accountLocked = false, u.failedLoginAttempts = 0 " +
           "WHERE u.accountLocked = true AND u.lockUntil < :now")
    void unlockExpiredAccounts(@Param("now") LocalDateTime now);
}


