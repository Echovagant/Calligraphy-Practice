package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.StatsOverviewDto;

/**
 * 首页概览统计。对应契约的 statsOverview 端点（GET /api/stats/overview）。
 */
public interface StatsService {

    /** 汇总作品数、用户数、练习记录数与热门字帖。 */
    StatsOverviewDto getOverview();
}
