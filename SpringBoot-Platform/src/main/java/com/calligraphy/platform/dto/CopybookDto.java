package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 字帖 DTO，字段集与 openapi/contract.yaml 的 {@code Copybook} schema 一致。
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
