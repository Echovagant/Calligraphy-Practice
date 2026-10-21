package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量审核请求，对应契约的 {@code FileReviewBatch} schema。
 *
 * <p>workIds 是作品ID数组（works.id 是 Long 自增，契约里声明成 int64）；
 * reviewStatus 的取值与单条审核一致。
 */
@Data
@Schema(description = "批量审核作品请求")
public class FileReviewBatchDto {

    @NotEmpty(message = "作品ID列表不能为空")
    @Schema(description = "作品ID列表", example = "[1, 2, 3]")
    private List<Long> workIds;

    @NotBlank(message = "审核状态不能为空")
    @Pattern(regexp = "(?i)^(PENDING|APPROVED|REJECTED|NEEDS_REVISION)$",
            message = "审核状态必须是 PENDING / APPROVED / REJECTED / NEEDS_REVISION 之一")
    @Schema(description = "审核状态", allowableValues = {"PENDING", "APPROVED", "REJECTED", "NEEDS_REVISION"})
    private String reviewStatus;

    @Size(max = 1000, message = "审核意见长度不能超过1000字符")
    @Schema(description = "审核意见")
    private String reviewComments;
}
