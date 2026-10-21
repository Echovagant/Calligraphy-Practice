package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.WorkDto;
import com.calligraphy.platform.dto.WorkCreateDto;
import com.calligraphy.platform.dto.WorkUpdateRequestDto;
import com.calligraphy.platform.model.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface WorkService {
    
    WorkDto createWork(String userId, WorkCreateDto createDto, MultipartFile[] files);
    
    WorkDto getWorkById(Long workId);
    
    Optional<Work> getWorkEntityById(Long workId);
    
    Page<WorkDto> getAllWorks(Pageable pageable);
    
    List<WorkDto> getAllWorks();
    
    Page<WorkDto> getWorksByUserId(String userId, Pageable pageable);
    
    List<WorkDto> getWorksByUserId(String userId);
    
    Page<WorkDto> getWorksByStatus(Work.WorkStatus status, Pageable pageable);
    
    List<WorkDto> getWorksByStatus(Work.WorkStatus status);
    
    Page<WorkDto> getWorksByReviewStatus(Work.ReviewStatus reviewStatus, Pageable pageable);
    
    List<WorkDto> getWorksByReviewStatus(Work.ReviewStatus reviewStatus);
    
    Page<WorkDto> getPublicWorks(Pageable pageable);
    
    List<WorkDto> getPublicWorks();
    
    Page<WorkDto> getFeaturedWorks(Pageable pageable);
    
    List<WorkDto> getFeaturedWorks();
    
    WorkDto updateWork(Long workId, String userId, WorkUpdateRequestDto updateDto, MultipartFile[] files);
    
    WorkDto updateWorkStatus(Long workId, String userId, Work.WorkStatus status);
    
    WorkDto updateWorkReviewStatus(Long workId, String reviewerId, Work.ReviewStatus reviewStatus, String reviewComment);
    
    void deleteWork(Long workId, String userId);
    
    void incrementViewCount(Long workId);
    
    void incrementLikeCount(Long workId);
    
    void decrementLikeCount(Long workId);
    
    boolean isLikedByUser(Long workId, String userId);
    
    void addLike(Long workId, String userId);
    
    void removeLike(Long workId, String userId);
    
    // 搜索和筛选
    List<String> getWorkTags(Long workId);
    
    WorkDto addTagToWork(Long workId, String tag);
    
    WorkDto removeTagFromWork(Long workId, String tag);
    
    List<String> getWorkFileUrls(Long workId);
    
    WorkDto addFileToWork(Long workId, MultipartFile file);
    
    WorkDto removeFileFromWork(Long workId, String fileUrl);
    
    List<WorkDto> getWorksPendingReview();
    
    List<WorkDto> getWorksByReviewerId(String reviewerId);
    
    long getPendingReviewCount();
    
    List<WorkDto> getTopLikedWorks(int limit);
    
    List<WorkDto> getTopViewedWorks(int limit);
    
    List<WorkDto> getRecentWorks(int limit);
    
    void bulkDeleteWorks(List<Long> workIds);
    
    void bulkUpdateStatus(List<Long> workIds, Work.WorkStatus status);
    
    void bulkUpdateReviewStatus(List<Long> workIds, Work.ReviewStatus reviewStatus, String reviewerId);
    
    List<WorkDto> searchWorks(String keyword);
    
    Page<WorkDto> getWorksByTags(List<String> tags, Pageable pageable);
    
    List<WorkDto> getWorksByTags(List<String> tags);
    
    Page<WorkDto> getWorksByCategory(String category, Pageable pageable);
    
    WorkDto likeWork(Long workId, String username);
    
    List<WorkDto> getWorksByCategory(String category);
    
    Page<WorkDto> getApprovedWorks(Pageable pageable);
    
    // 统计相关
    long getTotalWorkCount();
    
    long getWorkCountByUserId(String userId);
    
    long getWorkCountByStatus(Work.WorkStatus status);
    
    long getWorkCountByReviewStatus(Work.ReviewStatus reviewStatus);
    
    long getWorkCountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    long getApprovedWorkCount();
    
    long getPublicWorkCount();
    
    long countByStatusAndReviewStatus(Work.WorkStatus status, Work.ReviewStatus reviewStatus);
    void updateCommentCount(Long workId, int delta);
}


