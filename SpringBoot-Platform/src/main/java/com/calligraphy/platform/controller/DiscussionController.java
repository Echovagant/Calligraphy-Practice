package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.DiscussionCreateDto;
import com.calligraphy.platform.dto.DiscussionDto;
import com.calligraphy.platform.dto.DiscussionLikeResultDto;
import com.calligraphy.platform.dto.DiscussionReplyCreateDto;
import com.calligraphy.platform.dto.DiscussionReplyDto;
import com.calligraphy.platform.dto.DiscussionReplyUpdateDto;
import com.calligraphy.platform.dto.DiscussionUpdateDto;
import com.calligraphy.platform.exception.AuthenticationException;
import com.calligraphy.platform.service.DiscussionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 讨论接口，对应契约的 discussionList / discussionCreate / discussionHot /
 * discussionFeatured / discussionPinned / discussionSearch / discussionGetById /
 * discussionUpdate / discussionDelete / discussionLike / discussionReplyList /
 * discussionReplyCreate / discussionReplyUpdate / discussionReplyDelete
 * 十四个 operation。
 *
 * <p>成功响应：单资源是裸对象（没有 data 信封）；列表是
 * {@code {content,page,size,total,totalPages}}。
 * 失败响应由 exception.GlobalExceptionHandler 统一转成 {code, message}。
 *
 * <p>路径优先级：{@code /hot}、{@code /featured}、{@code /pinned}、{@code /search}
 * 是字面量路径，Spring 会让它们优先于 {@code /{id}} 模板路径 ——
 * 契约测试必须断言这两组互不吞掉。
 */
@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private static final int MAX_PAGE_SIZE = 100;

    private final DiscussionService discussionService;

    // ------------------------------------------------------------------ 公开读

    /**
     * GET /api/discussions —— 200 分页信封
     *
     * <p>公开：讨论列表是前台内容。
     *
     * @param category 分类过滤，可空
     * @param keyword  关键词过滤（标题 / 正文 / 标签），可空
     * @param sort     排序方式，可空（latest / hot / mostLiked / mostReplied / mostViewed）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort) {
        Page<DiscussionDto> discussions = discussionService.getDiscussions(
                pageable(page, size), category, keyword, sort);
        return ResponseEntity.ok(toPageEnvelope(discussions));
    }

    /**
     * GET /api/discussions/hot —— 200 分页信封
     *
     * <p>公开：热门讨论。
     */
    @GetMapping("/hot")
    public ResponseEntity<Map<String, Object>> listHotDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DiscussionDto> hot = discussionService.getHotDiscussions(pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(hot));
    }

    /**
     * GET /api/discussions/featured —— 200 分页信封
     *
     * <p>公开：精华讨论。
     */
    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> listFeaturedDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DiscussionDto> featured = discussionService.getFeaturedDiscussions(pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(featured));
    }

    /**
     * GET /api/discussions/pinned —— 200 分页信封
     *
     * <p>公开：置顶讨论。
     */
    @GetMapping("/pinned")
    public ResponseEntity<Map<String, Object>> listPinnedDiscussions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DiscussionDto> pinned = discussionService.getPinnedDiscussions(pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(pinned));
    }

    /**
     * GET /api/discussions/search —— 200 分页信封
     *
     * <p>公开：搜索讨论。keyword 是必填（契约里 required: true）。
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDiscussions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DiscussionDto> results = discussionService.searchDiscussions(keyword, pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(results));
    }

    /**
     * GET /api/discussions/{id} —— 200 裸讨论
     *
     * <p>公开：讨论详情（带回复列表）。
     */
    @GetMapping("/{id}")
    public ResponseEntity<DiscussionDto> getDiscussion(@PathVariable String id) {
        return ResponseEntity.ok(discussionService.getDiscussionById(id));
    }

    // ------------------------------------------------------------------ 需要登录

    /**
     * POST /api/discussions —— 201 裸讨论
     *
     * <p>需要登录：作者是当前用户。
     */
    @PostMapping
    public ResponseEntity<DiscussionDto> createDiscussion(@Valid @RequestBody DiscussionCreateDto request) {
        DiscussionDto created = discussionService.createDiscussion(currentUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/discussions/{id} —— 200 裸讨论
     *
     * <p>需要登录：只有作者本人或管理员能改，否则 403。
     */
    @PutMapping("/{id}")
    public ResponseEntity<DiscussionDto> updateDiscussion(
            @PathVariable String id,
            @Valid @RequestBody DiscussionUpdateDto request) {
        return ResponseEntity.ok(discussionService.updateDiscussion(id, currentUsername(), request));
    }

    /**
     * DELETE /api/discussions/{id} —— 204 无响应体
     *
     * <p>需要登录：只有作者本人或管理员能删，否则 403。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscussion(@PathVariable String id) {
        discussionService.deleteDiscussion(id, currentUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/discussions/{id}/like —— 200 裸点赞结果
     *
     * <p>需要登录：点赞 / 取消点赞，响应是 {liked, likeCount}。
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<DiscussionLikeResultDto> likeDiscussion(@PathVariable String id) {
        return ResponseEntity.ok(discussionService.toggleLike(id, currentUsername()));
    }

    // ------------------------------------------------------------------ 回复

    /**
     * GET /api/discussions/{discussionId}/replies —— 200 分页信封
     *
     * <p>公开：某个讨论的回复列表。
     */
    @GetMapping("/{discussionId}/replies")
    public ResponseEntity<Map<String, Object>> listReplies(
            @PathVariable String discussionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DiscussionReplyDto> replies =
                discussionService.getRepliesByDiscussionId(discussionId, pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(replies));
    }

    /**
     * POST /api/discussions/{discussionId}/replies —— 201 裸回复
     *
     * <p>需要登录：回复人是当前用户。
     */
    @PostMapping("/{discussionId}/replies")
    public ResponseEntity<DiscussionReplyDto> createReply(
            @PathVariable String discussionId,
            @Valid @RequestBody DiscussionReplyCreateDto request) {
        DiscussionReplyDto created = discussionService.createReply(
                discussionId, currentUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/discussions/replies/{replyId} —— 200 裸回复
     *
     * <p>需要登录：只有作者本人或管理员能改，否则 403。
     */
    @PutMapping("/replies/{replyId}")
    public ResponseEntity<DiscussionReplyDto> updateReply(
            @PathVariable String replyId,
            @Valid @RequestBody DiscussionReplyUpdateDto request) {
        return ResponseEntity.ok(discussionService.updateReply(replyId, currentUsername(), request));
    }

    /**
     * DELETE /api/discussions/replies/{replyId} —— 204 无响应体
     *
     * <p>需要登录：只有作者本人或管理员能删，否则 403。
     */
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable String replyId) {
        discussionService.deleteReply(replyId, currentUsername());
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------ 内部工具

    /** 当前登录用户名。 */
    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AuthenticationException("未认证或令牌无效");
        }
        return authentication.getName();
    }

    private Pageable pageable(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(page, 0), safeSize);
    }

    /** 统一分页信封 {content,page,size,total,totalPages}。 */
    private Map<String, Object> toPageEnvelope(Page<?> page) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", page.getContent());
        body.put("page", page.getNumber());
        body.put("size", page.getSize());
        body.put("total", page.getTotalElements());
        body.put("totalPages", page.getTotalPages());
        return body;
    }
}
