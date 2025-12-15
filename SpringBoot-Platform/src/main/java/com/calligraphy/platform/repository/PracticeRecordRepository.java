package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.PracticeRecord;
import com.calligraphy.platform.model.FrontendUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeRecordRepository extends JpaRepository<PracticeRecord, Long> {
    
    List<PracticeRecord> findByUser(FrontendUser user);
    
    List<PracticeRecord> findByUserId(String userId);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.user = :user AND p.copybookId = :copybookId")
    Optional<PracticeRecord> findByUserAndCopybookId(@Param("user") FrontendUser user, @Param("copybookId") String copybookId);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.user = :user AND p.isCompleted = true")
    List<PracticeRecord> findCompletedByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.user = :user AND p.isCompleted = false")
    List<PracticeRecord> findIncompleteByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.user = :user AND p.satisfactionScore >= :rating")
    List<PracticeRecord> findByUserAndSatisfactionScoreGreaterThanEqual(@Param("user") FrontendUser user, @Param("rating") Integer rating);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.user = :user AND p.practiceTime >= :time")
    List<PracticeRecord> findByUserAndPracticeTimeGreaterThanEqual(@Param("user") FrontendUser user, @Param("time") Integer time);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.user = :user AND p.createdAt BETWEEN :startDate AND :endDate")
    List<PracticeRecord> findByUserAndCreatedAtBetween(@Param("user") FrontendUser user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.copybookId = :copybookId")
    List<PracticeRecord> findByCopybookId(@Param("copybookId") String copybookId);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.copybookId = :copybookId AND p.isCompleted = true")
    List<PracticeRecord> findCompletedByCopybookId(@Param("copybookId") String copybookId);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.copybookId = :copybookId AND p.isCompleted = false")
    List<PracticeRecord> findIncompleteByCopybookId(@Param("copybookId") String copybookId);
    
    // 缁熻鐩稿叧鏌ヨ
    @Query("SELECT COUNT(p) FROM PracticeRecord p WHERE p.user = :user")
    long countByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT COUNT(p) FROM PracticeRecord p WHERE p.user = :user AND p.isCompleted = true")
    long countCompletedByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT COUNT(p) FROM PracticeRecord p WHERE p.user = :user AND p.isCompleted = false")
    long countIncompleteByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT COUNT(p) FROM PracticeRecord p WHERE p.copybookId = :copybookId")
    long countByCopybookId(@Param("copybookId") String copybookId);
    
    @Query("SELECT COUNT(p) FROM PracticeRecord p WHERE p.copybookId = :copybookId AND p.isCompleted = true")
    long countCompletedByCopybookId(@Param("copybookId") String copybookId);
    
    @Query("SELECT COUNT(p) FROM PracticeRecord p WHERE p.isCompleted = true")
    long countAllCompleted();
    
    @Query("SELECT COUNT(p) FROM PracticeRecord p WHERE p.isCompleted = false")
    long countAllIncomplete();
    
    // 缁冧範鏃堕棿缁熻
    @Query("SELECT SUM(p.practiceTime) FROM PracticeRecord p WHERE p.user = :user")
    Integer sumPracticeTimeByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT AVG(p.practiceTime) FROM PracticeRecord p WHERE p.user = :user")
    Double avgPracticeTimeByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT AVG(p.practiceTime) FROM PracticeRecord p WHERE p.copybookId = :copybookId")
    Double avgPracticeTimeByCopybookId(@Param("copybookId") String copybookId);
    
    // 婊℃剰搴︾粺璁?    @Query("SELECT AVG(p.satisfactionScore) FROM PracticeRecord p WHERE p.user = :user")
    Double avgSatisfactionScoreByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT AVG(p.satisfactionScore) FROM PracticeRecord p WHERE p.copybookId = :copybookId")
    Double avgSatisfactionScoreByCopybookId(@Param("copybookId") String copybookId);
    
    // 瀹屾垚鐜囩粺璁?    @Query("SELECT CAST(COUNT(CASE WHEN p.isCompleted = true THEN 1 END) AS DOUBLE) / COUNT(p) * 100 " +
           "FROM PracticeRecord p WHERE p.user = :user")
    Double getCompletionRateByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT CAST(COUNT(CASE WHEN p.isCompleted = true THEN 1 END) AS DOUBLE) / COUNT(p) * 100 " +
           "FROM PracticeRecord p WHERE p.copybookId = :copybookId")
    Double getCompletionRateByCopybookId(@Param("copybookId") String copybookId);
    
    // 鏈€杩戠粌涔犺褰?    @Query("SELECT p FROM PracticeRecord p WHERE p.user = :user ORDER BY p.practiceDate DESC")
    List<PracticeRecord> findByUserOrderByPracticeDateDesc(@Param("user") FrontendUser user);
    
    @Query("SELECT p FROM PracticeRecord p WHERE p.user = :user AND p.isCompleted = true ORDER BY p.practiceDate DESC")
    List<PracticeRecord> findCompletedByUserOrderByPracticeDateDesc(@Param("user") FrontendUser user);
    
    // 鎵归噺鎿嶄綔
    @Query("UPDATE PracticeRecord p SET p.isCompleted = true " +
           "WHERE p.user = :user AND p.copybookId = :copybookId")
    void markAsCompleted(@Param("user") FrontendUser user, @Param("copybookId") String copybookId);
    
    @Query("DELETE FROM PracticeRecord p WHERE p.user = :user AND p.isCompleted = false")
    void deleteIncompleteByUser(@Param("user") FrontendUser user);
}


