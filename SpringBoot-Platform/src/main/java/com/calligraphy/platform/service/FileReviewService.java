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
     * 创建文件审核记录
     */
    FileReviewResponseDto createReview(FileReviewRequestDto requestDto, String adminId);

    /**
     * 更新审核记录
     */
    FileReviewResponseDto updateReview(String reviewId, FileReviewUpdateDto updateDto, String adminId);

    /**
     * 获取审核记录详情
     */
    FileReviewResponseDto getReviewById(String reviewId);

    /**
     * 获取作品的审核记录
     */
    FileReviewResponseDto getReviewByWorkId(Long workId);

    /**
     * 分页获取所有审核记录
     */
    Page<FileReviewResponseDto> getAllReviews(Pageable pageable);

    /**
     * 根据审核状态分页获取记录
     */
    Page<FileReviewResponseDto> getReviewsByStatus(String reviewStatus, Pageable pageable);

    /**
     * 获取管理员的审核记录
     */
    Page<FileReviewResponseDto> getReviewsByReviewer(String adminId, Pageable pageable);

    /**
     * 删除审核记录
     */
    void deleteReview(String reviewId, String adminId);

    /**
     * 批量审核作品
     */
    List<FileReviewResponseDto> batchReviewWorks(List<Long> workIds, String reviewStatus, 
                                                        String reviewComments, String adminId);

    /**
     * 获取待审核的作品数量
     */
    long getPendingReviewCount();

    /**
     * 获取审核统计信息
     */
    Map<String, Long> getReviewStatistics();

    /**
     * 检查作品是否已被审核
     */
    boolean isWorkReviewed(Long workId);
}


