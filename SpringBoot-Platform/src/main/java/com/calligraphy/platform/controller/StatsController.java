package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.StatsOverviewDto;
import com.calligraphy.platform.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计接口，对应契约的 statsOverview 端点（GET /api/stats/overview）。
 *
 * <p>公开端点：首页概览不需要令牌。响应是裸资源，不带信封。
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * GET /api/stats/overview —— 200 裸 StatsOverview
     */
    @GetMapping("/overview")
    public ResponseEntity<StatsOverviewDto> overview() {
        return ResponseEntity.ok(statsService.getOverview());
    }
}
