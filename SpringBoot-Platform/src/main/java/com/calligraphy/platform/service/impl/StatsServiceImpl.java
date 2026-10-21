package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.StatsOverviewDto;
import com.calligraphy.platform.model.Copybook;
import com.calligraphy.platform.repository.CopybookRepository;
import com.calligraphy.platform.service.PracticeRecordService;
import com.calligraphy.platform.service.StatsService;
import com.calligraphy.platform.service.UserService;
import com.calligraphy.platform.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页概览统计实现。
 *
 * <p>数据来源：作品数取 {@code WorkService.getTotalWorkCount()}，用户数取
 * {@code UserService.getTotalUserCount()}，练习记录数取 practice_records 表的行数，
 * 热门字帖按 usage_count 倒序取前 {@value #POPULAR_COPYBOOK_LIMIT} 个。
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    /** 热门字帖返回条数。 */
    private static final int POPULAR_COPYBOOK_LIMIT = 5;

    private final WorkService workService;
    private final UserService userService;
    private final PracticeRecordService practiceRecordService;
    private final CopybookRepository copybookRepository;

    @Override
    public StatsOverviewDto getOverview() {
        List<Copybook> popular = copybookRepository.findByIsActiveTrueOrderByUsageCountDesc()
                .stream()
                .limit(POPULAR_COPYBOOK_LIMIT)
                .collect(Collectors.toList());

        return StatsOverviewDto.builder()
                .totalWorks(workService.getTotalWorkCount())
                .totalUsers(userService.getTotalUserCount())
                .totalPracticeRecords(practiceRecordService.countAll())
                .popularCopybooks(popular)
                .build();
    }
}
