package com.calligraphy.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 记录一次练习的请求体，字段集与 openapi/contract.yaml 的
 * {@code PracticeRecordCreate} schema 一致。
 *
 * <p>字帖 ID 不在请求体里 —— 它在路径 {@code /api/copybooks/{copybookId}/practice} 上。
 * 用户身份也从令牌取，不接受客户端指定。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeRecordCreateDto {

    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Size(max = 500, message = "内容长度不能超过500个字符")
    private String content;

    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    @Size(max = 50, message = "字体风格长度不能超过50个字符")
    private String style;

    @Size(max = 50, message = "练习类型长度不能超过50个字符")
    private String practiceType;

    /** 参考作品 ID（works.id，字符串）。 */
    @Size(max = 36, message = "参考作品 ID 长度不能超过36个字符")
    private String referenceWorkId;

    /** 练习图片 URL 列表。 */
    private List<String> fileUrls;

    /** 练习时长（秒）。 */
    @Min(value = 0, message = "练习时长不能为负数")
    private Long duration;

    @Min(value = 0, message = "练习字数不能为负数")
    private Integer characterCount;

    @Size(max = 20, message = "难度长度不能超过20个字符")
    private String difficulty;

    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String notes;

    /** 练习日期，不传则取当前时间。 */
    private LocalDateTime practiceDate;
}
