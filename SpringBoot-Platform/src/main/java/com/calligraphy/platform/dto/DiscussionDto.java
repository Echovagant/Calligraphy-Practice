package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 讨论。对应契约 openapi/contract.yaml 的 {@code Discussion} schema。
 *
 * <p>字段集严格对齐契约：{@code id,title,content,category,userId,username,userAvatar,
 * status,viewCount,likeCount,replyCount,isPinned,isFeatured,tags,createdAt,updatedAt,replies}。
 * 多一个字段少一个字段都会让契约测试炸出来，所以这里只放契约里有的东西。
 *
 * <p>两个易踩的坑：
 * <ul>
 *   <li>主键叫 {@code id}（不是 discussionId）—— 前端读的是 {@code discussion.id}。</li>
 *   <li>{@code isPinned}/{@code isFeatured} 必须用包装类型 {@code Boolean}：
 *       Lombok 生成 {@code getIsPinned()}，Jackson 的属性名仍是 {@code isPinned}；
 *       换成基本类型 boolean 会生成 {@code isPinned()}，属性名变成 {@code pinned}，与契约不符。</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionDto {

    /** 讨论主键。契约里主键字段一律叫 id。 */
    private String id;

    private String title;
    private String content;
    private String category;

    /** 作者 id（引用别人的 id，保留原名）。 */
    private String userId;
    private String username;
    private String userAvatar;

    /** ACTIVE / INACTIVE / DELETED。 */
    private String status;

    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;

    /** 置顶。用包装类型 Boolean，Jackson 属性名才是 isPinned。 */
    private Boolean isPinned;

    /** 精华。用包装类型 Boolean，Jackson 属性名才是 isFeatured。 */
    private Boolean isFeatured;

    /** 逗号分隔的标签字符串（契约里就是字符串，不是数组）。 */
    private String tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 嵌套的回复数组。列表类端点给空数组，详情端点给真实回复。 */
    private List<DiscussionReplyDto> replies;
}
