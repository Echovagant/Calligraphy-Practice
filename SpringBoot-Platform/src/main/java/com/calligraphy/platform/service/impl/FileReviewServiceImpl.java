package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.FileReviewBatchDto;
import com.calligraphy.platform.dto.FileReviewBatchResultDto;
import com.calligraphy.platform.dto.FileReviewDto;
import com.calligraphy.platform.dto.FileReviewRequestDto;
import com.calligraphy.platform.dto.FileReviewStatisticsDto;
import com.calligraphy.platform.dto.FileReviewUpdateDto;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.mapper.FileReviewMapper;
import com.calligraphy.platform.model.BackendUser;
import com.calligraphy.platform.model.FileReview;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.BackendUserRepository;
import com.calligraphy.platform.repository.FileReviewRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.repository.WorkRepository;
import com.calligraphy.platform.service.FileReviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 作品审核服务实现。
 *
 * <p>审核结果会同步回写到 {@code works.review_status}（契约 fileReviewBatch
 * 的描述里明确要求），同时把作品的可见性状态对齐到
 * {@code Work.approve / reject / requestRevision} 的语义：
 * 通过 → PUBLISHED，被拒 → HIDDEN，要修改 → UNDER_REVIEW。
 */
@Service
@RequiredArgsConstructor
// 类级只读事务：FileReviewMapper.toDto 会访问 review.getUploadedBy() /
// getReviewer() / getWork() 这类 LAZY 关联，映射发生在事务之外会抛
// LazyInitializationException。写方法自己带 @Transactional（readOnly 默认 false），
// 会覆盖类级设置。
@Transactional(readOnly = true)
public class FileReviewServiceImpl implements FileReviewService {

    private static final Logger log = LoggerFactory.getLogger(FileReviewServiceImpl.class);

    private final FileReviewRepository fileReviewRepository;
    private final WorkRepository workRepository;
    private final UserRepository userRepository;
    private final BackendUserRepository backendUserRepository;
    private final FileReviewMapper fileReviewMapper;
    private final TransactionTemplate transactionTemplate;

    // ------------------------------------------------------------ 7 个契约 operation

    @Override
    @Transactional
    public FileReviewDto createReview(FileReviewRequestDto requestDto, String reviewerUsername) {
        return createReviewInternal(requestDto.getWorkId(), requestDto.getReviewStatus(),
                requestDto.getReviewComments(), reviewerUsername);
    }

    /**
     * 批量审核。
     *
     * <p>每个作品单独跑一个事务（TransactionTemplate）：某一个作品失败不会把
     * 整个批次拖垮，也不会把已经成功的那些标成回滚。
     * 方法本身用 NOT_SUPPORTED 挂起类级的只读事务，避免内层事务被卷进只读事务里。
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FileReviewBatchResultDto batchReviewWorks(FileReviewBatchDto batchDto, String reviewerUsername) {
        List<Long> workIds = batchDto.getWorkIds() == null ? List.of() : batchDto.getWorkIds();
        List<FileReviewDto> succeeded = new ArrayList<>();
        int failed = 0;

        for (Long workId : workIds) {
            try {
                FileReviewDto dto = transactionTemplate.execute(status -> createReviewInternal(
                        workId, batchDto.getReviewStatus(), batchDto.getReviewComments(), reviewerUsername));
                if (dto != null) {
                    succeeded.add(dto);
                } else {
                    failed++;
                }
            } catch (RuntimeException e) {
                // 作品不存在等失败只计入 failed，其余作品继续处理。
                failed++;
                log.warn("批量审核失败 - 作品ID: {}, 原因: {}", workId, e.getMessage());
            }
        }

        FileReviewBatchResultDto result = new FileReviewBatchResultDto();
        result.setTotal((long) workIds.size());
        result.setSucceeded((long) succeeded.size());
        result.setFailed((long) failed);
        result.setReviews(succeeded);
        log.info("批量审核完成 - 总数: {}, 成功: {}, 失败: {}", result.getTotal(), result.getSucceeded(), result.getFailed());
        return result;
    }

    @Override
    public Page<FileReviewDto> getMyReviews(String reviewerUsername, Pageable pageable) {
        BackendUser reviewer = backendUserRepository.findByUsername(reviewerUsername).orElse(null);
        if (reviewer == null) {
            // 还没有提交过审核，自然也没有审核记录。
            return new PageImpl<>(List.of(), pageable, 0);
        }
        Page<FileReview> reviews = fileReviewRepository.findByReviewerId(reviewer.getId(), pageable);
        Map<Long, FileReviewStatisticsDto> statistics = statisticsByWork(reviews.getContent());
        return reviews.map(review -> fileReviewMapper.toDto(review,
                statistics.getOrDefault(review.getWorkId(), FileReviewStatisticsDto.empty())));
    }

    @Override
    public FileReviewDto getReviewByWorkId(Long workId) {
        if (!workRepository.existsById(workId)) {
            throw new ResourceNotFoundException("作品不存在: " + workId);
        }
        FileReview review = fileReviewRepository.findFirstByWorkIdOrderByCreatedAtDesc(workId)
                .orElseThrow(() -> new ResourceNotFoundException("该作品暂无审核记录: " + workId));
        return fileReviewMapper.toDto(review, statisticsOf(workId));
    }

    @Override
    public FileReviewDto getReviewById(String reviewId) {
        FileReview review = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("审核记录不存在: " + reviewId));
        return fileReviewMapper.toDto(review, statisticsOf(review.getWorkId()));
    }

    @Override
    @Transactional
    public FileReviewDto updateReview(String reviewId, FileReviewUpdateDto updateDto, String reviewerUsername) {
        FileReview.FileReviewStatus status = parseStatus(updateDto.getReviewStatus());
        FileReview review = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("审核记录不存在: " + reviewId));
        BackendUser reviewer = resolveReviewer(reviewerUsername);

        review.setStatus(status);
        review.setReviewNotes(updateDto.getReviewComments());
        review.setReviewer(reviewer);
        review.setReviewTime(status == FileReview.FileReviewStatus.PENDING ? null : LocalDateTime.now());
        FileReview saved = fileReviewRepository.save(review);

        if (saved.getWork() != null) {
            applyReviewToWork(saved.getWork(), reviewer, status, updateDto.getReviewComments(), saved.getReviewTime());
        }
        log.info("更新审核结论 - ID: {}, 状态: {}", reviewId, status);
        return fileReviewMapper.toDto(saved, statisticsOf(saved.getWorkId()));
    }

    @Override
    @Transactional
    public void deleteReview(String reviewId, String reviewerUsername) {
        FileReview review = fileReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("审核记录不存在: " + reviewId));
        fileReviewRepository.delete(review);
        log.info("删除审核记录 - ID: {}, 操作人: {}", reviewId, reviewerUsername);
    }

    // ------------------------------------------------------------ 内部实现

    /** 建一条审核记录并把结论回写到作品（调用方负责事务）。 */
    private FileReviewDto createReviewInternal(Long workId, String reviewStatus, String reviewComments,
            String reviewerUsername) {
        FileReview.FileReviewStatus status = parseStatus(reviewStatus);
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("作品不存在: " + workId));
        BackendUser reviewer = resolveReviewer(reviewerUsername);

        User author = work.getUser();
        if (author == null) {
            throw new BusinessException("作品缺少作者，无法生成审核记录: " + workId);
        }

        FileReview review = new FileReview();
        review.setWork(work);
        review.setUploadedBy(author);
        review.setReviewer(reviewer);
        review.setStatus(status);
        review.setReviewNotes(reviewComments);
        // 提交审核的时间 = 作品提交的时间，平均审核时长从这里算起。
        review.setUploadTime(work.getCreatedAt() != null ? work.getCreatedAt() : LocalDateTime.now());
        review.setReviewTime(status == FileReview.FileReviewStatus.PENDING ? null : LocalDateTime.now());
        review.setFileName(fileNameOf(work));
        review.setFilePath(work.getImageUrl() != null ? work.getImageUrl() : "");
        review.setFileType(fileTypeOf(work));
        review.setMimeType(mimeTypeOf(work));
        // 作品表里没有文件大小，file_reviews.file_size 只能记 0。
        review.setFileSize(0L);

        FileReview saved = fileReviewRepository.save(review);
        applyReviewToWork(work, reviewer, status, reviewComments, saved.getReviewTime());
        log.info("创建审核记录 - ID: {}, 作品ID: {}, 状态: {}", saved.getId(), workId, status);
        return fileReviewMapper.toDto(saved, statisticsOf(work.getId()));
    }

    /** 把审核结论回写到作品：review_status + 可见性状态 + 审核意见。 */
    private void applyReviewToWork(Work work, BackendUser reviewer, FileReview.FileReviewStatus status,
            String reviewComments, LocalDateTime reviewTime) {
        work.setReviewStatus(Work.ReviewStatus.valueOf(status.name()));
        work.setReviewNotes(reviewComments);
        work.setReviewTime(reviewTime != null ? reviewTime : LocalDateTime.now());
        work.setReviewer(reviewer);
        switch (status) {
            case APPROVED -> work.setStatus(Work.WorkStatus.PUBLISHED);
            case REJECTED -> work.setStatus(Work.WorkStatus.HIDDEN);
            case NEEDS_REVISION -> work.setStatus(Work.WorkStatus.UNDER_REVIEW);
            case PENDING -> {
                // 待审核：作品可见性保持原样，只把 review_status 落成 PENDING。
            }
        }
        workRepository.save(work);
        log.info("作品审核状态已回写 - 作品ID: {}, review_status: {}", work.getId(), work.getReviewStatus());
    }

    /**
     * 解析当前审核员。
     *
     * <p>管理员身份在 P2 之后统一到 users 表（role ∈ {ADMIN, SUPER_ADMIN}），
     * 但 file_reviews.reviewer_id / works.reviewer_id 的外键仍然指向历史遗留的
     * backend_users 表。为了让外键成立，这里为该管理员在 backend_users 里补一条
     * 审核员记录；username 唯一，所以每个管理员只会补一次，之后直接复用。
     */
    private BackendUser resolveReviewer(String username) {
        return backendUserRepository.findByUsername(username)
                .orElseGet(() -> provisionReviewer(username));
    }

    /** 为只有 users 表身份的管理员补一条 backend_users 审核员记录。 */
    private BackendUser provisionReviewer(String username) {
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("管理员账号不存在: " + username));
        if (!admin.isAdmin()) {
            throw new AccessDeniedException("只有管理员可以审核作品");
        }
        BackendUser reviewer = BackendUser.builder()
                .username(admin.getUsername())
                .email(reviewerEmail(admin))
                // 随机占位密码：这个账号只用来满足外键，不参与登录。
                .password(UUID.randomUUID().toString())
                .role(admin.getRole() == User.Role.SUPER_ADMIN
                        ? BackendUser.BackendUserRole.SUPER_ADMIN
                        : BackendUser.BackendUserRole.ADMIN)
                .fullName(admin.getDisplayName())
                .build();
        log.info("为管理员补建审核员记录 - 用户名: {}", admin.getUsername());
        return backendUserRepository.save(reviewer);
    }

    /** backend_users.email 唯一：撞了就用合成邮箱。 */
    private String reviewerEmail(User admin) {
        String email = admin.getEmail();
        if (email != null && !email.isBlank() && backendUserRepository.findByEmail(email).isEmpty()) {
            return email;
        }
        return admin.getUsername() + "@backend.invalid";
    }

    /** 审核状态取值校验（契约枚举：PENDING / APPROVED / REJECTED / NEEDS_REVISION）。 */
    private FileReview.FileReviewStatus parseStatus(String reviewStatus) {
        if (reviewStatus == null || reviewStatus.isBlank()) {
            throw new BusinessException("审核状态不能为空");
        }
        try {
            return FileReview.FileReviewStatus.valueOf(reviewStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("未知的审核状态: " + reviewStatus);
        }
    }

    // ------------------------------------------------------------ 统计字段

    /** 某个作品的审核汇总。 */
    private FileReviewStatisticsDto statisticsOf(Long workId) {
        if (workId == null) {
            return FileReviewStatisticsDto.empty();
        }
        return statisticsOf(fileReviewRepository.findByWorkId(workId));
    }

    /** 一次算出一批作品各自的审核汇总（避免逐行查库）。 */
    private Map<Long, FileReviewStatisticsDto> statisticsByWork(List<FileReview> reviews) {
        List<Long> workIds = reviews.stream()
                .map(FileReview::getWorkId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (workIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<FileReview>> grouped = fileReviewRepository.findByWorkIdIn(workIds).stream()
                .filter(review -> review.getWorkId() != null)
                .collect(Collectors.groupingBy(FileReview::getWorkId));
        Map<Long, FileReviewStatisticsDto> statistics = new LinkedHashMap<>();
        for (Long workId : workIds) {
            statistics.put(workId, statisticsOf(grouped.getOrDefault(workId, List.of())));
        }
        return statistics;
    }

    /** 汇总口径：total = 该作品全部审核记录数，approvalRate = 通过数 / 总数（0.0-1.0）。 */
    private FileReviewStatisticsDto statisticsOf(List<FileReview> reviews) {
        long total = reviews.size();
        long approved = countStatus(reviews, FileReview.FileReviewStatus.APPROVED);
        long rejected = countStatus(reviews, FileReview.FileReviewStatus.REJECTED);
        long revision = countStatus(reviews, FileReview.FileReviewStatus.NEEDS_REVISION);
        double approvalRate = total == 0 ? 0.0 : (double) approved / total;
        return new FileReviewStatisticsDto(total, approved, rejected, revision, approvalRate,
                averageReviewHours(reviews));
    }

    private long countStatus(List<FileReview> reviews, FileReview.FileReviewStatus status) {
        return reviews.stream().filter(review -> status.equals(review.getStatus())).count();
    }

    /** 平均审核时长（小时）= 平均（审核完成时间 - 提交审核时间）。 */
    private double averageReviewHours(List<FileReview> reviews) {
        double totalHours = 0.0;
        int counted = 0;
        for (FileReview review : reviews) {
            if (review.getReviewTime() == null || review.getUploadTime() == null) {
                continue;
            }
            totalHours += Duration.between(review.getUploadTime(), review.getReviewTime()).toMinutes() / 60.0;
            counted++;
        }
        return counted == 0 ? 0.0 : totalHours / counted;
    }

    // ------------------------------------------------------------ 文件属性

    /** 被审文件名：取作品主图的文件名，没有主图就用作品标题。 */
    private String fileNameOf(Work work) {
        String url = work.getImageUrl();
        if (url == null || url.isBlank()) {
            return work.getTitle();
        }
        int slash = url.lastIndexOf('/');
        String name = slash >= 0 ? url.substring(slash + 1) : url;
        return name.isBlank() ? work.getTitle() : name;
    }

    /** 被审文件类型：取主图扩展名，没有就记 image。 */
    private String fileTypeOf(Work work) {
        return extensionOf(work.getImageUrl(), "image");
    }

    /** 被审文件 MIME 类型。 */
    private String mimeTypeOf(Work work) {
        String type = fileTypeOf(work);
        return "image".equals(type) ? "image/*" : "image/" + type;
    }

    private String extensionOf(String url, String fallback) {
        if (url == null || url.isBlank()) {
            return fallback;
        }
        int dot = url.lastIndexOf('.');
        int slash = url.lastIndexOf('/');
        if (dot > slash && dot < url.length() - 1) {
            String extension = url.substring(dot + 1).toLowerCase(Locale.ROOT);
            return extension.matches("[a-z0-9]{1,10}") ? extension : fallback;
        }
        return fallback;
    }
}
