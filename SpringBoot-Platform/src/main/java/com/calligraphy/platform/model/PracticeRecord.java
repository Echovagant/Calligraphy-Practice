package com.calligraphy.platform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "practice_records", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_practice_user_type", columnList = "user_id, practice_type")
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
    
    @Column(name = "copybook_id", length = 36)
    private String copybookId;
    
    @Size(max = 100, message = "字帖标题长度不能超过100个字符")
    @Column(name = "copybook_title", length = 100)
    private String copybookTitle;
    
    @Min(value = 0, message = "练习时间不能为负数")
    @Max(value = 1440, message = "练习时间不能超过24小时")
    @Column(name = "practice_time")
    private Integer practiceTime; // 练习时间（分钟）
    
    @Min(value = 0, message = "笔画数不能为负数")
    @Column(name = "strokes_count")
    private Integer strokesCount;
    
    @Min(value = 0, message = "练习字数不能为负数")
    @Column(name = "characters_practiced")
    private Integer charactersPracticed;
    
    @Size(max = 50, message = "练习类型长度不能超过50个字符")
    @Column(name = "practice_type", length = 50)
    private String practiceType;
    
    @Size(max = 500, message = "内容长度不能超过500个字符")
    @Column(name = "content", length = 500)
    private String content; // 练习内容或笔记     
    @Column(name = "image_url", length = 500)
    private String imageUrl; // 练习截图或作品图片     
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;
    
    @Column(name = "completion_rate")
    private Double completionRate; // 完成度百分比
    
    @Column(name = "difficulty_level")
    private Integer difficultyLevel; // 难度等级 1-5
    
    @Column(name = "satisfaction_score")
    private Integer satisfactionScore; // 满意度评分 1-10
    
    @Column(name = "practice_date")
    private LocalDateTime practiceDate; // 练习日期
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 预持久化回调
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isCompleted == null) isCompleted = false;
        if (practiceDate == null) practiceDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 业务方法
    public void markAsCompleted() {
        this.isCompleted = true;
        this.completionRate = 100.0;
    }
    
    public void updateCompletionRate(double rate) {
        this.completionRate = Math.max(0.0, Math.min(100.0, rate));
        if (this.completionRate >= 100.0) {
            this.isCompleted = true;
        }
    }
    
    public void updateSatisfactionScore(int score) {
        this.satisfactionScore = Math.max(1, Math.min(10, score));
    }
    
    public void updateDifficultyLevel(int level) {
        this.difficultyLevel = Math.max(1, Math.min(5, level));
    }
    
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }
    
    public String getPracticeDurationDisplay() {
        if (practiceTime == null || practiceTime <= 0) {
            return "未记录";
        }
        if (practiceTime < 60) {
            return practiceTime + "分钟";
        } else {
            int hours = practiceTime / 60;
            int minutes = practiceTime % 60;
            if (minutes == 0) {
                return hours + "小时";
            } else {
                return hours + "小时" + minutes + "分钟";
            }
        }
    }
}


