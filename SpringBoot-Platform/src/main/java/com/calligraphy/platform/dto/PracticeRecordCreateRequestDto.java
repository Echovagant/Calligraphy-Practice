package com.calligraphy.platform.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeRecordCreateRequestDto {

    @NotBlank(message = "练习标题不能为空")
    @Size(max = 200, message = "练习标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "练习内容不能为空")
    @Size(max = 10000, message = "练习内容长度不能超过10000个字符")
    private String content;

    @NotBlank(message = "练习分类不能为空")
    @Size(max = 50, message = "练习分类长度不能超过50个字符")
    private String category;

    private String style;

    private String practiceType;

    private String referenceWorkId;

    private List<String> fileUrls;

    @Min(value = 1, message = "练习时长必须大于0")
    private long duration; // 练习时长（秒）

    @Min(value = 1, message = "字符数必须大于0")
    private int characterCount;

    private String difficulty;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String notes;

    private LocalDateTime practiceDate;
}