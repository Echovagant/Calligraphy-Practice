package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.FileReviewRequestDto;
import com.calligraphy.platform.dto.FileReviewResponseDto;
import com.calligraphy.platform.dto.FileReviewUpdateDto;
import com.calligraphy.platform.model.FileReview;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.model.BackendUser;
import com.calligraphy.platform.mapper.FileReviewMapper;
import com.calligraphy.platform.repository.FileReviewRepository;
import com.calligraphy.platform.repository.WorkRepository;
import com.calligraphy.platform.repository.BackendUserRepository;
import com.calligraphy.platform.service.FileReviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileReviewServiceImpl implements FileReviewService {

    private static final Logger log = LoggerFactory.getLogger(FileReviewServiceImpl.class);

    private final FileReviewRepository fileReviewRepository;
    private final WorkRepository workRepository;
    private final BackendUserRepository backendUserRepository;
    private final FileReviewMapper fileReviewMapper;
    @Override
    @Transactional
    public FileReviewResponseDto createReview(FileReviewRequestDto requestDto, String adminId) {
        log.info("创建文件审核记录 - 作品ID: {}, 审核员ID: {}", requestDto.getWorkId(), adminId);

        // 验证作品存在
        Work work = workRepository.findById(requestDto.getWorkId())
                .orElseThrow(() -> new EntityNotFoundException("作品不存在"));

        // 验证审核员存在
        BackendUser reviewer = backendUserRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("审核员不存在"));

        // 检查是否已存在审核记录
        if (fileReviewRepository.existsByWorkId(requestDto.getWorkId())) {
            throw new IllegalStateException("该作品已有审核记录");
        }

        // 创建审核记录
        FileReview fileReview = new FileReview();
        fileReview.setWork(work);
        fileReview.setReviewer(reviewer);
        fileReview.setStatus(FileReview.FileReviewStatus.valueOf(requestDto.getReviewStatus()));
        fileReview.setReviewNotes(requestDto.getReviewComments());
        fileReview.setReviewTime(LocalDateTime.now());
        fileReview.setCreatedAt(LocalDateTime.now());
        fileReview.setUpdatedAt(LocalDateTime.now());

        FileReview savedReview = fileReviewRepository.save(fileReview);

        // 更新作品状态
        updateWorkStatus(work, requestDto.getReviewStatus());

        log.info("文件审核记录创建成功 - ID: {}", savedReview.getId());
        return fileReviewMapper.toDto(savedReview);
    }
    @Override
    @Transactional
    public FileReviewResponseDto updateReview(String reviewId, FileReviewUpdateDto updateDto, String adminId) {
        log.info("更新审核记录 - ID: {}, 审核员ID: {}", reviewId, adminId);

        FileReview fileReview = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("审核记录不存在"));

        // 验证审核员权限
        if (!fileReview.getReviewer().getId().equals(adminId)) {
            throw new SecurityException("只能更新自己的审核记录");
        }

        // 更新审核信息
        fileReview.setStatus(FileReview.FileReviewStatus.valueOf(updateDto.getReviewStatus()));
        fileReview.setReviewNotes(updateDto.getReviewComments());
        fileReview.setReviewTime(LocalDateTime.now());
        fileReview.setUpdatedAt(LocalDateTime.now());

        FileReview updatedReview = fileReviewRepository.save(fileReview);

        // 更新作品状态
        updateWorkStatus(fileReview.getWork(), updateDto.getReviewStatus());

        log.info("审核记录更新成功 - ID: {}", reviewId);
        return fileReviewMapper.toDto(updatedReview);
    }
    @Override
    public FileReviewResponseDto getReviewById(String reviewId) {
        log.info("获取审核记录详情 - ID: {}", reviewId);

        FileReview fileReview = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("审核记录不存在"));

        return fileReviewMapper.toDto(fileReview);
    }
    @Override
    public FileReviewResponseDto getReviewByWorkId(Long workId) {
        log.info("获取作品的审核记录 - 作品ID: {}", workId);

        FileReview fileReview = fileReviewRepository.findByWorkId(workId)
                .orElseThrow(() -> new EntityNotFoundException("该作品暂无审核记录"));

        return fileReviewMapper.toDto(fileReview);
    }
    @Override
    public Page<FileReviewResponseDto> getAllReviews(Pageable pageable) {
        log.info("分页获取所有审核记录 - 页码: {}, 大小: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<FileReview> reviews = fileReviewRepository.findAll(pageable);
        return reviews.map(fileReviewMapper::toDto);
    }
    @Override
    public Page<FileReviewResponseDto> getReviewsByStatus(String reviewStatus, Pageable pageable) {
        log.info("根据审核状态获取记录 - 状态: {}, 页码: {}, 大小: {}",
                reviewStatus, pageable.getPageNumber(), pageable.getPageSize());

        Page<FileReview> reviews = fileReviewRepository.findByStatus(FileReview.FileReviewStatus.valueOf(reviewStatus),
                pageable);
        return reviews.map(fileReviewMapper::toDto);
    }
    @Override
    public Page<FileReviewResponseDto> getReviewsByReviewer(String adminId, Pageable pageable) {
        log.info("获取管理员的审核记录 - 审核员ID: {}, 页码: {}, 大小: {}",
                adminId, pageable.getPageNumber(), pageable.getPageSize());

        Page<FileReview> reviews = fileReviewRepository.findByReviewerId(adminId, pageable);
        return reviews.map(fileReviewMapper::toDto);
    }
    @Override
    @Transactional
    public void deleteReview(String reviewId, String adminId) {
        log.info("删除审核记录 - ID: {}, 审核员ID: {}", reviewId, adminId);

        FileReview fileReview = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("审核记录不存在"));

        // 验证审核员权限
        if (!fileReview.getReviewer().getId().equals(adminId)) {
            throw new SecurityException("只能删除自己的审核记录");
        }

        fileReviewRepository.delete(fileReview);
        log.info("审核记录删除成功 - ID: {}", reviewId);
    }
    @Override
    @Transactional
    public List<FileReviewResponseDto> batchReviewWorks(List<Long> workIds, String reviewStatus, String reviewComments,
            String adminId) {
        log.info("批量审核作品 - 作品数量: {}, 状态: {}, 审核员ID: {}", workIds.size(), reviewStatus, adminId);

        return workIds.stream()
                .map(workId -> {
                    try {
                        FileReviewRequestDto requestDto = new FileReviewRequestDto();
                        requestDto.setWorkId(workId);
                        requestDto.setReviewStatus(reviewStatus);
                        requestDto.setReviewComments(reviewComments);

                        return createReview(requestDto, adminId);
                    } catch (Exception e) {
                        log.error("批量审核作品失败 - 作品ID: {}, 错误: {}", workId, e.getMessage());
                        return null;
                    }
                })
                .filter(review -> review != null)
                .collect(Collectors.toList());
    }
    @Override
    public long getPendingReviewCount() {
        log.info("获取待审核的作品数量");
        return fileReviewRepository.countByStatus(FileReview.FileReviewStatus.PENDING);
    }
    @Override
    public Map<String, Long> getReviewStatistics() {
        log.info("获取审核统计信息");

        Map<String, Long> statistics = Map.of(
                "total", fileReviewRepository.count(),
                "pending", fileReviewRepository.countByStatus(FileReview.FileReviewStatus.PENDING),
                "approved", fileReviewRepository.countByStatus(FileReview.FileReviewStatus.APPROVED),
                "rejected", fileReviewRepository.countByStatus(FileReview.FileReviewStatus.REJECTED));

        return statistics;
    }
    @Override
    public boolean isWorkReviewed(Long workId) {
        log.info("检查作品是否已审核 - 作品ID: {}", workId);
        return fileReviewRepository.existsByWorkId(workId);
    }

    /**
     * 更新作品状态      */
    private void updateWorkStatus(Work work, String reviewStatus) {
        switch (reviewStatus.toUpperCase()) {
            case "APPROVED":
                work.setStatus(Work.WorkStatus.PUBLISHED);
                break;
            case "REJECTED":
                work.setStatus(Work.WorkStatus.HIDDEN);
                break;
            case "NEEDS_REVISION":
                work.setStatus(Work.WorkStatus.UNDER_REVIEW);
                break;
            default:
                // 保持原有状态
                break;
        }
        work.setUpdatedAt(LocalDateTime.now());
        workRepository.save(work);
        log.info("作品状态已更新 - ID: {}, 状态: {}", work.getId(), reviewStatus);
    }
}


