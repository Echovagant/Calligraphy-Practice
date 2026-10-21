package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Comment;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByWork(Work work);
    
    List<Comment> findByWorkId(Long workId);
    
    List<Comment> findByUser(User user);
    
    List<Comment> findByUserId(String userId);
    
    List<Comment> findByStatus(String status);
    
    @Query("SELECT c FROM Comment c WHERE c.work = :work AND c.status = :status")
    List<Comment> findByWorkAndStatus(@Param("work") Work work, @Param("status") String status);
    
    @Query("SELECT c FROM Comment c WHERE c.user = :user AND c.status = :status")
    List<Comment> findByUserAndStatus(@Param("user") User user, @Param("status") String status);
    
    @Query("SELECT c FROM Comment c WHERE c.parentId = :parentId")
    List<Comment> findByParentId(@Param("parentId") Long parentId);
    
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword%")
    List<Comment> searchComments(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Comment c WHERE c.work = :work AND c.content LIKE %:keyword%")
    List<Comment> searchCommentsByWork(@Param("work") Work work, @Param("keyword") String keyword);
    
    @Query("SELECT c FROM Comment c WHERE c.user = :user AND c.content LIKE %:keyword%")
    List<Comment> searchCommentsByUser(@Param("user") User user, @Param("keyword") String keyword);
    
    @Query("SELECT c FROM Comment c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Comment> findCommentsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT c FROM Comment c WHERE c.user = :user AND c.createdAt BETWEEN :startDate AND :endDate")
    List<Comment> findCommentsByUserCreatedBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 统计相关查询
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.work = :work")
    long countByWork(@Param("work") Work work);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user = :user")
    long countByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.work = :work AND c.status = :status")
    long countByWorkAndStatus(@Param("work") Work work, @Param("status") String status);
    
    // 热门评论查询
    @Query("SELECT c FROM Comment c WHERE c.status = 'APPROVED' ORDER BY c.likeCount DESC")
    List<Comment> findPopularComments();
    
    // 最新评论查询
    @Query("SELECT c FROM Comment c WHERE c.status = 'APPROVED' ORDER BY c.createdAt DESC")
    List<Comment> findLatestComments();
    
    // 排序相关查询
    @Query("SELECT c FROM Comment c WHERE c.work = :work ORDER BY c.createdAt DESC")
    List<Comment> findByWorkOrderByCreatedAtDesc(@Param("work") Work work);
    
    @Query("SELECT c FROM Comment c WHERE c.work = :work ORDER BY c.likeCount DESC")
    List<Comment> findByWorkOrderByLikeCountDesc(@Param("work") Work work);
    
    // 根评论查询（不包含回复）
    @Query("SELECT c FROM Comment c WHERE c.work = :work AND c.parentId IS NULL AND c.status = 'APPROVED' ORDER BY c.createdAt DESC")
    List<Comment> findRootCommentsByWork(@Param("work") Work work);
    
    // 批量操作
    @Query("UPDATE Comment c SET c.status = :status WHERE c.work = :work")
    void updateStatusByWork(@Param("work") Work work, @Param("status") String status);
    
    @Query("UPDATE Comment c SET c.status = :status WHERE c.user = :user")
    void updateStatusByUser(@Param("user") User user, @Param("status") String status);
    
    // 新增查询方法
    List<Comment> findByWorkIdAndStatusNotOrderByCreatedAtDesc(Long workId, String status);
    
    Page<Comment> findByWorkIdAndStatusNotOrderByCreatedAtDesc(Long workId, String status, Pageable pageable);
    
    Page<Comment> findByUserIdAndStatusNotOrderByCreatedAtDesc(String userId, String status, Pageable pageable);
    
    List<Comment> findByParentIdAndStatusNotOrderByCreatedAtAsc(Long parentId, String status);
    
    long countByWorkIdAndStatusNot(Long workId, String status);
    Page<Comment> findByStatus(String status, Pageable pageable);
    long countByParentIdAndStatusNot(Long parentId, String status);
    long countByUserIdAndStatusNot(String userId, String status);
}


