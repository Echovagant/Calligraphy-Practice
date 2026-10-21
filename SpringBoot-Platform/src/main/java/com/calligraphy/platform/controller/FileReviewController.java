package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.FileReviewBatchDto;
import com.calligraphy.platform.dto.FileReviewBatchResultDto;
import com.calligraphy.platform.dto.FileReviewDto;
import com.calligraphy.platform.dto.FileReviewRequestDto;
import com.calligraphy.platform.dto.FileReviewUpdateDto;
import com.calligraphy.platform.exception.AuthenticationException;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.service.FileReviewService;
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
 * 管理端作品审核接口，对应契约的 fileReviewCreate / fileReviewBatch /
 * fileReviewMyReviews / fileReviewGetByWorkId / fileReviewGetById /
 * fileReviewUpdate / fileReviewDelete 七个 operation。
 *
 * <p>成功响应：单资源是裸 FileReviewDto；列表是
 * {@code {content,page,size,total,totalPages}}；删除是 204 无响应体。
 * 失败响应由 exception.GlobalExceptionHandler 统一转成 {code, message}。
 *
 * <p>鉴权由 SecurityConfig 的 {@code /api/admin/** → hasAnyRole("ADMIN","SUPER_ADMIN")}
 * 负责，这里**不**加注解式鉴权（项目没有用 @PreAuthorize）。
 *
 * <p>路径优先级：{@code /my-reviews}、{@code /batch}、{@code /work/{workId}}
 * 都是字面量或更具体的路径，Spring 会让它们优先于 {@code /{reviewId}} 模板路径。
 */
@RestController
@RequestMapping("/api/admin/file-reviews")
@RequiredArgsConstructor
public class FileReviewController {

    private static final int MAX_PAGE_SIZE = 100;

    private final FileReviewService fileReviewService;

    /**
     * POST /api/admin/file-reviews —— 201 裸 FileReview
     *
     * <p>审核结果会回写 works.review_status；作品不存在 → 404。
     */
    @PostMapping
    public ResponseEntity<FileReviewDto> createReview(@Valid @RequestBody FileReviewRequestDto request) {
        FileReviewDto created = fileReviewService.createReview(request, currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * POST /api/admin/file-reviews/batch —— 200 FileReviewBatchResult
     *
     * <p>某个作品失败只计入 failed，整个请求仍然是 200。
     */
    @PostMapping("/batch")
    public ResponseEntity<FileReviewBatchResultDto> batchReview(@Valid @RequestBody FileReviewBatchDto batch) {
        return ResponseEntity.ok(fileReviewService.batchReviewWorks(batch, currentUsername()));
    }

    /**
     * GET /api/admin/file-reviews/my-reviews —— 200 分页信封
     *
     * <p>当前登录管理员提交的审核记录。
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<Map<String, Object>> myReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FileReviewDto> reviews = fileReviewService.getMyReviews(currentUsername(), pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(reviews));
    }

    /**
     * GET /api/admin/file-reviews/work/{workId} —— 200 裸 FileReview
     *
     * <p>某个作品的最新一条审核记录；作品或审核记录不存在 → 404。
     */
    @GetMapping("/work/{workId}")
    public ResponseEntity<FileReviewDto> getReviewByWorkId(@PathVariable String workId) {
        return ResponseEntity.ok(fileReviewService.getReviewByWorkId(parseWorkId(workId)));
    }

    /**
     * GET /api/admin/file-reviews/{reviewId} —— 200 裸 FileReview
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<FileReviewDto> getReview(@PathVariable String reviewId) {
        return ResponseEntity.ok(fileReviewService.getReviewById(reviewId));
    }

    /**
     * PUT /api/admin/file-reviews/{reviewId} —— 200 裸 FileReview
     *
     * <p>更新审核结论并回写 works.review_status。
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<FileReviewDto> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody FileReviewUpdateDto request) {
        return ResponseEntity.ok(fileReviewService.updateReview(reviewId, request, currentUsername()));
    }

    /**
     * DELETE /api/admin/file-reviews/{reviewId} —— 204 无响应体
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        fileReviewService.deleteReview(reviewId, currentUsername());
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
