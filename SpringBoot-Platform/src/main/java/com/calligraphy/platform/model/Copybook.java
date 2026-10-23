package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "copybooks", indexes = {
    // 首页概览取「启用中的字帖，按使用次数倒序的前 5 条」。
    // 索引名与列序必须与 db/06-performance-indexes.sql 一致。
    @Index(name = "idx_copybooks_active_usage", columnList = "is_active, usage_count")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Copybook {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @NotBlank(message = "字帖标题不能为空")
    @Size(max = 200, message = "字帖标题长度不能超过200个字符")
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255, nullable = false)
    private String imageUrl;

    @Column(name = "difficulty", length = 20)
    private String difficulty;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "character_count")
    private Integer characterCount;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "is_active")
    private Boolean isActive;

    /**
     * 是否为用户自建字帖。精选字帖 false，自建字帖 true。
     *
     * <p>列可空：ddl-auto=update 给已有库补列时不会给默认值，
     * 补一个 NOT NULL 且无默认值的列会让老数据启动即失败。
     * 所以这里允许 NULL，读的时候按「不是 true 就是精选字帖」处理。
     */
    @Column(name = "is_custom")
    private Boolean isCustom;

    /**
     * 练习用的正文内容（自建字帖有，精选字帖为空）。
     *
     * <p>用 TEXT 而不是 VARCHAR(255)：一份自建字帖的正文可能有好几千字。
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** 字体风格（kai / xing / li / zhuan / cao）。 */
    @Column(name = "font", length = 20)
    private String font;

    /** 布局方式（grid / vertical / horizontal）。 */
    @Column(name = "layout", length = 20)
    private String layout;

    /** 每页字数。 */
    @Column(name = "characters_per_page")
    private Integer charactersPerPage;

    /** 单字字号（像素）。 */
    @Column(name = "font_size")
    private Integer fontSize;

    /** 纸张颜色。 */
    @Column(name = "paper_color", length = 20)
    private String paperColor;

    /** 格子颜色。 */
    @Column(name = "grid_color", length = 20)
    private String gridColor;

    /** 自建字帖的归属用户（users.id）。精选字帖为空。 */
    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (isActive == null) isActive = true;
        if (isCustom == null) isCustom = false;
        if (characterCount == null) characterCount = 0;
        if (usageCount == null) usageCount = 0;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
