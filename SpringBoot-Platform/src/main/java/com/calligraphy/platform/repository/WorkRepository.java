package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {
    
    List<Work> findByUser(User user);
    
    List<Work> findByUserId(String userId);

    /**
     * 某个用户的作品（**分页**）。
     *
     * <p>不能用 List 版本自己切片：那样 total / totalPages 会算错，
     * 而且数据量大时会把整批作品全查出来。
     */
    Page<Work> findByUserId(String userId, org.springframework.data.domain.Pageable pageable);

    /**
     * 浏览量 +1 —— 独立的一条 UPDATE 语句，不依赖实体脏检查。
     *
     * <p>浏览量自增必须走批量 UPDATE：类级事务是 readOnly，
     * 「改实体再 save」在只读事务里不会 flush（FlushMode.MANUAL），
     * 响应里的数字变了、库里却永远是 0。
     */
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Work w SET w.viewCount = w.viewCount + 1 WHERE w.id = :id")
    int incrementViewCount(@Param("id") Long id);


    List<Work> findByStatus(Work.WorkStatus status);
    
    List<Work> findByReviewStatus(Work.ReviewStatus reviewStatus);
    
    @Query("SELECT w FROM Work w WHERE w.user = :user AND w.status = :status")
    List<Work> findByUserAndStatus(@Param("user") User user, @Param("status") Work.WorkStatus status);
    
    @Query("SELECT w FROM Work w WHERE w.reviewStatus = :reviewStatus AND w.status = :status")
    List<Work> findByReviewStatusAndStatus(@Param("reviewStatus") Work.ReviewStatus reviewStatus, @Param("status") Work.WorkStatus status);
    
    @Query("SELECT w FROM Work w WHERE w.title LIKE %:keyword% OR w.content LIKE %:keyword%")
    List<Work> searchWorks(@Param("keyword") String keyword);
    
    @Query("SELECT w FROM Work w WHERE w.user = :user AND (w.title LIKE %:keyword% OR w.content LIKE %:keyword%)")
    List<Work> searchUserWorks(@Param("user") User user, @Param("keyword") String keyword);
    
    @Query("SELECT w FROM Work w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<Work> findWorksCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT w FROM Work w WHERE w.user = :user AND w.createdAt BETWEEN :startDate AND :endDate")
    List<Work> findUserWorksCreatedBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 统计相关查询
    @Query("SELECT COUNT(w) FROM Work w WHERE w.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(w) FROM Work w WHERE w.status = :status")
    long countByStatus(@Param("status") Work.WorkStatus status);
    
    @Query("SELECT COUNT(w) FROM Work w WHERE w.reviewStatus = :reviewStatus")
    long countByReviewStatus(@Param("reviewStatus") Work.ReviewStatus reviewStatus);
    
    @Query("SELECT COUNT(w) FROM Work w WHERE w.status = :status AND w.reviewStatus = :reviewStatus")
    long countByStatusAndReviewStatus(@Param("status") Work.WorkStatus status, @Param("reviewStatus") Work.ReviewStatus reviewStatus);
    
    @Query("SELECT COUNT(w) FROM Work w WHERE w.user = :user AND w.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") Work.WorkStatus status);
    
    // 排序相关查询
    @Query("SELECT w FROM Work w ORDER BY w.createdAt DESC")
    List<Work> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT w FROM Work w WHERE w.status = :status ORDER BY w.createdAt DESC")
    List<Work> findByStatusOrderByCreatedAtDesc(@Param("status") Work.WorkStatus status);
    
    @Query("SELECT w FROM Work w ORDER BY w.likeCount DESC")
    List<Work> findAllOrderByLikeCountDesc();
    
    @Query("SELECT w FROM Work w ORDER BY w.viewCount DESC")
    List<Work> findAllOrderByViewCountDesc();
    
    // 待审核作品查询
    @Query("SELECT w FROM Work w WHERE w.reviewStatus = :pending OR w.reviewStatus = :needsRevision ORDER BY w.createdAt ASC")
    List<Work> findWorksNeedingReview(@Param("pending") Work.ReviewStatus pending, @Param("needsRevision") Work.ReviewStatus needsRevision);

    // 待审核作品查询（分页）—— 后台「作品审核」列表用
    @Query("SELECT w FROM Work w WHERE w.reviewStatus = :pending OR w.reviewStatus = :needsRevision ORDER BY w.createdAt ASC")
    Page<Work> findWorksNeedingReview(@Param("pending") Work.ReviewStatus pending,
                                     @Param("needsRevision") Work.ReviewStatus needsRevision,
                                     org.springframework.data.domain.Pageable pageable);

    @Query("SELECT w FROM Work w WHERE w.status = :status AND w.reviewStatus = :reviewStatus")
    Page<Work> findByStatusAndReviewStatus(@Param("status") Work.WorkStatus status, @Param("reviewStatus") Work.ReviewStatus reviewStatus, org.springframework.data.domain.Pageable pageable);
    
    // 热门作品查询
    @Query("SELECT w FROM Work w WHERE w.status = :published AND w.reviewStatus = :approved ORDER BY w.likeCount DESC")
    List<Work> findPopularWorks(@Param("published") Work.WorkStatus published, @Param("approved") Work.ReviewStatus approved);
    
    // 最新作品查询
    @Query("SELECT w FROM Work w WHERE w.status = :published AND w.reviewStatus = :approved ORDER BY w.createdAt DESC")
    List<Work> findLatestWorks(@Param("published") Work.WorkStatus published, @Param("approved") Work.ReviewStatus approved);

    List<Work> findByReviewerId(String reviewerId);
}


