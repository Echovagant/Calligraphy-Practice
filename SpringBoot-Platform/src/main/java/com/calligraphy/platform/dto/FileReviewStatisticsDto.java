package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 某个作品的审核汇总 —— **内部载体，不是契约 schema**。
 *
 * <p>契约把统计字段放在 FileReview 里（totalReviews / approvedCount /
 * rejectedCount / revisionRequestedCount / approvalRate / averageReviewTime），
 * 但它们描述的是「这条记录所属作品」的全部审核记录，所以需要一个中间载体
 * 把汇总结果交给 FileReviewMapper 装配。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileReviewStatisticsDto {

    /** 该作品的审核记录总数。 */
    private Long totalReviews;

    /** 该作品审核通过的次数。 */
    private Long approvedCount;

    /** 该作品审核被拒的次数。 */
    private Long rejectedCount;

    /** 该作品被要求修改的次数。 */
    private Long revisionRequestedCount;

    /** 审核通过率，0.0–1.0。 */
    private Double approvalRate;

    /** 平均审核时长（小时）。 */
    private Double averageReviewTime;

    /** 空汇总。 */
    public static FileReviewStatisticsDto empty() {
        return new FileReviewStatisticsDto(0L, 0L, 0L, 0L, 0.0, 0.0);
    }
}
