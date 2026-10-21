package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核记录，对应契约的 {@code FileReview} schema（openapi/contract.yaml）。
 *
 * <p>契约要求主键字段叫 {@code id}（不是 reviewId），并把「这条记录自身」与
 * 「该作品的审核汇总」放在同一个对象里：统计字段（totalReviews / approvedCount /
 * rejectedCount / revisionRequestedCount / approvalRate / averageReviewTime）
 * 描述的是**这条记录所属作品**的全部审核记录，不是它自己。
 *
 * <p>approvalRate 是 0.0–1.0 的比例（1.0 = 100% 通过）；
 * averageReviewTime 的单位是**小时**（契约里写明了）。
 */
@Data
@Schema(description = "作品审核记录")
public class FileReviewDto {

    @Schema(description = "审核记录ID")
    private String id;

    @Schema(description = "被审文件ID（file_reviews 直接存文件属性，没有独立的文件登记表，这里取被审作品的ID）")
    private String fileId;

    @Schema(description = "被审文件名")
    private String fileName;

    @Schema(description = "被审文件地址")
    private String fileUrl;

    @Schema(description = "被审文件类型")
    private String fileType;

    @Schema(description = "被审文件大小（字节）")
    private Long fileSize;

    @Schema(description = "作品作者ID")
    private String userId;

    @Schema(description = "作品作者用户名")
    private String username;

    @Schema(description = "作品作者昵称")
    private String userNickname;

    @Schema(description = "审核员ID")
    private String reviewerId;

    @Schema(description = "审核员用户名")
    private String reviewerUsername;

    @Schema(description = "审核员昵称")
    private String reviewerNickname;

    @Schema(description = "审核状态", allowableValues = {"PENDING", "APPROVED", "REJECTED", "NEEDS_REVISION"})
    private String status;

    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "给作者的反馈（与审核意见同源）")
    private String feedback;

    @Schema(description = "关联作品ID")
    private String relatedWorkId;

    @Schema(description = "关联作品标题")
    private String relatedWorkTitle;

    @Schema(description = "提交审核时间")
    private LocalDateTime submittedAt;

    @Schema(description = "审核完成时间")
    private LocalDateTime reviewedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "该作品的审核记录总数")
    private Long totalReviews;

    @Schema(description = "该作品审核通过的次数")
    private Long approvedCount;

    @Schema(description = "该作品审核被拒的次数")
    private Long rejectedCount;

    @Schema(description = "该作品被要求修改的次数")
    private Long revisionRequestedCount;

    @Schema(description = "该作品的审核通过率（0.0-1.0）")
    private Double approvalRate;

    @Schema(description = "该作品的平均审核时长（小时）")
    private Double averageReviewTime;
}
