package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 字帖 DTO，字段集与 openapi/contract.yaml 的 {@code Copybook} schema 一致，
 * 外加一项 {@code userId}（自建字帖的归属用户，比契约多出来的内部字段）。
 *
 * <p>{@code isActive} 用包装类型 Boolean：包装类型下 Lombok 生成 {@code getIsActive()}，
 * Jackson 的属性名仍是 {@code isActive}，与契约对齐（见 docs/api-contract.md §3.1）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopybookDto {

    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String difficulty;
    private String category;
    private Integer characterCount;
    private Integer usageCount;
    private Boolean isActive;

    /** 是否为用户自建字帖。精选字帖 false，自建字帖 true。 */
    private Boolean isCustom;

    /** 练习用的正文内容。自建字帖有，精选字帖为空。 */
    private String content;

    /** 字体风格（kai / xing / li / zhuan / cao）。 */
    private String font;

    /** 布局方式（grid / vertical / horizontal）。 */
    private String layout;

    /** 每页字数。 */
    private Integer charactersPerPage;

    /** 单字字号（像素）。 */
    private Integer fontSize;

    /** 纸张颜色。 */
    private String paperColor;

    /** 格子颜色。 */
    private String gridColor;

    /**
     * 自建字帖的归属用户 id（users.id）。精选字帖为 null。
     *
     * <p>契约的 Copybook schema 里没有这一项 —— 它是比契约多出来的内部归属信息，
     * 前端用它判断「这份字帖是不是我存的」。
     */
    private String userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
