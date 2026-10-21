package com.calligraphy.platform.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 谁赞了哪个讨论 —— 点赞去重的最小依据。
 *
 * <p>契约的 {@code discussionLike} 端点是「点赞 / 取消点赞」，响应里有 {@code liked} 布尔值。
 * 没有这张表就没法知道「当前用户是不是已经赞过」，也就没法既支持取消点赞、
 * 又保证同一个用户的重复点赞不会把 likeCount 一次次往上加。
 *
 * <p>唯一约束 (discussion_id, user_id) 是并发下的最后一道防线：
 * 同一个用户对同一个讨论最多只有一条记录。
 *
 * <p>刻意不建 {@code @ManyToOne} 关联：只存两个普通列，删用户时不会因为外键
 * 约束卡住（代价是删用户后会留下孤儿点赞记录，计数会略微偏高）。
 */
@Entity
@Table(name = "discussion_likes", uniqueConstraints = @UniqueConstraint(
        name = "uk_discussion_likes_discussion_user",
        columnNames = {"discussion_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "discussion_id", nullable = false)
    private Long discussionId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
