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
        log.info("鍒涘缓鏂囦欢瀹℃牳璁板綍 - 作品ID: {}, 瀹℃牳鍛業D: {}", requestDto.getWorkId(), adminId);

        // 验证作品存在
        Work work = workRepository.findById(requestDto.getWorkId())
                .orElseThrow(() -> new EntityNotFoundException("浣滃搧涓嶅瓨鍦"));

        // 楠岃瘉瀹℃牳鍛樺瓨鍦
        BackendUser reviewer = backendUserRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("瀹℃牳鍛樹笉瀛樺湪"));

        // 妫€鏌ユ槸鍚﹀凡瀛樺湪瀹℃牳璁板綍
        if (fileReviewRepository.existsByWorkId(requestDto.getWorkId())) {
            throw new IllegalStateException("璇ヤ綔鍝佸凡鏈夊鏍歌褰");
        }

        // 鍒涘缓瀹℃牳璁板綍
        FileReview fileReview = new FileReview();
        fileReview.setWork(work);
        fileReview.setReviewer(reviewer);
        fileReview.setStatus(FileReview.FileReviewStatus.valueOf(requestDto.getReviewStatus()));
        fileReview.setReviewNotes(requestDto.getReviewComments());
        fileReview.setReviewTime(LocalDateTime.now());
        fileReview.setCreatedAt(LocalDateTime.now());
        fileReview.setUpdatedAt(LocalDateTime.now());

        FileReview savedReview = fileReviewRepository.save(fileReview);

        // 鏇存柊浣滃搧鐘舵€
        updateWorkStatus(work, requestDto.getReviewStatus());

        log.info("鏂囦欢瀹℃牳璁板綍鍒涘缓鎴愬姛 - ID: {}", savedReview.getId());
        return fileReviewMapper.toDto(savedReview);
    }
    @Override
    @Transactional
    public FileReviewResponseDto updateReview(String reviewId, FileReviewUpdateDto updateDto, String adminId) {
        log.info("鏇存柊瀹℃牳璁板綍 - ID: {}, 瀹℃牳鍛業D: {}", reviewId, adminId);

        FileReview fileReview = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("瀹℃牳璁板綍涓嶅瓨鍦"));

        // 楠岃瘉瀹℃牳鍛樻潈闄
        if (!fileReview.getReviewer().getId().equals(adminId)) {
            throw new SecurityException("鍙兘鏇存柊鑷繁鐨勫鏍歌褰");
        }

        // 鏇存柊瀹℃牳淇℃伅
        fileReview.setStatus(FileReview.FileReviewStatus.valueOf(updateDto.getReviewStatus()));
        fileReview.setReviewNotes(updateDto.getReviewComments());
        fileReview.setReviewTime(LocalDateTime.now());
        fileReview.setUpdatedAt(LocalDateTime.now());

        FileReview updatedReview = fileReviewRepository.save(fileReview);

        // 鏇存柊浣滃搧鐘舵€
        updateWorkStatus(fileReview.getWork(), updateDto.getReviewStatus());

        log.info("瀹℃牳璁板綍鏇存柊鎴愬姛 - ID: {}", reviewId);
        return fileReviewMapper.toDto(updatedReview);
    }
    @Override
    public FileReviewResponseDto getReviewById(String reviewId) {
        log.info("鑾峰彇瀹℃牳璁板綍璇︽儏 - ID: {}", reviewId);

        FileReview fileReview = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("瀹℃牳璁板綍涓嶅瓨鍦"));

        return fileReviewMapper.toDto(fileReview);
    }
    @Override
    public FileReviewResponseDto getReviewByWorkId(Long workId) {
        log.info("鑾峰彇浣滃搧鐨勫鏍歌褰?- 作品ID: {}", workId);

        FileReview fileReview = fileReviewRepository.findByWorkId(workId)
                .orElseThrow(() -> new EntityNotFoundException("璇ヤ綔鍝佹殏鏃犲鏍歌褰"));

        return fileReviewMapper.toDto(fileReview);
    }
    @Override
    public Page<FileReviewResponseDto> getAllReviews(Pageable pageable) {
        log.info("鍒嗛〉鑾峰彇鎵€鏈夊鏍歌褰?- 页码: {}, 澶у皬: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<FileReview> reviews = fileReviewRepository.findAll(pageable);
        return reviews.map(fileReviewMapper::toDto);
    }
    @Override
    public Page<FileReviewResponseDto> getReviewsByStatus(String reviewStatus, Pageable pageable) {
        log.info("鏍规嵁瀹℃牳鐘舵€佽幏鍙栬褰?- 鐘舵€? {}, 页码: {}, 澶у皬: {}",
                reviewStatus, pageable.getPageNumber(), pageable.getPageSize());

        Page<FileReview> reviews = fileReviewRepository.findByStatus(FileReview.FileReviewStatus.valueOf(reviewStatus),
                pageable);
        return reviews.map(fileReviewMapper::toDto);
    }
    @Override
    public Page<FileReviewResponseDto> getReviewsByReviewer(String adminId, Pageable pageable) {
        log.info("鑾峰彇绠＄悊鍛樼殑瀹℃牳璁板綍 - 瀹℃牳鍛業D: {}, 页码: {}, 澶у皬: {}",
                adminId, pageable.getPageNumber(), pageable.getPageSize());

        Page<FileReview> reviews = fileReviewRepository.findByReviewerId(adminId, pageable);
        return reviews.map(fileReviewMapper::toDto);
    }
    @Override
    @Transactional
    public void deleteReview(String reviewId, String adminId) {
        log.info("鍒犻櫎瀹℃牳璁板綍 - ID: {}, 瀹℃牳鍛業D: {}", reviewId, adminId);

        FileReview fileReview = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("瀹℃牳璁板綍涓嶅瓨鍦"));

        // 楠岃瘉瀹℃牳鍛樻潈闄
        if (!fileReview.getReviewer().getId().equals(adminId)) {
            throw new SecurityException("鍙兘鍒犻櫎鑷繁鐨勫鏍歌褰");
        }

        fileReviewRepository.delete(fileReview);
        log.info("瀹℃牳璁板綍鍒犻櫎鎴愬姛 - ID: {}", reviewId);
    }
    @Override
    @Transactional
    public List<FileReviewResponseDto> batchReviewWorks(List<Long> workIds, String reviewStatus, String reviewComments,
            String adminId) {
        log.info("鎵归噺瀹℃牳浣滃搧 - 作品数量: {}, 鐘舵€? {}, 瀹℃牳鍛業D: {}", workIds.size(), reviewStatus, adminId);

        return workIds.stream()
                .map(workId -> {
                    try {
                        FileReviewRequestDto requestDto = new FileReviewRequestDto();
                        requestDto.setWorkId(workId);
                        requestDto.setReviewStatus(reviewStatus);
                        requestDto.setReviewComments(reviewComments);

                        return createReview(requestDto, adminId);
                    } catch (Exception e) {
                        log.error("鎵归噺瀹℃牳浣滃搧澶辫触 - 作品ID: {}, 閿欒: {}", workId, e.getMessage());
                        return null;
                    }
                })
                .filter(review -> review != null)
                .collect(Collectors.toList());
    }
    @Override
    public long getPendingReviewCount() {
        log.info("鑾峰彇寰呭核的作品数量");
        return fileReviewRepository.countByStatus(FileReview.FileReviewStatus.PENDING);
    }
    @Override
    public Map<String, Long> getReviewStatistics() {
        log.info("鑾峰彇瀹℃牳缁熻淇℃伅");

        Map<String, Long> statistics = Map.of(
                "total", fileReviewRepository.count(),
                "pending", fileReviewRepository.countByStatus(FileReview.FileReviewStatus.PENDING),
                "approved", fileReviewRepository.countByStatus(FileReview.FileReviewStatus.APPROVED),
                "rejected", fileReviewRepository.countByStatus(FileReview.FileReviewStatus.REJECTED));

        return statistics;
    }
    @Override
    public boolean isWorkReviewed(Long workId) {
        log.info("妫€鏌ヤ綔鍝佹槸鍚﹀凡瀹℃牳 - 作品ID: {}", workId);
        return fileReviewRepository.existsByWorkId(workId);
    }

    /**
     * 鏇存柊浣滃搧鐘舵€?     */
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
                // 淇濇寔鍘熸湁鐘舵€
                break;
        }
        work.setUpdatedAt(LocalDateTime.now());
        workRepository.save(work);
        log.info("浣滃搧鐘舵€佸凡鏇存柊 - ID: {}, 鐘舵€? {}", work.getId(), reviewStatus);
    }
}


