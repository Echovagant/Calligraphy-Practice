package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    
    List<UserActivityLog> findByUserId(String userId);
    
    List<UserActivityLog> findByUserType(UserActivityLog.UserType userType);
    
    List<UserActivityLog> findByActivityType(String activityType);
    
    List<UserActivityLog> findByIpAddress(String ipAddress);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.userId = :userId AND l.userType = :userType")
    List<UserActivityLog> findByUserIdAndType(@Param("userId") String userId, @Param("userType") UserActivityLog.UserType userType);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.userId = :userId AND l.activityType = :activityType")
    List<UserActivityLog> findByUserIdAndActivityType(@Param("userId") String userId, @Param("activityType") String activityType);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.activityType = :activityType AND l.createdAt BETWEEN :startDate AND :endDate")
    List<UserActivityLog> findByActivityTypeAndDateRange(@Param("activityType") String activityType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.ipAddress = :ipAddress AND l.createdAt BETWEEN :startDate AND :endDate")
    List<UserActivityLog> findByIpAddressAndDateRange(@Param("ipAddress") String ipAddress, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.userId = :userId AND l.createdAt BETWEEN :startDate AND :endDate")
    List<UserActivityLog> findByUserIdAndDateRange(@Param("userId") String userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate")
    List<UserActivityLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Hibernate 6 无法把嵌套枚举的「点号全限定名」当作枚举字面量解析
    // （SemanticException: Could not interpret path expression），
    // 嵌套枚举必须写成二进制名（$ 分隔），否则仓库 bean 创建失败、上下文启动不了。
    @Query("SELECT l FROM UserActivityLog l WHERE l.responseStatusEnum = com.calligraphy.platform.model.UserActivityLog$ResponseStatus.ERROR")
    List<UserActivityLog> findErrorActivities();

    @Query("SELECT l FROM UserActivityLog l WHERE l.userId = :userId AND l.responseStatusEnum = com.calligraphy.platform.model.UserActivityLog$ResponseStatus.ERROR")
    List<UserActivityLog> findErrorActivitiesByUser(@Param("userId") String userId);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.ipAddress = :ipAddress")
    List<UserActivityLog> findByIpAddressQuery(@Param("ipAddress") String ipAddress);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.userAgent LIKE %:userAgent%")
    List<UserActivityLog> findByUserAgentContaining(@Param("userAgent") String userAgent);
    
    // 登录相关查询
    @Query("SELECT l FROM UserActivityLog l WHERE l.activityType = 'USER_LOGIN' AND l.userId = :userId ORDER BY l.createdAt DESC")
    List<UserActivityLog> findLoginActivitiesByUser(@Param("userId") String userId);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.activityType = 'USER_LOGIN' AND l.ipAddress = :ipAddress")
    List<UserActivityLog> findLoginActivitiesByIp(@Param("ipAddress") String ipAddress);
    
    // 安全相关查询
    @Query("SELECT l FROM UserActivityLog l WHERE l.activityType IN ('LOGIN_FAILED', 'PASSWORD_RESET_REQUEST', 'ACCOUNT_LOCKED')")
    List<UserActivityLog> findSecurityActivities();
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.userId = :userId AND l.activityType IN ('LOGIN_FAILED', 'PASSWORD_RESET_REQUEST', 'ACCOUNT_LOCKED')")
    List<UserActivityLog> findSecurityActivitiesByUser(@Param("userId") String userId);
    
    // 统计相关查询
    @Query("SELECT COUNT(l) FROM UserActivityLog l WHERE l.userId = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(l) FROM UserActivityLog l WHERE l.activityType = :activityType")
    long countByActivityType(@Param("activityType") String activityType);
    
    @Query("SELECT COUNT(l) FROM UserActivityLog l WHERE l.userType = :userType")
    long countByUserType(@Param("userType") UserActivityLog.UserType userType);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.responseStatusEnum = :status")
    long countByResponseStatus(@Param("status") UserActivityLog.ResponseStatus status);
    
    @Query("SELECT COUNT(l) FROM UserActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate")
    long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 活跃度统计
    @Query("SELECT COUNT(DISTINCT l.userId) FROM UserActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate")
    long countActiveUsers(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(DISTINCT l.userId) FROM UserActivityLog l WHERE l.userType = :userType AND l.createdAt BETWEEN :startDate AND :endDate")
    long countActiveUsersByType(@Param("userType") UserActivityLog.UserType userType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 活动类型统计
    @Query("SELECT l.activityType, COUNT(l) FROM UserActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate GROUP BY l.activityType")
    List<Object[]> getActivityTypeStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT l.activityType, COUNT(l) FROM UserActivityLog l WHERE l.userType = :userType AND l.createdAt BETWEEN :startDate AND :endDate GROUP BY l.activityType")
    List<Object[]> getActivityTypeStatsByUserType(@Param("userType") UserActivityLog.UserType userType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 用户类型统计
    @Query("SELECT l.userType, COUNT(l) FROM UserActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate GROUP BY l.userType")
    List<Object[]> getUserTypeStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 响应状态统计
    @Query("SELECT l FROM UserActivityLog l WHERE l.responseStatusEnum = :status")
    List<UserActivityLog> findByResponseStatus(@Param("status") UserActivityLog.ResponseStatus status);
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.responseStatusEnum = :status AND l.createdAt BETWEEN :startDate AND :endDate")
    List<UserActivityLog> findByResponseStatusAndDateRange(@Param("status") UserActivityLog.ResponseStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT l.responseStatusEnum, COUNT(l) FROM UserActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate GROUP BY l.responseStatusEnum")
    List<Object[]> getResponseStatusStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // IP地址统计
    @Query("SELECT l.ipAddress, COUNT(l) FROM UserActivityLog l WHERE l.createdAt BETWEEN :startDate AND :endDate GROUP BY l.ipAddress ORDER BY COUNT(l) DESC")
    List<Object[]> getIpAddressStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 最新活动查询
    @Query("SELECT l FROM UserActivityLog l ORDER BY l.createdAt DESC")
    List<UserActivityLog> findLatestActivities();
    
    @Query("SELECT l FROM UserActivityLog l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    List<UserActivityLog> findLatestActivitiesByUser(@Param("userId") String userId);
    
    // 批量清理
    @Query("DELETE FROM UserActivityLog l WHERE l.createdAt < :date")
    void deleteActivitiesOlderThan(@Param("date") LocalDateTime date);
}


