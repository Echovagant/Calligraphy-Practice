package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.WorkDto;
import com.calligraphy.platform.dto.WorkCreateDto;
import com.calligraphy.platform.dto.WorkCreateRequestDto;
import com.calligraphy.platform.dto.WorkUpdateRequestDto;
import com.calligraphy.platform.dto.FileUploadResponseDto;
import com.calligraphy.platform.model.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface WorkService {

    WorkDto createWork(String userId, WorkCreateDto createDto, MultipartFile[] files);

    /**
     * 按用户名创建作品 —— 作者从 users 表查出来并**设置关联**
     * （works.user_id 由 @ManyToOne 关联独占写入，只传 id 字符串会写成 NULL）。
     *
     * @param createRequest 作品内容
     * @param username 作者用户名
     * @return 创建后的作品
     */
    WorkDto createWork(WorkCreateRequestDto createRequest, String username);

    /**
     * 按用户名更新作品（带权限校验：只有作者或管理员能改）。
     *
     * @param workId 作品 id
     * @param updateRequest 更新内容
     * @param username 操作者用户名
     * @return 更新后的作品
     */
    WorkDto updateWork(Long workId, WorkUpdateRequestDto updateRequest, String username);
    
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

    void addLike(Long workId, String userId);
    
    void removeLike(Long workId, String userId);
    
    // 搜索和筛选
    List<String> getWorkTags(Long workId);
    
    WorkDto addTagToWork(Long workId, String tag);
    
    WorkDto removeTagFromWork(Long workId, String tag);
    
    List<String> getWorkFileUrls(Long workId);
    
    /**
     * 换作品主图：**上传 + 落库一次做完**，返回真正入库的那个文件。
     *
     * <p>只上传一次是关键：以前 Controller 先传一遍、Service 内部又传一遍，
     * 磁盘上留下两份文件，而且响应里的 fileUrl 指向的是第一份（不是作品主图）。
     *
     * @param workId 作品 id
     * @param file 新主图文件
     * @param username 操作者用户名（用于归属校验与文件归属目录）
     * @return 上传结果，{@code fileUrl} 与作品的 imageUrl 完全一致
     */
    FileUploadResponseDto addFileToWork(Long workId, MultipartFile file, String username);
    
    WorkDto removeFileFromWork(Long workId, String fileUrl);
    
    List<WorkDto> getWorksPendingReview();

    /**
     * 待审核作品（分页）—— 后台「作品审核」列表用。
     *
     * <p>待审核 = review_status 为 PENDING（还没审）或 NEEDS_REVISION（作者改过、等复审），
     * 与实体 {@code Work.canBeReviewed()} 一致。
     *
     * <p>公开画廊只返回已发布且已审核通过的作品，所以后台要看到待审队列
     * **必须**有这个查询 —— 少了它审核功能等于废的。
     */
    Page<WorkDto> getPendingReviewWorks(Pageable pageable);

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

    long getApprovedWorkCount();
    
    long getPublicWorkCount();
    
    long countByStatusAndReviewStatus(Work.WorkStatus status, Work.ReviewStatus reviewStatus);
    void updateCommentCount(Long workId, int delta);
}


