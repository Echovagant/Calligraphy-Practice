package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.FrontendUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FrontendUserRepository extends JpaRepository<FrontendUser, String> {
    
    Optional<FrontendUser> findByUsername(String username);
    
    Optional<FrontendUser> findByEmail(String email);
    
    Optional<FrontendUser> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM FrontendUser u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<FrontendUser> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    @Query("SELECT u FROM FrontendUser u WHERE u.status = :status")
    List<FrontendUser> findByStatus(@Param("status") FrontendUser.FrontendUserStatus status);
    
    @Query("SELECT u FROM FrontendUser u WHERE u.status = :status")
    List<FrontendUser> findUsersByStatus(@Param("status") FrontendUser.FrontendUserStatus status);
    
    @Query("SELECT u FROM FrontendUser u WHERE u.lastLogin < :date")
    List<FrontendUser> findInactiveUsers(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM FrontendUser u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<FrontendUser> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM FrontendUser u WHERE u.status = :status")
    long countByStatus(@Param("status") FrontendUser.FrontendUserStatus status);
    
    // 统计相关查询
    @Query("SELECT COUNT(u) FROM FrontendUser u WHERE u.createdAt >= :date")
    long countNewUsersSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT DATE(u.createdAt) as date, COUNT(u) as count FROM FrontendUser u " +
           "WHERE u.createdAt >= :startDate GROUP BY DATE(u.createdAt) ORDER BY date")
    List<Object[]> getUserRegistrationStats(@Param("startDate") LocalDateTime startDate);
    
    // 搜索相关查询
    @Query("SELECT u FROM FrontendUser u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<FrontendUser> searchUsers(@Param("keyword") String keyword);
    
    // 批量操作
    @Query("DELETE FROM FrontendUser u WHERE u.status = :status AND u.updatedAt < :date")
    void deleteInactiveUsers(@Param("status") FrontendUser.FrontendUserStatus status, @Param("date") LocalDateTime date);
}


