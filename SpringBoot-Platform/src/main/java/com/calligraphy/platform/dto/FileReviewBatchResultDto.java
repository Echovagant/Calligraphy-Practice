package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量审核结果，对应契约的 {@code FileReviewBatchResult} schema。
 *
 * <p>total 是请求里提交的 workIds 个数；succeeded + failed = total。
 * 单个作品失败（例如作品不存在）只计入 failed，不会让整个请求失败。
 */
@Data
@Schema(description = "批量审核结果")
public class FileReviewBatchResultDto {

    @Schema(description = "提交的审核总数")
    private Long total;

    @Schema(description = "成功数")
    private Long succeeded;

    @Schema(description = "失败数")
    private Long failed;

    @Schema(description = "成功产生的审核记录")
    private List<FileReviewDto> reviews;
}
