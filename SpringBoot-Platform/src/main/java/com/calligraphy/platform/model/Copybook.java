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
@Table(name = "copybooks")
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
