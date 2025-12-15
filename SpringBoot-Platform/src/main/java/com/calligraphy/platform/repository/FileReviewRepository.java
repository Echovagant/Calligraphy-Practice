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
     * 鏍规嵁浣滃搧ID鏌ヨ瀹℃牳璁板綍
     */
    Optional<FileReview> findByWorkId(Long workId);

    /**
     * 妫€鏌ヤ綔鍝佹槸鍚﹀凡鏈夊鏍歌褰?     */
    boolean existsByWorkId(Long workId);

    /**
     * 鏍规嵁瀹℃牳鐘舵€佸垎椤垫煡璇?     */
    Page<FileReview> findByStatus(FileReview.FileReviewStatus status, Pageable pageable);

    /**
     * 鏍规嵁瀹℃牳鍛業D鍒嗛〉鏌ヨ
     */
    Page<FileReview> findByReviewerId(String reviewerId, Pageable pageable);

    /**
     * 鏍规嵁瀹℃牳鐘舵€佺粺璁℃暟閲?     */
    long countByStatus(FileReview.FileReviewStatus status);

    /**
     * 鑾峰彇鏈€杩戝鏍哥殑璁板綍
     */
    List<FileReview> findTop10ByOrderByReviewTimeDesc();

    /**
     * 鑾峰彇寰呭鏍哥殑璁板綍
     */
    List<FileReview> findByStatusOrderByCreatedAtAsc(FileReview.FileReviewStatus status);

    /**
     * 缁熻瀹℃牳鍛樼殑瀹℃牳鏁伴噺
     */
    @Query("SELECT COUNT(fr) FROM FileReview fr WHERE fr.reviewerId = :reviewerId")
    long countByReviewerId(@Param("reviewerId") String reviewerId);

    /**
     * 缁熻瀹℃牳鍛樺湪鎸囧畾鐘舵€佷笅鐨勫鏍告暟閲?     */
    @Query("SELECT COUNT(fr) FROM FileReview fr WHERE fr.reviewerId = :reviewerId AND fr.status = :status")
    long countByReviewerIdAndStatus(@Param("reviewerId") String reviewerId, @Param("status") FileReview.FileReviewStatus status);

    /**
     * 鑾峰彇瀹℃牳鍛樻渶杩戠殑瀹℃牳璁板綍
     */
    List<FileReview> findTop10ByReviewerIdOrderByReviewTimeDesc(String reviewerId);
}


