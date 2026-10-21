package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前用户的练习统计，字段集与 openapi/contract.yaml 的 {@code PracticeStatistics} schema 一致。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeStatisticsDto {

    /** 练习总次数。 */
    private long totalPractices;

    /** 练习总时长（秒）。 */
    private long totalDuration;

    /** 平均单次时长（秒）。 */
    private double averageDuration;

    /** 连续练习天数。 */
    private long streakDays;

    /** 累计练习字数。 */
    private long characterCount;

    /** 练过的字帖数（去重）。 */
    private long copybookCount;
}
