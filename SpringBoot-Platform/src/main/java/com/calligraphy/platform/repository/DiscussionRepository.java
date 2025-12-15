package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    
    Page<Discussion> findByStatusNotOrderByCreatedAtDesc(Discussion.DiscussionStatus status, Pageable pageable);
    
    Page<Discussion> findByCategoryAndStatusNotOrderByCreatedAtDesc(String category, Discussion.DiscussionStatus status, Pageable pageable);
    
    Page<Discussion> findByUserIdAndStatusNotOrderByCreatedAtDesc(String userId, Discussion.DiscussionStatus status, Pageable pageable);
    
    Page<Discussion> findByTitleContainingIgnoreCaseAndStatusNotOrderByCreatedAtDesc(String keyword, Discussion.DiscussionStatus status, Pageable pageable);
    
    Page<Discussion> findByContentContainingIgnoreCaseAndStatusNotOrderByCreatedAtDesc(String keyword, Discussion.DiscussionStatus status, Pageable pageable);
    
    Page<Discussion> findByTagsContainingIgnoreCaseAndStatusNotOrderByCreatedAtDesc(String keyword, Discussion.DiscussionStatus status, Pageable pageable);
    
    List<Discussion> findByStatusNotOrderByCreatedAtDesc(Discussion.DiscussionStatus status);
    
    List<Discussion> findByCategoryAndStatusNotOrderByCreatedAtDesc(String category, Discussion.DiscussionStatus status);
    
    List<Discussion> findByUserIdAndStatusNotOrderByCreatedAtDesc(String userId, Discussion.DiscussionStatus status);
    
    List<Discussion> findByIsPinnedTrueAndStatusNotOrderByCreatedAtDesc(Discussion.DiscussionStatus status);
    
    List<Discussion> findByIsFeaturedTrueAndStatusNotOrderByCreatedAtDesc(Discussion.DiscussionStatus status);
    
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY d.createdAt DESC")
    Page<Discussion> searchDiscussions(@Param("keyword") String keyword, @Param("status") Discussion.DiscussionStatus status, Pageable pageable);
    
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY d.createdAt DESC")
    List<Discussion> searchDiscussions(@Param("keyword") String keyword, @Param("status") Discussion.DiscussionStatus status);
    
    long countByStatusNot(Discussion.DiscussionStatus status);
    
    long countByCategoryAndStatusNot(String category, Discussion.DiscussionStatus status);
    
    long countByUserIdAndStatusNot(String userId, Discussion.DiscussionStatus status);
    
    @Query("SELECT COUNT(d) FROM Discussion d WHERE d.status != :status AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    long countBySearchKeyword(@Param("keyword") String keyword, @Param("status") Discussion.DiscussionStatus status);
    
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND d.createdAt >= :startDate AND d.createdAt <= :endDate ORDER BY d.createdAt DESC")
    List<Discussion> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") Discussion.DiscussionStatus status);
    
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND d.createdAt >= :startDate AND d.createdAt <= :endDate ORDER BY d.createdAt DESC")
    Page<Discussion> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") Discussion.DiscussionStatus status, Pageable pageable);
    
    @Query("SELECT d FROM Discussion d WHERE d.status != :status ORDER BY d.likeCount DESC, d.createdAt DESC")
    List<Discussion> findTopLikedDiscussions(@Param("status") Discussion.DiscussionStatus status, Pageable pageable);
    
    @Query("SELECT d FROM Discussion d WHERE d.status != :status ORDER BY d.viewCount DESC, d.createdAt DESC")
    List<Discussion> findTopViewedDiscussions(@Param("status") Discussion.DiscussionStatus status, Pageable pageable);
    
    @Query("SELECT d FROM Discussion d WHERE d.status != :status ORDER BY d.replyCount DESC, d.createdAt DESC")
    List<Discussion> findTopRepliedDiscussions(@Param("status") Discussion.DiscussionStatus status, Pageable pageable);
    
    Optional<Discussion> findByIdAndStatusNot(Long id, Discussion.DiscussionStatus status);
}


