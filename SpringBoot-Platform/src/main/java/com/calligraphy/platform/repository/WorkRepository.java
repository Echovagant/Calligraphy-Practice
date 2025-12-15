package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.model.FrontendUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {
    
    List<Work> findByUser(FrontendUser user);
    
    List<Work> findByUserId(String userId);
    
    List<Work> findByStatus(Work.WorkStatus status);
    
    List<Work> findByReviewStatus(Work.ReviewStatus reviewStatus);
    
    @Query("SELECT w FROM Work w WHERE w.user = :user AND w.status = :status")
    List<Work> findByUserAndStatus(@Param("user") FrontendUser user, @Param("status") Work.WorkStatus status);
    
    @Query("SELECT w FROM Work w WHERE w.reviewStatus = :reviewStatus AND w.status = :status")
    List<Work> findByReviewStatusAndStatus(@Param("reviewStatus") Work.ReviewStatus reviewStatus, @Param("status") Work.WorkStatus status);
    
    @Query("SELECT w FROM Work w WHERE w.title LIKE %:keyword% OR w.content LIKE %:keyword%")
    List<Work> searchWorks(@Param("keyword") String keyword);
    
    @Query("SELECT w FROM Work w WHERE w.user = :user AND (w.title LIKE %:keyword% OR w.content LIKE %:keyword%)")
    List<Work> searchUserWorks(@Param("user") FrontendUser user, @Param("keyword") String keyword);
    
    @Query("SELECT w FROM Work w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<Work> findWorksCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT w FROM Work w WHERE w.user = :user AND w.createdAt BETWEEN :startDate AND :endDate")
    List<Work> findUserWorksCreatedBetween(@Param("user") FrontendUser user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 缁熻鐩稿叧鏌ヨ
    @Query("SELECT COUNT(w) FROM Work w WHERE w.user = :user")
    long countByUser(@Param("user") FrontendUser user);
    
    @Query("SELECT COUNT(w) FROM Work w WHERE w.status = :status")
    long countByStatus(@Param("status") Work.WorkStatus status);
    
    @Query("SELECT COUNT(w) FROM Work w WHERE w.reviewStatus = :reviewStatus")
    long countByReviewStatus(@Param("reviewStatus") Work.ReviewStatus reviewStatus);
    
    @Query("SELECT COUNT(w) FROM Work w WHERE w.status = :status AND w.reviewStatus = :reviewStatus")
    long countByStatusAndReviewStatus(@Param("status") Work.WorkStatus status, @Param("reviewStatus") Work.ReviewStatus reviewStatus);
    
    @Query("SELECT COUNT(w) FROM Work w WHERE w.user = :user AND w.status = :status")
    long countByUserAndStatus(@Param("user") FrontendUser user, @Param("status") Work.WorkStatus status);
    
    // 鎺掑簭鐩稿叧鏌ヨ
    @Query("SELECT w FROM Work w ORDER BY w.createdAt DESC")
    List<Work> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT w FROM Work w WHERE w.status = :status ORDER BY w.createdAt DESC")
    List<Work> findByStatusOrderByCreatedAtDesc(@Param("status") Work.WorkStatus status);
    
    @Query("SELECT w FROM Work w ORDER BY w.likeCount DESC")
    List<Work> findAllOrderByLikeCountDesc();
    
    @Query("SELECT w FROM Work w ORDER BY w.viewCount DESC")
    List<Work> findAllOrderByViewCountDesc();
    
    // 寰呭鏍镐綔鍝佹煡璇?    @Query("SELECT w FROM Work w WHERE w.reviewStatus = :pending OR w.reviewStatus = :needsRevision ORDER BY w.createdAt ASC")
    List<Work> findWorksNeedingReview(@Param("pending") Work.ReviewStatus pending, @Param("needsRevision") Work.ReviewStatus needsRevision);
    
    @Query("SELECT w FROM Work w WHERE w.status = :status AND w.reviewStatus = :reviewStatus")
    Page<Work> findByStatusAndReviewStatus(@Param("status") Work.WorkStatus status, @Param("reviewStatus") Work.ReviewStatus reviewStatus, org.springframework.data.domain.Pageable pageable);
    
    // 鐑棬浣滃搧鏌ヨ
    @Query("SELECT w FROM Work w WHERE w.status = :published AND w.reviewStatus = :approved ORDER BY w.likeCount DESC")
    List<Work> findPopularWorks(@Param("published") Work.WorkStatus published, @Param("approved") Work.ReviewStatus approved);
    
    // 鏈€鏂颁綔鍝佹煡璇?    @Query("SELECT w FROM Work w WHERE w.status = :published AND w.reviewStatus = :approved ORDER BY w.createdAt DESC")
    List<Work> findLatestWorks(@Param("published") Work.WorkStatus published, @Param("approved") Work.ReviewStatus approved);

    List<Work> findByReviewerId(String reviewerId);
}


