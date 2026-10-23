package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.PracticeRecordCreateDto;
import com.calligraphy.platform.dto.PracticeRecordDto;
import com.calligraphy.platform.dto.PracticeStatisticsDto;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.exception.UserNotFoundException;
import com.calligraphy.platform.model.Copybook;
import com.calligraphy.platform.model.PracticeRecord;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.CopybookRepository;
import com.calligraphy.platform.repository.PracticeRecordRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.service.PracticeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 练习记录服务实现。
 *
 * <p>类级只读事务：映射时会访问 {@code record.getUser()} 这个 LAZY 关联，
 * 映射发生在事务之外会抛 LazyInitializationException（同 P3 的 WorkMapper）。
 * 写方法自己带 {@code @Transactional}，会覆盖类级设置。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PracticeRecordServiceImpl implements PracticeRecordService {

    /**
     * 日期区间下界兜底值：MySQL DATETIME 的最小值。
     * 用它代替「没有下界」，效果与不过滤一致。
     */
    private static final LocalDateTime DATE_FLOOR = LocalDateTime.of(1000, 1, 1, 0, 0);

    /**
     * 日期区间上界兜底值：MySQL DATETIME 的最大值。
     * 用它代替「没有上界」，效果与不过滤一致。
     */
    private static final LocalDateTime DATE_CEILING = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final PracticeRecordRepository practiceRecordRepository;
    private final CopybookRepository copybookRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PracticeRecordDto recordPractice(String username, String copybookId, PracticeRecordCreateDto request) {
        User user = requireUser(username);
        Copybook copybook = copybookRepository.findById(copybookId)
                .orElseThrow(() -> new ResourceNotFoundException("字帖不存在: " + copybookId));

        PracticeRecord record = PracticeRecord.builder()
                .user(user)
                .copybookId(copybookId)
                .copybookTitle(copybook.getTitle())
                // 标题 / 分类 / 难度没传时用字帖自己的，避免前端漏传就存空
                .title(request.getTitle() != null ? request.getTitle() : copybook.getTitle())
                .content(request.getContent())
                .category(request.getCategory() != null ? request.getCategory() : copybook.getCategory())
                .style(request.getStyle())
                .practiceType(request.getPracticeType())
                .referenceWorkId(request.getReferenceWorkId())
                .fileUrls(request.getFileUrls())
                .duration(request.getDuration())
                .characterCount(request.getCharacterCount())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : copybook.getDifficulty())
                .notes(request.getNotes())
                .practiceDate(request.getPracticeDate())
                .isCompleted(false)
                .build();

        PracticeRecord saved = practiceRecordRepository.save(record);

        // 字帖使用次数 +1 —— 热门榜就是按这个排序的
        copybook.setUsageCount((copybook.getUsageCount() == null ? 0 : copybook.getUsageCount()) + 1);
        copybookRepository.save(copybook);

        PracticeRecordDto dto = toDto(saved);
        applyStatistics(dto, buildListStatistics(user.getId()));
        return dto;
    }

    @Override
    public Page<PracticeRecordDto> getPracticeHistory(String username, Pageable pageable) {
        return getPracticeHistory(username, pageable, null, null, null, null);
    }

    @Override
    public Page<PracticeRecordDto> getPracticeHistory(String username, Pageable pageable,
                                                       LocalDate from, LocalDate to,
                                                       String category, String keyword) {
        User user = requireUser(username);
        // 统计只算一次（在分页映射之外），再逐条填进 DTO。
        // 列表用的统计只含 DTO 里那 4 个字段 —— 少算两个用不上的聚合查询。
        PracticeStatisticsDto stats = buildListStatistics(user.getId());
        return practiceRecordRepository
                .findHistory(user.getId(), toFromBound(from), toToBound(to),
                        trimToNull(category), trimToNull(keyword), pageable)
                .map(record -> {
                    PracticeRecordDto dto = toDto(record);
                    applyStatistics(dto, stats);
                    return dto;
                });
    }

    /**
     * 日期区间下界（含）。
     *
     * <p>没给 from 时用一个「足够早」的下界代替 null —— 效果与不过滤一样，
     * 但比较表达式两边都是确定的时间戳，不用在 SQL 里判 null。
     */
    private LocalDateTime toFromBound(LocalDate from) {
        return from == null ? DATE_FLOOR : from.atStartOfDay();
    }

    /**
     * 日期区间上界（**不含**）。
     *
     * <p>to 是「含边界」的日期语义，所以真正要排除的是 to 的**第二天** 00:00:00。
     * 直接拿 to 当上界会把 to 当天的练习全部丢掉（一整天）。
     */
    private LocalDateTime toToBound(LocalDate to) {
        return to == null ? DATE_CEILING : to.plusDays(1).atStartOfDay();
    }

    @Override
    public PracticeStatisticsDto getUserStatistics(String username) {
        return buildStatistics(requireUser(username).getId());
    }

    @Override
    public long countByUserId(String userId) {
        return practiceRecordRepository.countByUserId(userId);
    }

    @Override
    public long countAll() {
        return practiceRecordRepository.count();
    }

    // ------------------------------------------------------------ 内部工具

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("用户未找到: " + username));
    }

    /** null 或空白串一律当「没传这个筛选条件」。 */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 完整统计：{@code GET /api/copybooks/statistics/user} 端点与「记录练习」的响应用。
     *
     * <p>六个字段全算。
     */
    private PracticeStatisticsDto buildStatistics(String userId) {
        return PracticeStatisticsDto.builder()
                .totalPractices(practiceRecordRepository.countByUserId(userId))
                .totalDuration(sumDuration(userId))
                .averageDuration(avgDuration(userId))
                .streakDays(streakDays(userId))
                .characterCount(sumCharacters(userId))
                .copybookCount(practiceRecordRepository.countDistinctCopybookByUserId(userId))
                .build();
    }

    /**
     * 列表用的统计：只要 {@link PracticeRecordDto} 里那 4 个字段。
     *
     * <p>练习历史列表每个元素只带 totalPractices / totalDuration /
     * averageDuration / streakDays，characterCount 与 copybookCount 根本用不上 ——
     * 以前每次翻页都为这两个没人看的数字多跑一次聚合查询。
     */
    private PracticeStatisticsDto buildListStatistics(String userId) {
        return PracticeStatisticsDto.builder()
                .totalPractices(practiceRecordRepository.countByUserId(userId))
                .totalDuration(sumDuration(userId))
                .averageDuration(avgDuration(userId))
                .streakDays(streakDays(userId))
                .build();
    }

    private long sumDuration(String userId) {
        Long totalDuration = practiceRecordRepository.sumDurationByUserId(userId);
        return totalDuration == null ? 0L : totalDuration;
    }

    private double avgDuration(String userId) {
        Double averageDuration = practiceRecordRepository.avgDurationByUserId(userId);
        return averageDuration == null ? 0.0 : averageDuration;
    }

    private long sumCharacters(String userId) {
        Long totalCharacters = practiceRecordRepository.sumCharacterCountByUserId(userId);
        return totalCharacters == null ? 0L : totalCharacters;
    }

    /**
     * 连续练习天数。
     *
     * <p>用按天去重的投影查询（{@code SELECT DISTINCT DATE(practice_date)}）：
     * 同一天练 10 次也只回 1 行，返回行数与练习次数无关。
     */
    private long streakDays(String userId) {
        List<LocalDate> days = practiceRecordRepository.findDistinctPracticeDatesByUserId(userId)
                .stream()
                .map(java.sql.Date::toLocalDate)
                .toList();
        return calculateStreakDays(days);
    }

    /**
     * 连续练习天数：从今天（或昨天）往前逐日回溯，中间断一天就停。
     *
     * <p>今天还没练不算断 —— 从昨天开始接着数。
     */
    private long calculateStreakDays(List<LocalDate> practiceDays) {
        if (practiceDays == null || practiceDays.isEmpty()) {
            return 0L;
        }
        Set<LocalDate> days = new HashSet<>(practiceDays);
        LocalDate cursor = LocalDate.now();
        if (!days.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        long streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private void applyStatistics(PracticeRecordDto dto, PracticeStatisticsDto stats) {
        dto.setTotalPractices(stats.getTotalPractices());
        dto.setTotalDuration(stats.getTotalDuration());
        dto.setAverageDuration(stats.getAverageDuration());
        dto.setStreakDays(stats.getStreakDays());
    }

    private PracticeRecordDto toDto(PracticeRecord record) {
        User user = record.getUser();
        return PracticeRecordDto.builder()
                .id(record.getId() != null ? String.valueOf(record.getId()) : null)
                // userId 是只读列映射：刚插入的实体上还没回填，所以优先取关联对象的 id
                .userId(user != null ? user.getId() : record.getUserId())
                .username(user != null ? user.getUsername() : null)
                .userNickname(user != null ? user.getDisplayName() : null)
                .userAvatar(user != null ? user.getAvatarUrl() : null)
                .title(record.getTitle())
                .content(record.getContent())
                .category(record.getCategory())
                .style(record.getStyle())
                .practiceType(record.getPracticeType())
                .referenceWorkId(record.getReferenceWorkId())
                .referenceWorkTitle(record.getReferenceWorkTitle())
                .fileUrl(record.getFirstFileUrl())
                .duration(record.getDuration() != null ? record.getDuration() : 0L)
                .characterCount(record.getCharacterCount() != null ? record.getCharacterCount() : 0)
                .difficulty(record.getDifficulty())
                .notes(record.getNotes())
                .practiceDate(record.getPracticeDate())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
