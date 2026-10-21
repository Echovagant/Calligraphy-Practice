package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 作品发布表单（multipart/form-data），对应契约的 {@code WorkCreateForm} schema。
 *
 * <p>前端实际只发 {@code title} + {@code description} + {@code image}，所以这里
 * **只有 title 必填**。不能直接用 {@code WorkCreateRequestDto} 收 multipart ——
 * 它的 content / category 是 {@code @NotBlank}，前端不传会直接 400。
 */
@Data
public class WorkCreateFormDto {

    @NotBlank(message = "作品标题不能为空")
    @Size(max = 200, message = "作品标题长度不能超过200位")
    private String title;

    @Size(max = 1000, message = "作品描述长度不能超过1000位")
    private String description;

    @Size(max = 10000, message = "作品内容长度不能超过10000位")
    private String content;

    @Size(max = 50, message = "作品分类长度不能超过50位")
    private String category;

    /** 逗号分隔的标签串（multipart 里没有数组类型）。 */
    @Size(max = 500, message = "标签长度不能超过500位")
    private String tags;

    /** 作品图片，multipart 字段名就是 image。 */
    private MultipartFile image;
}
