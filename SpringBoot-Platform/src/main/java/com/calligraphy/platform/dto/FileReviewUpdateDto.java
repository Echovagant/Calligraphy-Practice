package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "文件审核更新数据传输对象")
public class FileReviewUpdateDto {
    
    @NotNull(message = "审核状态不能为空")
    @Size(max = 50, message = "审核状态长度不能超过50个字符")
    @Schema(description = "审核状态", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED", "NEEDS_REVISION"})
    private String reviewStatus;
    
    @Size(max = 1000, message = "审核意见长度不能超过1000个字符")
    @Schema(description = "审核意见", example = "作品内容符合要求，通过审核")
    private String reviewComments;
    
    // 无参构造器
    public FileReviewUpdateDto() {
    }
    
    // 全参构造器
    public FileReviewUpdateDto(String reviewStatus, String reviewComments) {
        this.reviewStatus = reviewStatus;
        this.reviewComments = reviewComments;
    }
    
    // Getter 和 Setter 方法
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


