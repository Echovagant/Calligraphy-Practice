package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.CommentCreateRequestDto;
import com.calligraphy.platform.dto.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 评论服务 —— 对应契约评论域的四个 operation。
 *
 * <p>契约（openapi/contract.yaml）里评论域只有：
 * {@code commentCreate} / {@code commentListByWork} / {@code commentListByUser} /
 * {@code commentDelete}。原先那 40 多个方法里没有对应端点的已随接口一起裁掉
 * （见 .superpowers/sdd/2026-10-21-p5-community/CONVENTIONS.md §5）。
 */
public interface CommentService {

    /**
     * 发表评论（commentCreate）。
     *
     * @param username      当前登录用户名（令牌里装的是用户名，不是 id）
     * @param createRequest 契约的 CommentCreateRequest（workId 是 string）
     */
    CommentDto createComment(String username, CommentCreateRequestDto createRequest);

    /**
     * 某个作品的评论列表（commentListByWork）。
     *
     * <p>公开端点：只返回审核通过的评论。
     */
    Page<CommentDto> getCommentsByWorkId(Long workId, Pageable pageable);

    /**
     * 某个用户的评论历史（commentListByUser）。
     */
    Page<CommentDto> getCommentsByUserId(String userId, Pageable pageable);

    /**
     * 删除评论（commentDelete）。只有作者本人或管理员能删。
     */
    void deleteComment(String commentId, String username);

    /**
     * 内容审核：评论正文是否命中敏感词。
     *
     * <p>敏感词清单来自配置 {@code comment.moderation.sensitive-words}，匹配忽略大小写。
     */
    boolean containsSensitiveContent(String content);
}
