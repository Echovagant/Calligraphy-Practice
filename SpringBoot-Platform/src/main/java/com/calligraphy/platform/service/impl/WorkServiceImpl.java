package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.WorkCreateRequestDto;
import com.calligraphy.platform.dto.WorkDto;
import com.calligraphy.platform.dto.WorkStatisticsDto;
import com.calligraphy.platform.dto.WorkUpdateRequestDto;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.BackendUser;

import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.mapper.WorkMapper;
import com.calligraphy.platform.repository.WorkRepository;
import com.calligraphy.platform.service.UserService;
import com.calligraphy.platform.service.WorkService;
import com.calligraphy.platform.repository.BackendUserRepository;
import com.calligraphy.platform.service.FileUploadService;
import com.calligraphy.platform.dto.FileUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// 类级只读事务：WorkMapper.toDto 会访问 work.getUser() / work.getReviewer() 这类
// LAZY 关联。映射发生在事务之外时会抛 LazyInitializationException。
// 写方法自己带 @Transactional（readOnly 默认 false），会覆盖类级设置。
@Transactional(readOnly = true)
public class WorkServiceImpl implements WorkService {

    private static final Logger log = LoggerFactory.getLogger(WorkServiceImpl.class);
    private final WorkRepository workRepository;
    private final UserService userService;
    private final WorkMapper workMapper;
    private final BackendUserRepository backendUserRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public WorkDto createWork(WorkCreateRequestDto createRequest, String username) {
        log.info("创建新作品: {} by {}", createRequest.getTitle(), username);

        User author = userService.findByUsername(username);

        Work work = Work.builder()
                .title(createRequest.getTitle())
                .description(createRequest.getDescription())
                .content(createRequest.getContent())
                .imageUrl(createRequest.getFileUrls() != null && !createRequest.getFileUrls().isEmpty()
                        ? createRequest.getFileUrls().get(0)
                        : null)
                .user(author)
                .userId(author.getId())
                .status(Work.WorkStatus.DRAFT)
                .reviewStatus(Work.ReviewStatus.PENDING)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build();

        Work savedWork = workRepository.save(work);
        log.info("作品创建成功: {} (ID: {})", savedWork.getTitle(), savedWork.getId());

        return workMapper.toDto(savedWork);
    }

    @Transactional
    public WorkDto updateWork(Long workId, WorkUpdateRequestDto updateRequest, String username) {
        log.info("更新作品: {} (ID: {})", updateRequest.getTitle(), workId);

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        // 检查权限 - 只有作者或管理员可以更新
        User currentUser = userService.findByUsername(username);
        if (!work.getUserId().equals(currentUser.getId()) &&
                backendUserRepository.findByUsername(username).isEmpty()) {
            throw new SecurityException("没有权限更新此作品");
        }

        // 更新基本信息
        if (updateRequest.getTitle() != null) {
            work.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getDescription() != null) {
            work.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getContent() != null) {
            work.setContent(updateRequest.getContent());
        }

        if (updateRequest.getFileUrls() != null && !updateRequest.getFileUrls().isEmpty()) {
            work.setImageUrl(updateRequest.getFileUrls().get(0));
        }

        Work updatedWork = workRepository.save(work);
        log.info("作品更新成功: {} (ID: {})", updatedWork.getTitle(), updatedWork.getId());

        return workMapper.toDto(updatedWork);
    }

    // ===== 兼容接口方法签名 =====
    @Override
    @Transactional
    public WorkDto createWork(String userId, com.calligraphy.platform.dto.WorkCreateDto createDto,
            MultipartFile[] files) {
        Work work = Work.builder()
                .title(createDto.getTitle())
                .description(createDto.getDescription())
                .content(createDto.getContent())
                .imageUrl(createDto.getImageUrl())
                .userId(userId)
                .status(Work.WorkStatus.DRAFT)
                .reviewStatus(Work.ReviewStatus.PENDING)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build();
        Work saved = workRepository.save(work);
        return workMapper.toDto(saved);
    }
    @Override
    @Transactional
    public WorkDto updateWork(Long workId, String userId, WorkUpdateRequestDto updateDto, MultipartFile[] files) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));
        if (updateDto.getTitle() != null)
            work.setTitle(updateDto.getTitle());
        if (updateDto.getDescription() != null)
            work.setDescription(updateDto.getDescription());
        if (updateDto.getContent() != null)
            work.setContent(updateDto.getContent());
        if (updateDto.getFileUrls() != null && !updateDto.getFileUrls().isEmpty())
            work.setImageUrl(updateDto.getFileUrls().get(0));
        Work updated = workRepository.save(work);
        return workMapper.toDto(updated);
    }
    @Override
    @Transactional
    public void deleteWork(Long workId, String username) {
        log.info("删除作品 (ID: {}) by {}", workId, username);

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        // 检查权限 - 只有作者或管理员可以删除
        User currentUser = userService.findByUsername(username);
        if (!work.getUserId().equals(currentUser.getId()) &&
                backendUserRepository.findByUsername(username).isEmpty()) {
            throw new SecurityException("没有权限删除此作品");
        }

        workRepository.delete(work);
        log.info("作品删除成功: {} (ID: {})", work.getTitle(), workId);
    }
    @Override
    public WorkDto getWorkById(Long workId) {
        log.info("获取作品详情: {}", workId);

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        // 增加浏览量
        work.incrementViewCount();
        workRepository.save(work);

        return workMapper.toDto(work);
    }

    public List<WorkDto> getWorksByUser(String username) {
        log.info("获取用户 {} 的所有作品", username);

        User author = userService.findByUsername(username);
        List<Work> works = workRepository.findByUserId(author.getId());

        return works.stream()
                .map(workMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<WorkDto> getAllWorks(Pageable pageable) {
        Page<Work> works = workRepository.findAll(pageable);
        return works.map(workMapper::toDto);
    }
    @Override
    public Page<WorkDto> getPublicWorks(Pageable pageable) {
        log.info("获取公开作品列表, 页码: {}, 每页数量: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Work> works = workRepository.findByStatusAndReviewStatus(
                Work.WorkStatus.PUBLISHED, Work.ReviewStatus.APPROVED, pageable);
        return works.map(workMapper::toDto);
    }
    @Override
    public Page<WorkDto> getApprovedWorks(Pageable pageable) {
        log.info("获取已批准作品列表: 页码: {}, 每页数量: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Work> works = workRepository.findByStatusAndReviewStatus(
                Work.WorkStatus.PUBLISHED, Work.ReviewStatus.APPROVED, pageable);
        return works.map(workMapper::toDto);
    }
    @Override
    public Page<WorkDto> getWorksByCategory(String category, Pageable pageable) {
        log.info("获取分类下 {} 的作品: 页码: {}, 每页数量: {}",
                category, pageable.getPageNumber(), pageable.getPageSize());
        // 由于Work实体没有category字段，这里返回所有作品
        List<Work> list = workRepository.findAll();
        List<WorkDto> dtos = list.stream().map(workMapper::toDto).collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, dtos.size());
    }

    public Page<WorkDto> searchWorks(String keyword, Pageable pageable) {
        log.info("搜索作品, 关键词: {}, 页码: {}, 每页数量: {}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());
        List<Work> list = workRepository.searchWorks(keyword);
        List<WorkDto> dtos = list.stream().map(workMapper::toDto).collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, dtos.size());
    }
    @Override
    public List<WorkDto> getAllWorks() {
        return workRepository.findAll().stream().map(workMapper::toDto).collect(Collectors.toList());
    }
    @Override
    public Page<WorkDto> getWorksByUserId(String userId, Pageable pageable) {
        List<WorkDto> dtos = workRepository.findByUserId(userId).stream().map(workMapper::toDto)
                .collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, dtos.size());
    }
    @Override
    public List<WorkDto> getWorksByUserId(String userId) {
        return workRepository.findByUserId(userId).stream().map(workMapper::toDto).collect(Collectors.toList());
    }
    @Override
    public Page<WorkDto> getWorksByStatus(Work.WorkStatus status, Pageable pageable) {
        List<WorkDto> dtos = workRepository.findByStatus(status).stream().map(workMapper::toDto)
                .collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, dtos.size());
    }
    @Override
    public List<WorkDto> getWorksByStatus(Work.WorkStatus status) {
        return workRepository.findByStatus(status).stream().map(workMapper::toDto).collect(Collectors.toList());
    }
    @Override
    public Page<WorkDto> getWorksByReviewStatus(Work.ReviewStatus reviewStatus, Pageable pageable) {
        List<WorkDto> dtos = workRepository.findByReviewStatus(reviewStatus).stream().map(workMapper::toDto)
                .collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, dtos.size());
    }
    @Override
    public List<WorkDto> getWorksByReviewStatus(Work.ReviewStatus reviewStatus) {
        return workRepository.findByReviewStatus(reviewStatus).stream().map(workMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<WorkDto> getPublicWorks() {
        return workRepository
                .findByStatusAndReviewStatus(Work.WorkStatus.PUBLISHED, Work.ReviewStatus.APPROVED,
                        org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .map(workMapper::toDto)
                .getContent();
    }
    @Override
    public Page<WorkDto> getFeaturedWorks(Pageable pageable) {
        List<WorkDto> dtos = getFeaturedWorks().stream().collect(Collectors.toList());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, dtos.size());
    }
    @Override
    @Transactional
    public WorkDto updateWorkStatus(Long workId, String userId, Work.WorkStatus status) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));
        work.setStatus(status);
        return workMapper.toDto(workRepository.save(work));
    }
    @Override
    @Transactional
    public WorkDto updateWorkReviewStatus(Long workId, String reviewerId, Work.ReviewStatus reviewStatus,
            String reviewComment) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));
        work.setReviewStatus(reviewStatus);
        work.setReviewNotes(reviewComment);
        work.setReviewTime(LocalDateTime.now());
        work.setReviewerId(reviewerId);
        return workMapper.toDto(workRepository.save(work));
    }
    @Override
    public Optional<Work> getWorkEntityById(Long workId) {
        return workRepository.findById(workId);
    }
    @Override
    public List<WorkDto> searchWorks(String keyword) {
        return workRepository.searchWorks(keyword).stream().map(workMapper::toDto).collect(Collectors.toList());
    }
    @Override
    public Page<WorkDto> getWorksByTags(List<String> tags, Pageable pageable) {
        List<WorkDto> dtos = java.util.Collections.emptyList();
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, 0);
    }
    @Override
    public List<WorkDto> getWorksByTags(List<String> tags) {
        return java.util.Collections.emptyList();
    }
    @Override
    public List<WorkDto> getWorksByCategory(String category) {
        // 由于Work实体没有category字段，这里返回所有作品
        return workRepository.findAll().stream().map(workMapper::toDto).collect(Collectors.toList());
    }
    @Override
    public long getWorkCountByUserId(String userId) {
        return workRepository.findByUserId(userId).size();
    }
    @Override
    public long getWorkCountByStatus(Work.WorkStatus status) {
        return workRepository.countByStatus(status);
    }
    @Override
    public long getWorkCountByReviewStatus(Work.ReviewStatus reviewStatus) {
        return workRepository.countByReviewStatus(reviewStatus);
    }
    @Override
    @Transactional
    public void incrementViewCount(Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        work.setViewCount(work.getViewCount() + 1);
        workRepository.save(work);
    }
    @Override
    @Transactional
    public WorkDto likeWork(Long workId, String username) {
        log.info("用户 {} 点赞作品 ID: {}", username, workId);

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        // 验证用户存在
        User user = userService.findByUsername(username);

        // 增加点赞数
        work.incrementLikeCount();
        Work updatedWork = workRepository.save(work);

        log.info("作品点赞成功: {} (ID: {}), 点赞数: {}",
                updatedWork.getTitle(), updatedWork.getId(), updatedWork.getLikeCount());

        return workMapper.toDto(updatedWork);
    }
    @Override
    @Transactional
    public void decrementLikeCount(Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        if (work.getLikeCount() > 0) {
            work.setLikeCount(work.getLikeCount() - 1);
            workRepository.save(work);
        }
    }
    @Override
    @Transactional
    public void incrementLikeCount(Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        work.incrementLikeCount();
        workRepository.save(work);
    }
    @Override
    @Transactional
    public void addLike(Long workId, String userId) {
        // 由于当前系统没有点赞记录表，暂时只增加点赞数
        incrementLikeCount(workId);
    }
    @Override
    @Transactional
    public void removeLike(Long workId, String userId) {
        // 由于当前系统没有点赞记录表，暂时只减少点赞数
        decrementLikeCount(workId);
    }
    @Override
    @Transactional
    public void updateCommentCount(Long workId, int delta) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        work.setCommentCount(work.getCommentCount() + delta);
        workRepository.save(work);
    }

    @Transactional
    public WorkDto approveWork(Long workId, String adminUsername) {
        log.info("管理员:{} 批准作品 ID: {}", adminUsername, workId);

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        BackendUser reviewer = backendUserRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new SecurityException("管理员不存在或无权限: " + adminUsername));

        work.approve(reviewer, "审核通过");
        Work updated = workRepository.save(work);
        return workMapper.toDto(updated);
    }

    @Transactional
    public WorkDto rejectWork(Long workId, String adminUsername, String reason) {
        log.info("管理员:{} 拒绝作品 ID: {}", adminUsername, workId);

        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        BackendUser reviewer = backendUserRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new SecurityException("管理员不存在或无权限: " + adminUsername));

        work.reject(reviewer, reason != null ? reason : "审核拒绝");
        Work updated = workRepository.save(work);
        return workMapper.toDto(updated);
    }

    public List<WorkDto> getWorksByStatus(String status) {
        log.info("获取状态为 {} 的作品", status);

        try {
            Work.WorkStatus workStatus = Work.WorkStatus.valueOf(status.toUpperCase());
            List<Work> works = workRepository.findByStatus(workStatus);

            return works.stream()
                    .map(workMapper::toDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("无效的作品状态: {}", status);
            throw new IllegalArgumentException("无效的作品状态: " + status);
        }
    }
    @Override
    public long getTotalWorkCount() {
        return workRepository.count();
    }
    @Override
    public long getPublicWorkCount() {
        log.info("获取公开作品数量");
        return workRepository.countByStatusAndReviewStatus(Work.WorkStatus.PUBLISHED, Work.ReviewStatus.APPROVED);
    }
    @Override
    public long countByStatusAndReviewStatus(Work.WorkStatus status, Work.ReviewStatus reviewStatus) {
        log.info("获取状态为{}且审核状态为{}的作品数量", status, reviewStatus);
        return workRepository.countByStatusAndReviewStatus(status, reviewStatus);
    }
    @Override
    public long getApprovedWorkCount() {
        log.info("获取已批准作品数量");
        return workRepository.countByReviewStatus(Work.ReviewStatus.APPROVED);
    }

    public long getPendingWorkCount() {
        log.info("获取待审核作品数量");
        return workRepository.countByReviewStatus(Work.ReviewStatus.PENDING);
    }
    @Override
    public List<WorkDto> getFeaturedWorks() {
        log.info("获取推荐作品列表");
        List<WorkDto> dtos = workRepository.findAll().stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsFeatured()))
                .map(workMapper::toDto)
                .collect(Collectors.toList());
        return dtos;
    }

    public WorkStatisticsDto getWorkStatistics() {
        log.info("获取作品统计信息");

        long totalWorks = workRepository.count();
        long publishedWorks = workRepository.countByStatus(Work.WorkStatus.PUBLISHED);
        long draftWorks = workRepository.countByStatus(Work.WorkStatus.DRAFT);
        long hiddenWorks = workRepository.countByStatus(Work.WorkStatus.HIDDEN);
        long pendingReviewWorks = workRepository.countByReviewStatus(Work.ReviewStatus.PENDING);
        long approvedWorks = workRepository.countByReviewStatus(Work.ReviewStatus.APPROVED);
        long rejectedWorks = workRepository.countByReviewStatus(Work.ReviewStatus.REJECTED);

        return WorkStatisticsDto.builder()
                .totalWorks(totalWorks)
                .publishedWorks(publishedWorks)
                .draftWorks(draftWorks)
                .hiddenWorks(hiddenWorks)
                .pendingWorks(pendingReviewWorks)
                .approvedWorks(approvedWorks)
                .rejectedWorks(rejectedWorks)
                .build();
    }
    @Override
    public List<String> getWorkTags(Long workId) {
        return java.util.Collections.emptyList();
    }
    @Override
    @Transactional
    public WorkDto addTagToWork(Long workId, String tag) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));
        Work updatedWork = workRepository.save(work);
        return workMapper.toDto(updatedWork);
    }
    @Override
    @Transactional
    public WorkDto removeTagFromWork(Long workId, String tag) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));
        Work updatedWork = workRepository.save(work);
        return workMapper.toDto(updatedWork);
    }
    @Override
    public List<String> getWorkFileUrls(Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        // 假设imageUrl字段存储主要文件URL
        return work.getImageUrl() != null ? List.of(work.getImageUrl()) : List.of();
    }
    @Override
    @Transactional
    public WorkDto addFileToWork(Long workId, MultipartFile file) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        String username = work.getUser() != null ? work.getUser().getUsername() : null;
        FileUploadResponseDto uploaded = fileUploadService.uploadWorkFile(file, username);
        work.setImageUrl(uploaded.getFileUrl());
        Work updated = workRepository.save(work);
        return workMapper.toDto(updated);
    }
    @Override
    @Transactional
    public WorkDto removeFileFromWork(Long workId, String fileUrl) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));

        if (work.getImageUrl() != null && work.getImageUrl().equals(fileUrl)) {
            work.setImageUrl(null);
        }

        Work updatedWork = workRepository.save(work);
        return workMapper.toDto(updatedWork);
    }
    @Override
    public List<WorkDto> getWorksPendingReview() {
        List<Work> works = workRepository.findWorksNeedingReview(Work.ReviewStatus.PENDING,
                Work.ReviewStatus.NEEDS_REVISION);
        return works.stream()
                .map(workMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<WorkDto> getPendingReviewWorks(Pageable pageable) {
        return workRepository
                .findWorksNeedingReview(Work.ReviewStatus.PENDING, Work.ReviewStatus.NEEDS_REVISION, pageable)
                .map(workMapper::toDto);
    }
    @Override
    public List<WorkDto> getWorksByReviewerId(String reviewerId) {
        List<Work> works = workRepository.findByReviewerId(reviewerId);
        return works.stream().map(workMapper::toDto).collect(java.util.stream.Collectors.toList());
    }
    @Override
    public long getPendingReviewCount() {
        return workRepository.countByReviewStatus(Work.ReviewStatus.PENDING);
    }
    @Override
    public List<WorkDto> getTopLikedWorks(int limit) {
        List<Work> works = workRepository.findAllOrderByLikeCountDesc();
        return works.stream()
                .limit(limit)
                .map(workMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<WorkDto> getTopViewedWorks(int limit) {
        List<Work> works = workRepository.findAllOrderByViewCountDesc();
        return works.stream()
                .limit(limit)
                .map(workMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<WorkDto> getRecentWorks(int limit) {
        List<Work> works = workRepository.findAllOrderByCreatedAtDesc();
        return works.stream()
                .limit(limit)
                .map(workMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void bulkDeleteWorks(List<Long> workIds) {
        for (Long workId : workIds) {
            try {
                deleteWork(workId, null); // 使用null作为用户ID，表示系统操作
            } catch (Exception e) {
                log.warn("删除作品 {} 失败: {}", workId, e.getMessage());
            }
        }
    }
    @Override
    @Transactional
    public void bulkUpdateStatus(List<Long> workIds, Work.WorkStatus status) {
        for (Long workId : workIds) {
            try {
                updateWorkStatus(workId, null, status); // 使用null作为用户ID，表示系统操作
            } catch (Exception e) {
                log.warn("更新作品 {} 状态失败: {}", workId, e.getMessage());
            }
        }
    }
    @Override
    @Transactional
    public void bulkUpdateReviewStatus(List<Long> workIds, Work.ReviewStatus reviewStatus, String reviewerId) {
        for (Long workId : workIds) {
            try {
                updateWorkReviewStatus(workId, reviewerId, reviewStatus, "批量审核");
            } catch (Exception e) {
                log.warn("更新作品 {} 审核状态失败: {}", workId, e.getMessage());
            }
        }
    }
}


