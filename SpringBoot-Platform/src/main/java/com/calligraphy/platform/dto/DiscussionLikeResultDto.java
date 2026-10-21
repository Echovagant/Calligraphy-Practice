package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点赞结果。对应契约的 {@code LikeResult} schema：{@code liked,likeCount}。
 *
 * <p>裸对象，没有 data 信封。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionLikeResultDto {

    /** 点赞之后的状态：true 已点赞，false 已取消点赞。 */
    private boolean liked;

    /** 点赞后的计数。 */
    private Integer likeCount;
}
