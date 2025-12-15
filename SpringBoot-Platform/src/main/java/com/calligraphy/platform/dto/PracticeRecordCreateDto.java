package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "练习记录创建请求DTO")
public class PracticeRecordCreateDto {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotBlank(message = "字体不能为空")
    @Size(max = 50, message = "字体长度不能超过50个字符")
    @Schema(description = "字体", example = "欧体")
    private String font;

    @NotBlank(message = "汉字不能为空")
    @Size(max = 10, message = "汉字长度不能超过10个字符")
    @Schema(description = "汉字", example = "人")
    private String character;

    @NotNull(message = "练习次数不能为空")
    @Schema(description = "练习次数", example = "5")
    private Integer practiceCount;

    @NotBlank(message = "完成状态不能为空")
    @Schema(description = "完成状态", example = "COMPLETED")
    private String completionStatus;

    @Schema(description = "得分", example = "85.5")
    private Double score;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "练习心得")
    private String notes;

    @Schema(description = "练习图片URL", example = "https://example.com/practice.jpg")
    private String practiceImage;
}