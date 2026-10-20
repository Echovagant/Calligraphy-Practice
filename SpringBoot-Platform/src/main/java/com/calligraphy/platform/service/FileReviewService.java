package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.FileReviewRequestDto;
import com.calligraphy.platform.dto.FileReviewResponseDto;
import com.calligraphy.platform.dto.FileReviewUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface FileReviewService {
    
    /**
     * 鍒涘缓鏂囦欢瀹℃牳璁板綍
     */
    FileReviewResponseDto createReview(FileReviewRequestDto requestDto, String adminId);

    /**
     * 鏇存柊瀹℃牳璁板綍
     */
    FileReviewResponseDto updateReview(String reviewId, FileReviewUpdateDto updateDto, String adminId);

    /**
     * 鑾峰彇瀹℃牳璁板綍璇︽儏
     */
    FileReviewResponseDto getReviewById(String reviewId);

    /**
     * 鑾峰彇浣滃搧鐨勫鏍歌褰?     */
    FileReviewResponseDto getReviewByWorkId(Long workId);

    /**
     * 鍒嗛〉鑾峰彇鎵€鏈夊鏍歌褰?     */
    Page<FileReviewResponseDto> getAllReviews(Pageable pageable);

    /**
     * 鏍规嵁瀹℃牳鐘舵€佸垎椤佃幏鍙栬褰?     */
    Page<FileReviewResponseDto> getReviewsByStatus(String reviewStatus, Pageable pageable);

    /**
     * 鑾峰彇绠＄悊鍛樼殑瀹℃牳璁板綍
     */
    Page<FileReviewResponseDto> getReviewsByReviewer(String adminId, Pageable pageable);

    /**
     * 鍒犻櫎瀹℃牳璁板綍
     */
    void deleteReview(String reviewId, String adminId);

    /**
     * 鎵归噺瀹℃牳浣滃搧
     */
    List<FileReviewResponseDto> batchReviewWorks(List<Long> workIds, String reviewStatus, 
                                                        String reviewComments, String adminId);

    /**
     * 鑾峰彇寰呭核的作品数量
     */
    long getPendingReviewCount();

    /**
     * 鑾峰彇瀹℃牳缁熻淇℃伅
     */
    Map<String, Long> getReviewStatistics();

    /**
     * 妫€鏌ヤ綔鍝佹槸鍚﹀凡琚鏍?     */
    boolean isWorkReviewed(Long workId);
}


