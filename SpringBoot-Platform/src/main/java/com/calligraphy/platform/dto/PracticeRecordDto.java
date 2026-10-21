package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 练习记录响应，字段集与 openapi/contract.yaml 的 {@code PracticeRecord} schema 一致。
 *
 * <p>主键字段叫 {@code id}（不是 {@code recordId}）—— 契约统一以 {@code id} 为主键名。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeRecordDto {

    private String id;
    private String userId;
    private String username;
    private String userNickname;
    private String userAvatar;
    private String title;
    private String content;
    private String category;
    private String style;
    private String practiceType;
    private String referenceWorkId;
    private String referenceWorkTitle;
    private String fileUrl;
    /** 练习时长（秒）。 */
    private long duration;
    private int characterCount;
    private String difficulty;
    private String notes;
    private LocalDateTime practiceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- 统计信息（当前用户的汇总，不是这条记录自身的）----

    private long totalPractices;
    private long totalDuration;
    private double averageDuration;
    private long streakDays;
}
