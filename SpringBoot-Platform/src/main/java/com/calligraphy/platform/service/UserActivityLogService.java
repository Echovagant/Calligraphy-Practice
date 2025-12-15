package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.UserActivityLogDto;
import com.calligraphy.platform.dto.UserActivityLogCreateDto;
import com.calligraphy.platform.model.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserActivityLogService {
    
    UserActivityLogDto createActivityLog(UserActivityLogCreateDto createDto);
    
    UserActivityLogDto createActivityLog(String userId, UserActivityLog.UserType userType, String activityType, String description, String ipAddress, String userAgent);
    
    UserActivityLogDto getActivityLogById(String logId);
    
    Optional<UserActivityLog> getActivityLogEntityById(String logId);
    
    Page<UserActivityLogDto> getAllActivityLogs(Pageable pageable);
    
    List<UserActivityLogDto> getAllActivityLogs();
    
    Page<UserActivityLogDto> getActivityLogsByUserId(String userId, Pageable pageable);
    
    List<UserActivityLogDto> getActivityLogsByUserId(String userId);
    
    Page<UserActivityLogDto> getActivityLogsByUserType(UserActivityLog.UserType userType, Pageable pageable);
    
    List<UserActivityLogDto> getActivityLogsByUserType(UserActivityLog.UserType userType);
    
    Page<UserActivityLogDto> getActivityLogsByActivityType(String activityType, Pageable pageable);
    
    List<UserActivityLogDto> getActivityLogsByActivityType(String activityType);
    
    Page<UserActivityLogDto> getActivityLogsByIpAddress(String ipAddress, Pageable pageable);
    
    List<UserActivityLogDto> getActivityLogsByIpAddress(String ipAddress);
    
    Page<UserActivityLogDto> getActivityLogsByStatus(String status, Pageable pageable);
    
    List<UserActivityLogDto> getActivityLogsByStatus(String status);
    
    // 鏃堕棿鑼冨洿鏌ヨ
    Page<UserActivityLogDto> getActivityLogsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, Pageable pageable);
    
    List<UserActivityLogDto> getActivityLogsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    Page<UserActivityLogDto> getActivityLogsByDateRangeAndUserId(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, Pageable pageable);
    
    List<UserActivityLogDto> getActivityLogsByDateRangeAndUserId(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 鎼滅储鍜岀瓫閫?    Page<UserActivityLogDto> searchActivityLogs(String keyword, Pageable pageable);
    
    List<UserActivityLogDto> searchActivityLogs(String keyword);
    
    // 缁熻鐩稿叧
    long getTotalActivityLogCount();
    
    long getActivityLogCountByUserId(String userId);
    
    long getActivityLogCountByUserType(UserActivityLog.UserType userType);
    
    long getActivityLogCountByActivityType(String activityType);
    
    long getActivityLogCountByIpAddress(String ipAddress);
    
    long getActivityLogCountByStatus(String status);
    
    long getActivityLogCountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 鐢ㄦ埛娲诲姩缁熻
    long getUserLoginCount(String userId);
    
    long getUserLoginCountByDateRange(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    long getUserFailedLoginCount(String userId);
    
    java.time.LocalDateTime getUserLastLoginTime(String userId);
    
    String getUserLastLoginIp(String userId);
    
    // 娲诲姩绫诲瀷缁熻
    java.util.Map<String, Long> getActivityTypeCounts();
    
    java.util.Map<String, Long> getActivityTypeCountsByUserId(String userId);
    
    java.util.Map<String, Long> getActivityTypeCountsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // IP鍦板潃缁熻
    java.util.Map<String, Long> getTopIpAddresses(int limit);
    
    java.util.Map<String, Long> getTopIpAddressesByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, int limit);
    
    // 鐢ㄦ埛绫诲瀷缁熻
    java.util.Map<UserActivityLog.UserType, Long> getUserTypeCounts();
    
    java.util.Map<UserActivityLog.UserType, Long> getUserTypeCountsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 鍝嶅簲鐘舵€佺粺璁?    java.util.Map<String, Long> getResponseStatusCounts();
    
    java.util.Map<String, Long> getResponseStatusCountsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 鏈€杩戞椿鍔?    List<UserActivityLogDto> getRecentActivityLogs(int limit);
    
    List<UserActivityLogDto> getRecentActivityLogsByUserId(String userId, int limit);
    
    List<UserActivityLogDto> getRecentActivityLogsByUserType(UserActivityLog.UserType userType, int limit);
    
    List<UserActivityLogDto> getRecentActivityLogsByActivityType(String activityType, int limit);
    
    // 瀹夊叏鐩稿叧
    List<UserActivityLogDto> getSuspiciousActivityLogs();
    
    List<UserActivityLogDto> getSuspiciousActivityLogsByUserId(String userId);
    
    List<UserActivityLogDto> getFailedLoginAttempts();
    
    List<UserActivityLogDto> getFailedLoginAttemptsByUserId(String userId);
    
    List<UserActivityLogDto> getFailedLoginAttemptsByIpAddress(String ipAddress);
    
    boolean isSuspiciousIpAddress(String ipAddress);
    
    boolean isSuspiciousUserActivity(String userId);
    
    // 鎵归噺鎿嶄綔
    void bulkDeleteActivityLogs(List<String> logIds);
    
    void bulkUpdateStatus(List<String> logIds, String status);
    
    // 鏃ュ織娓呯悊
    void cleanupOldActivityLogs(int daysToKeep);
    
    void cleanupActivityLogsByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 娲诲姩鍒嗘瀽
    java.util.Map<String, Object> getUserActivityAnalysis(String userId);
    
    java.util.Map<String, Object> getSystemActivityAnalysis();
    
    java.util.Map<String, Object> getActivityTrendAnalysis(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    // 瀵煎嚭鍔熻兘
    byte[] exportActivityLogs(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, String format);
    
    byte[] exportUserActivityLogs(String userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, String format);
}


