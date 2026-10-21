package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.CommentCreateRequestDto;
import com.calligraphy.platform.dto.CommentDto;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.service.CommentService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 评论接口，对应契约的 commentCreate / commentListByWork / commentListByUser /
 * commentDelete 四个 operation。
 *
 * <p>成功响应：单资源是裸 CommentDto；列表是 {@code {content,page,size,total,totalPages}}。
 * 失败响应由 exception.GlobalExceptionHandler 统一转成 {code, message}。
 *
 * <p>主键字段一律叫 {@code id}（契约统一），不是 commentId。
 */
@RestController
@RequestMapping("/api/community/comments")
@RequiredArgsConstructor
public class CommentController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CommentService commentService;

    /**
     * POST /api/community/comments —— 201 裸 Comment
     *
     * <p>需要登录：评论归属当前用户，用户身份从令牌取（令牌里装的是用户名）。
     */
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentCreateRequestDto request) {
        CommentDto created = commentService.createComment(currentUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/community/comments/work/{workId} —— 200 分页信封
     *
     * <p>公开：作品评论列表不需要令牌。
     */
    @GetMapping("/work/{workId}")
    public ResponseEntity<Map<String, Object>> listCommentsByWork(
            @PathVariable String workId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CommentDto> comments = commentService.getCommentsByWorkId(parseWorkId(workId), pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(comments));
    }

    /**
     * GET /api/community/comments/user/{userId} —— 200 分页信封
     *
     * <p>需要登录：用户评论历史属于「当前用户」相关的数据。
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> listCommentsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CommentDto> comments = commentService.getCommentsByUserId(userId, pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(comments));
    }

    /**
     * DELETE /api/community/comments/{commentId} —— 204 无响应体
     *
     * <p>需要登录：只有评论作者本人或管理员能删，越权返回 403。
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId, currentUsername());
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------ 内部工具

    /** 当前登录用户名。 */
    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new com.calligraphy.platform.exception.AuthenticationException("未认证或令牌无效");
        }
        return authentication.getName();
    }

    /**
     * workId 是字符串路径参数，实体主键是 Long。
     * 解析失败是客户端错误（不能变成 500）。
     */
    private Long parseWorkId(String workId) {
        try {
            return Long.valueOf(workId);
        } catch (NumberFormatException e) {
            throw new BusinessException("作品 ID 格式不正确: " + workId);
        }
    }

    private Pageable pageable(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(page, 0), safeSize);
    }

    /** 统一分页信封 {content,page,size,total,totalPages}。 */
    private Map<String, Object> toPageEnvelope(Page<CommentDto> page) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", page.getContent());
        body.put("page", page.getNumber());
        body.put("size", page.getSize());
        body.put("total", page.getTotalElements());
        body.put("totalPages", page.getTotalPages());
        return body;
    }
}
