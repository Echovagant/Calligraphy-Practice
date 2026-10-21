package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.DiscussionCreateDto;
import com.calligraphy.platform.dto.DiscussionDto;
import com.calligraphy.platform.dto.DiscussionLikeResultDto;
import com.calligraphy.platform.dto.DiscussionReplyCreateDto;
import com.calligraphy.platform.dto.DiscussionReplyDto;
import com.calligraphy.platform.dto.DiscussionReplyUpdateDto;
import com.calligraphy.platform.dto.DiscussionUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 讨论域的服务接口。
 *
 * <p>一个方法对应契约里的一个 operation（14 个端点 ↔ 14 个方法）。
 * 契约里没有对应端点的方法（按用户查、按父回复查、计数、置顶/加精的管理动作等）
 * 已连同实现删掉。
 *
 * <p>当前用户一律用**用户名**（令牌里装的是用户名，不是 id）。
 */
public interface DiscussionService {

    /**
     * 讨论列表（{@code discussionList}）。
     *
     * @param pageable 分页（page / size 由控制器夹到合法范围）
     * @param category 分类过滤，可空
     * @param keyword  关键词过滤（标题 / 正文 / 标签），可空
     * @param sort     排序方式，可空。取值：{@code latest}（默认）、{@code hot}、
     *                 {@code mostLiked}、{@code mostReplied}、{@code mostViewed}；
     *                 认不出来的值按 {@code latest} 处理
     */
    Page<DiscussionDto> getDiscussions(Pageable pageable, String category, String keyword, String sort);

    /** 发帖（{@code discussionCreate}）。 */
    DiscussionDto createDiscussion(String username, DiscussionCreateDto createDto);

    /** 热门讨论（{@code discussionHot}）。 */
    Page<DiscussionDto> getHotDiscussions(Pageable pageable);

    /** 精华讨论（{@code discussionFeatured}）。 */
    Page<DiscussionDto> getFeaturedDiscussions(Pageable pageable);

    /** 置顶讨论（{@code discussionPinned}）。 */
    Page<DiscussionDto> getPinnedDiscussions(Pageable pageable);

    /** 搜索讨论（{@code discussionSearch}）。 */
    Page<DiscussionDto> searchDiscussions(String keyword, Pageable pageable);

    /** 讨论详情（{@code discussionGetById}），带回复列表。 */
    DiscussionDto getDiscussionById(String discussionId);

    /** 编辑帖子（{@code discussionUpdate}）。只有作者本人或管理员能改。 */
    DiscussionDto updateDiscussion(String discussionId, String username, DiscussionUpdateDto updateDto);

    /** 删除帖子（{@code discussionDelete}）。只有作者本人或管理员能删。 */
    void deleteDiscussion(String discussionId, String username);

    /** 点赞 / 取消点赞（{@code discussionLike}）。 */
    DiscussionLikeResultDto toggleLike(String discussionId, String username);

    /** 某个讨论的回复列表（{@code discussionReplyList}）。 */
    Page<DiscussionReplyDto> getRepliesByDiscussionId(String discussionId, Pageable pageable);

    /** 回复讨论（{@code discussionReplyCreate}）。 */
    DiscussionReplyDto createReply(String discussionId, String username, DiscussionReplyCreateDto createDto);

    /** 编辑回复（{@code discussionReplyUpdate}）。只有作者本人或管理员能改。 */
    DiscussionReplyDto updateReply(String replyId, String username, DiscussionReplyUpdateDto updateDto);

    /** 删除回复（{@code discussionReplyDelete}）。只有作者本人或管理员能删。 */
    void deleteReply(String replyId, String username);
}
