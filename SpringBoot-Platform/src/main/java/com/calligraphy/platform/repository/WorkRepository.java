package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
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

    /**
     * 某个用户的全部作品。
     *
     * <p>{@code user} 走 JOIN 一次取齐：{@code WorkMapper.toDto} 会读
     * {@code work.getUser()}，不 JOIN 的话每条作品都会再查一次 users（N+1）。
     */
    @EntityGraph(attributePaths = {"user"})
    List<Work> findByUserId(String userId);

    /**
     * 某个用户的作品（**分页**）。
     *
     * <p>不能用 List 版本自己切片：那样 total / totalPages 会算错，
     * 而且数据量大时会把整批作品全查出来。
     */
    @EntityGraph(attributePaths = {"user"})
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

    /**
     * 某个用户的作品数、收到的点赞总数与浏览总数 —— **聚合查询**。
     *
     * <p>用户资料页要的就是这几个数字。以前是把该用户的全部作品（含 TEXT content 列）
     * 查出来在内存里求和，数据量越大越慢；这里让数据库直接算，
     * 返回行数恒为 1，与作品数量无关。
     *
     * <p>返回一行 {@code [作品数, 点赞总数, 浏览总数]}。
     */
    @Query("SELECT COUNT(w), COALESCE(SUM(w.likeCount), 0), COALESCE(SUM(w.viewCount), 0) "
            + "FROM Work w WHERE w.userId = :userId")
    List<Object[]> sumWorkCounts(@Param("userId") String userId);

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
    /**
     * 待审核作品（不分页）。
     *
     * <p>{@code user} 走 JOIN 一次取齐：{@code WorkMapper.toDto} 会读
     * {@code work.getUser()}，不 JOIN 的话每条作品都会再查一次 users（N+1）。
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT w FROM Work w WHERE w.reviewStatus = :pending OR w.reviewStatus = :needsRevision ORDER BY w.createdAt ASC")
    List<Work> findWorksNeedingReview(@Param("pending") Work.ReviewStatus pending, @Param("needsRevision") Work.ReviewStatus needsRevision);

    // 待审核作品查询（分页）—— 后台「作品审核」列表用
    /**
     * 待审核作品（分页）。
     *
     * <p>{@code user} 走 JOIN 一次取齐，避免每条作品再查一次 users。
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT w FROM Work w WHERE w.reviewStatus = :pending OR w.reviewStatus = :needsRevision ORDER BY w.createdAt ASC")
    Page<Work> findWorksNeedingReview(@Param("pending") Work.ReviewStatus pending,
                                     @Param("needsRevision") Work.ReviewStatus needsRevision,
                                     org.springframework.data.domain.Pageable pageable);

    /**
     * 待审核作品 + 关键词（分页、库内筛选）。
     *
     * <p>关键词匹配**作品标题或作者名**（username / nickname）。
     * 作者名走 users 表 —— 用 EXISTS 子查询，不 JOIN users：
     * 一来避免与 @EntityGraph 的 users 关联重复连接，
     * 二来免得每条作品再查一次作者（N+1）。
     *
     * <p>{@code keyword} 为 null 时不把条件加进 where。
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT w FROM Work w WHERE (w.reviewStatus = :pending OR w.reviewStatus = :needsRevision) "
            + "AND (:keyword IS NULL OR LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR EXISTS (SELECT u FROM User u WHERE u.id = w.userId "
            + "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))))) "
            + "ORDER BY w.createdAt ASC")
    Page<Work> findWorksNeedingReviewByKeyword(@Param("pending") Work.ReviewStatus pending,
                                                @Param("needsRevision") Work.ReviewStatus needsRevision,
                                                @Param("keyword") String keyword,
                                                org.springframework.data.domain.Pageable pageable);

    /**
     * 公开画廊的作品（分页）。
     *
     * <p>{@code user} 走 JOIN 一次取齐：一页 20 条作品原本会再发 20 次 users 查询。
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT w FROM Work w WHERE w.status = :status AND w.reviewStatus = :reviewStatus")
    Page<Work> findByStatusAndReviewStatus(@Param("status") Work.WorkStatus status, @Param("reviewStatus") Work.ReviewStatus reviewStatus, org.springframework.data.domain.Pageable pageable);

    /**
     * 公开画廊的作品 + 关键词（分页、库内筛选）。
     *
     * <p>关键词匹配标题或描述，**大小写不敏感**。
     * {@code keyword} 为 null 时不把条件加进 where —— 否则一条都查不到。
     *
     * <p>排序由 {@link Pageable} 里的 Sort 决定（latest / popular / commented），
     * 这样一条查询就能切排序，不用为每种排序再写一个方法。
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT w FROM Work w WHERE w.status = :status AND w.reviewStatus = :reviewStatus "
            + "AND (:keyword IS NULL OR LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Work> findPublicWorks(@Param("status") Work.WorkStatus status,
                               @Param("reviewStatus") Work.ReviewStatus reviewStatus,
                               @Param("keyword") String keyword,
                               org.springframework.data.domain.Pageable pageable);

    /**
     * 某个用户的作品 + 关键词（分页、库内筛选）。
     *
     * <p>关键词匹配标题或描述，大小写不敏感；null 表示不过滤。
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT w FROM Work w WHERE w.userId = :userId "
            + "AND (:keyword IS NULL OR LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Work> findByUserIdAndKeyword(@Param("userId") String userId,
                                       @Param("keyword") String keyword,
                                       org.springframework.data.domain.Pageable pageable);
    
    // 热门作品查询
    @Query("SELECT w FROM Work w WHERE w.status = :published AND w.reviewStatus = :approved ORDER BY w.likeCount DESC")
    List<Work> findPopularWorks(@Param("published") Work.WorkStatus published, @Param("approved") Work.ReviewStatus approved);
    
    // 最新作品查询
    @Query("SELECT w FROM Work w WHERE w.status = :published AND w.reviewStatus = :approved ORDER BY w.createdAt DESC")
    List<Work> findLatestWorks(@Param("published") Work.WorkStatus published, @Param("approved") Work.ReviewStatus approved);

    List<Work> findByReviewerId(String reviewerId);
}


