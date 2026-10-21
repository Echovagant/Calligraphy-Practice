package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新审核结论的请求体，对应契约的 {@code FileReviewUpdate} schema。
 */
@Data
@Schema(description = "更新审核结论请求")
public class FileReviewUpdateDto {

    @NotBlank(message = "审核状态不能为空")
    @Pattern(regexp = "(?i)^(PENDING|APPROVED|REJECTED|NEEDS_REVISION)$",
            message = "审核状态必须是 PENDING / APPROVED / REJECTED / NEEDS_REVISION 之一")
    @Schema(description = "审核状态", allowableValues = {"PENDING", "APPROVED", "REJECTED", "NEEDS_REVISION"})
    private String reviewStatus;

    @Size(max = 1000, message = "审核意见长度不能超过1000字符")
    @Schema(description = "审核意见")
    private String reviewComments;
}
