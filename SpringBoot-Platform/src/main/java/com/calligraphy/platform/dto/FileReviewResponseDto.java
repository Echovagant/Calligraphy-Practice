package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "鏂囦欢瀹℃牳鍝嶅簲鏁版嵁浼犺緭瀵硅薄")
public class FileReviewResponseDto {
    
    @Schema(description = "瀹℃牳璁板綍ID", example = "123")
    private Long id;
    
    @Schema(description = "浣滃搧ID", example = "123")
    private Long workId;
    
    @Schema(description = "浣滃搧鏍囬", example = "鎴戠殑涔︽硶浣滃搧")
    private String workTitle;
    
    @Schema(description = "瀹℃牳鍛業D", example = "456")
    private Long reviewerId;
    
    @Schema(description = "瀹℃牳鍛樺鍚?, example = "寮犵鐞嗗憳")
    private String reviewerName;
    
    @Schema(description = "瀹℃牳鐘舵€?, example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED", "NEEDS_REVISION"})
    private String reviewStatus;
    
    @Schema(description = "瀹℃牳鎰忚", example = "浣滃搧鍐呭绗﹀悎瑕佹眰锛岄€氳繃瀹℃牳")
    private String reviewComments;
    
    @Schema(description = "瀹℃牳鏃堕棿", example = "2024-01-01T12:00:00")
    private LocalDateTime reviewedAt;
    
    @Schema(description = "鍒涘缓鏃堕棿", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "鏇存柊鏃堕棿", example = "2024-01-01T12:00:00")
    private LocalDateTime updatedAt;
}


