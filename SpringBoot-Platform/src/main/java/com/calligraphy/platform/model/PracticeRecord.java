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
    private FrontendUser user;
    
    @Column(name = "user_id", length = 36, nullable = false, insertable = false, updatable = false)
    private String userId;
    
    @Column(name = "copybook_id")
    private Long copybookId;
    
    @Size(max = 100, message = "瀛楀笘鏍囬闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Column(name = "copybook_title", length = 100)
    private String copybookTitle;
    
    @Min(value = 0, message = "缁冧範鏃堕棿涓嶈兘涓鸿礋鏁?)
    @Max(value = 1440, message = "缁冧範鏃堕棿涓嶈兘瓒呰繃24灏忔椂")
    @Column(name = "practice_time")
    private Integer practiceTime; // 缁冧範鏃堕棿锛堝垎閽燂級
    
    @Min(value = 0, message = "绗旂敾鏁颁笉鑳戒负璐熸暟")
    @Column(name = "strokes_count")
    private Integer strokesCount;
    
    @Min(value = 0, message = "缁冧範瀛楁暟涓嶈兘涓鸿礋鏁?)
    @Column(name = "characters_practiced")
    private Integer charactersPracticed;
    
    @Size(max = 50, message = "缁冧範绫诲瀷闀垮害涓嶈兘瓒呰繃50涓瓧绗?)
    @Column(name = "practice_type", length = 50)
    private String practiceType;
    
    @Size(max = 500, message = "鍐呭闀垮害涓嶈兘瓒呰繃500涓瓧绗?)
    @Column(name = "content", length = 500)
    private String content; // 缁冧範鍐呭鎴栫瑪璁?    
    @Column(name = "image_url", length = 500)
    private String imageUrl; // 缁冧範鎴浘鎴栦綔鍝佸浘鐗?    
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;
    
    @Column(name = "completion_rate")
    private Double completionRate; // 瀹屾垚搴︾櫨鍒嗘瘮
    
    @Column(name = "difficulty_level")
    private Integer difficultyLevel; // 闅惧害绛夌骇 1-5
    
    @Column(name = "satisfaction_score")
    private Integer satisfactionScore; // 婊℃剰搴﹁瘎鍒?1-10
    
    @Column(name = "practice_date")
    private LocalDateTime practiceDate; // 缁冧範鏃ユ湡
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 棰勬寔涔呭寲鍥炶皟
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
    
    // 涓氬姟鏂规硶
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
            return "鏈褰?;
        }
        if (practiceTime < 60) {
            return practiceTime + "鍒嗛挓";
        } else {
            int hours = practiceTime / 60;
            int minutes = practiceTime % 60;
            if (minutes == 0) {
                return hours + "灏忔椂";
            } else {
                return hours + "灏忔椂" + minutes + "鍒嗛挓";
            }
        }
    }
}


