package com.calligraphy.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存一份自建字帖的请求体，对应契约的 {@code CopybookCreate} schema。
 *
 * <p>契约里必填的是 {@code title} 与 {@code content}；其余字段是「字帖生成」页
 * 生成的排版参数，缺省时后端留空，前端自己带默认值。
 *
 * <p>各字段的长度上限与 {@code model/Copybook} 的列长度一一对应：
 * title 200、description 1000、font 20、layout 20、paperColor 20、gridColor 20、
 * category 50、difficulty 20；content 落在 TEXT 列上，限 10000 字符。
 */
@Data
public class CopybookCreateRequestDto {

    @NotBlank(message = "字帖标题不能为空")
    @Size(max = 200, message = "字帖标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "练习内容不能为空")
    @Size(max = 10000, message = "练习内容长度不能超过10000个字符")
    private String content;

    @Size(max = 1000, message = "描述长度不能超过1000个字符")
    private String description;

    /** 字体风格：kai / xing / li / zhuan / cao。 */
    @Size(max = 20, message = "字体风格长度不能超过20个字符")
    private String font;

    /** 布局方式：grid / vertical / horizontal。 */
    @Size(max = 20, message = "布局方式长度不能超过20个字符")
    private String layout;

    /** 每页字数。 */
    @Min(value = 0, message = "每页字数不能为负数")
    private Integer charactersPerPage;

    /** 单字字号（像素）。 */
    @Min(value = 0, message = "字号不能为负数")
    private Integer fontSize;

    /** 纸张颜色。 */
    @Size(max = 20, message = "纸张颜色长度不能超过20个字符")
    private String paperColor;

    /** 格子颜色。 */
    @Size(max = 20, message = "格子颜色长度不能超过20个字符")
    private String gridColor;

    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    @Size(max = 20, message = "难度长度不能超过20个字符")
    private String difficulty;
}
