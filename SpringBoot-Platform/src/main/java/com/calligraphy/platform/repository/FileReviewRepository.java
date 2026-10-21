package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.FileReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileReviewRepository extends JpaRepository<FileReview, String> {

    /**
     * 根据作品ID查询审核记录
     */
    Optional<FileReview> findByWorkId(Long workId);

    /**
     * 检查作品是否已有审核记录
     */
    boolean existsByWorkId(Long workId);

    /**
     * 根据审核状态分页查询
     */
    Page<FileReview> findByStatus(FileReview.FileReviewStatus status, Pageable pageable);

    /**
     * 根据审核员ID分页查询
     */
    Page<FileReview> findByReviewerId(String reviewerId, Pageable pageable);

    /**
     * 根据审核状态统计数量
     */
    long countByStatus(FileReview.FileReviewStatus status);

    /**
     * 获取最近审核的记录
     */
    List<FileReview> findTop10ByOrderByReviewTimeDesc();

    /**
     * 获取待审核的记录
     */
    List<FileReview> findByStatusOrderByCreatedAtAsc(FileReview.FileReviewStatus status);

    /**
     * 统计审核员的审核数量
     */
    @Query("SELECT COUNT(fr) FROM FileReview fr WHERE fr.reviewerId = :reviewerId")
    long countByReviewerId(@Param("reviewerId") String reviewerId);

    /**
     * 统计审核员在指定状态下的审核数量
     */
    @Query("SELECT COUNT(fr) FROM FileReview fr WHERE fr.reviewerId = :reviewerId AND fr.status = :status")
    long countByReviewerIdAndStatus(@Param("reviewerId") String reviewerId, @Param("status") FileReview.FileReviewStatus status);

    /**
     * 获取审核员最近的审核记录
     */
    List<FileReview> findTop10ByReviewerIdOrderByReviewTimeDesc(String reviewerId);
}


