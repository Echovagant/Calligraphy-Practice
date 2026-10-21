package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "文件审核响应数据传输对象")
public class FileReviewResponseDto {
    
    @Schema(description = "审核记录ID", example = "123")
    private Long id;
    
    @Schema(description = "作品ID", example = "123")
    private Long workId;
    
    @Schema(description = "作品标题", example = "我的书法作品")
    private String workTitle;
    
    @Schema(description = "审核员ID", example = "456")
    private Long reviewerId;
    
    @Schema(description = "审核员姓名", example = "张管理员")
    private String reviewerName;
    
    @Schema(description = "审核状态", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED", "NEEDS_REVISION"})
    private String reviewStatus;
    
    @Schema(description = "审核意见", example = "作品内容符合要求，通过审核")
    private String reviewComments;
    
    @Schema(description = "审核时间", example = "2024-01-01T12:00:00")
    private LocalDateTime reviewedAt;
    
    @Schema(description = "创建时间", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;
}


