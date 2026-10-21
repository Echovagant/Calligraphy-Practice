package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "文件审核请求数据传输对象")
public class FileReviewRequestDto {
    
    @NotNull(message = "作品ID不能为空")
    @Schema(description = "作品ID", example = "123")
    private Long workId;
    
    @NotNull(message = "审核状态不能为空")
    @Size(max = 50, message = "审核状态长度不能超过50字符")
    @Schema(description = "审核状态", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED", "NEEDS_REVISION"})
    private String reviewStatus;
    
    @Size(max = 1000, message = "审核意见长度不能超过1000字符")
    @Schema(description = "审核意见", example = "作品内容符合要求，通过审核")
    private String reviewComments;
    
    public FileReviewRequestDto() {
    }
    
    public FileReviewRequestDto(Long workId, String reviewStatus, String reviewComments) {
        this.workId = workId;
        this.reviewStatus = reviewStatus;
        this.reviewComments = reviewComments;
    }
    
    public Long getWorkId() {
        return workId;
    }
    
    public void setWorkId(Long workId) {
        this.workId = workId;
    }
    
    public String getReviewStatus() {
        return reviewStatus;
    }
    
    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
    
    public String getReviewComments() {
        return reviewComments;
    }
    
    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }
}


