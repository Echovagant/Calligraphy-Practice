package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "练习记录更新请求DTO")
public class PracticeRecordUpdateDto {
    
    @NotBlank(message = "记录ID不能为空")
    @Schema(description = "记录ID", example = "1")
    private Long recordId;
    
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "练习心得")
    private String notes;
    
    @Schema(description = "得分", example = "85.5")
    private Double score;
    
    @Schema(description = "完成状态", example = "COMPLETED")
    private String completionStatus;
    
    @Schema(description = "练习图片URL", example = "https://example.com/practice.jpg")
    private String practiceImage;
}