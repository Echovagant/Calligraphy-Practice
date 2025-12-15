package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.PracticeRecord;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeRecordDto {

    private String recordId;
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
    private long duration; // 练习时长（秒）
    private int characterCount;
    private String difficulty;
    private String notes;
    private LocalDateTime practiceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 统计信息
    private long totalPractices;
    private long totalDuration;
    private double averageDuration;
    private long streakDays;
}
