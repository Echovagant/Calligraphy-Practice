package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.Copybook;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页概览，对应契约的 {@code StatsOverview} schema。
 *
 * <p>契约里写明「后端暂无对应 DTO，P3 实现时按此形状新建」——就是这个类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsOverviewDto {

    private long totalWorks;
    private long totalUsers;
    private long totalPracticeRecords;

    /** 热门字帖，按 usageCount 倒序取前几个。元素形状与契约的 Copybook schema 一致。 */
    private List<Copybook> popularCopybooks;
}
