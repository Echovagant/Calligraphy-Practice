package com.calligraphy.platform.model;

import com.calligraphy.platform.converter.StringListJsonConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习记录。字段集对齐 openapi/contract.yaml 的 {@code PracticeRecord} schema。
 *
 * <p>与契约相比只多一个内部字段 {@code isCompleted} —— 它的数据库列是
 * {@code NOT NULL} 且没有默认值，删掉会导致 INSERT 失败，所以保留但不对外暴露。
 */
@Entity
@Table(name = "practice_records", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_practice_user_type", columnList = "user_id, practice_type"),
        // 练习历史：某个用户的记录，按练习时间倒序翻页。
        // 索引名与列序必须与 db/06-performance-indexes.sql 一致。
        @Index(name = "idx_practice_user_date", columnList = "user_id, practice_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "user_id", length = 36, nullable = false, insertable = false, updatable = false)
    private String userId;

    /** 练习的字帖 ID，对应 copybooks.id。 */
    @Column(name = "copybook_id", length = 36)
    private String copybookId;

    /** 字帖标题的冗余快照，列表直接展示用。 */
    @Size(max = 100, message = "字帖标题长度不能超过100个字符")
    @Column(name = "copybook_title", length = 100)
    private String copybookTitle;

    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Column(name = "title", length = 200)
    private String title;

    @Size(max = 500, message = "内容长度不能超过500个字符")
    @Column(name = "content", length = 500)
    private String content;

    @Size(max = 50, message = "分类长度不能超过50个字符")
    @Column(name = "category", length = 50)
    private String category;

    @Size(max = 50, message = "字体风格长度不能超过50个字符")
    @Column(name = "style", length = 50)
    private String style;

    @Size(max = 50, message = "练习类型长度不能超过50个字符")
    @Column(name = "practice_type", length = 50)
    private String practiceType;

    /** 参考作品 ID，对应 works.id。 */
    @Column(name = "reference_work_id", length = 36)
    private String referenceWorkId;

    @Size(max = 200, message = "参考作品标题长度不能超过200个字符")
    @Column(name = "reference_work_title", length = 200)
    private String referenceWorkTitle;

    /** 练习图片（可能多张）。契约响应只暴露单个 fileUrl，这里存全量。 */
    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "file_urls", columnDefinition = "TEXT")
    private List<String> fileUrls;

    /** 练习时长（秒）。契约字段 duration。 */
    @Min(value = 0, message = "练习时长不能为负数")
    @Column(name = "duration")
    private Long duration;

    @Min(value = 0, message = "练习字数不能为负数")
    @Column(name = "character_count")
    private Integer characterCount;

    @Size(max = 20, message = "难度长度不能超过20个字符")
    @Column(name = "difficulty", length = 20)
    private String difficulty;

    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    @Column(name = "notes", length = 1000)
    private String notes;

    /** 内部字段，不对契约暴露。数据库列 NOT NULL 且无默认值，不能删。 */
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "practice_date")
    private LocalDateTime practiceDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isCompleted == null) {
            isCompleted = false;
        }
        if (practiceDate == null) {
            practiceDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** 附件列表里的第一个 URL —— 契约的响应字段 fileUrl。 */
    public String getFirstFileUrl() {
        return fileUrls == null || fileUrls.isEmpty() ? null : fileUrls.get(0);
    }
}
