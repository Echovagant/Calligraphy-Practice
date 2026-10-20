package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "鏂囦欢瀹℃牳璇锋眰鏁版嵁浼犺緭瀵硅薄")
public class FileReviewRequestDto {
    
    (message = "作品ID涓嶈兘涓虹┖")
    @Schema(description = "作品ID", example = "123")
    private Long workId;
    
    (message = "瀹℃牳鐘舵€佷笉鑳戒负绌")
    @Size(max = 50, message = "瀹℃牳鐘舵€侀暱搴︿笉鑳借秴杩?0瀛楃")
    @Schema(description = "瀹℃牳鐘舵€", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED", "NEEDS_REVISION"})
    private String reviewStatus;
    
    @Size(max = 1000, message = "瀹℃牳鎰忚长度不能超过1000瀛楃")
    @Schema(description = "瀹℃牳鎰忚", example = "浣滃搧鍐呭绗﹀悎瑕佹眰锛岄€氳繃瀹℃牳")
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


