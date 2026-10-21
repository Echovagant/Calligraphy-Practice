package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.Work;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkDto {

    /**
     * 作品主键。契约（openapi/contract.yaml 的 Work schema）里主键字段名叫 {@code id}
     * —— 前端读的就是 {@code work.id}，所以 JSON 名用 id，Java 侧保留 workId。
     */
    @JsonProperty("id")
    private String workId;

    /** 作者 id（引用别人的 id，保留原名）。 */
    private String userId;
    private String username;
    private String title;
    private String description;
    private String content;
    private String category;
    private List<String> tags;

    /** 主图 URL（前端 GalleryView / MyWorksView 读的是它）。 */
    private String imageUrl;

    private List<String> fileUrls;
    private Work.WorkStatus status;
    private Work.ReviewStatus reviewStatus;
    private String reviewerId;
    private String reviewerComment;
    private long viewCount;
    private long likeCount;

    /** 同上，Jackson 默认会输出 {@code featured}，契约要求 {@code isFeatured}。 */
    @JsonProperty("isFeatured")
    private boolean isFeatured;

    private LocalDateTime publishedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 用户信息
    private String userNickname;
    private String userAvatar;

    // 审核者信息
    private String reviewerUsername;
    private String reviewerNickname;

    // 统计信息
    private long commentCount;
    private double popularityScore;
}


