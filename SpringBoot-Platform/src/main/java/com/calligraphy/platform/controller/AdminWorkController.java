package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.WorkDto;
import com.calligraphy.platform.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管理端作品查询，对应契约的 adminPendingWorks 一个 operation。
 *
 * <p><b>为什么需要它</b>：公开画廊 {@code GET /api/works} 只返回「已发布且已审核通过」的作品，
 * 所以后台的「作品审核」列表如果也用它，**待审队列根本取不到** ——
 * 刚上传的作品是 DRAFT+PENDING，永远不在列表里，审核功能等于废的。
 * 这个端点返回的是「还没审 / 等复审」的作品（对应实体 {@code Work.canBeReviewed()}）。
 *
 * <p>成功响应是分页信封 {@code {content,page,size,total,totalPages}}。
 * 失败响应由 exception.GlobalExceptionHandler 统一转成 {code, message}。
 *
 * <p>鉴权由 SecurityConfig 的 {@code /api/admin/** → hasAnyRole("ADMIN","SUPER_ADMIN")}
 * 负责，这里**不**加注解式鉴权（项目没有用 @PreAuthorize）。
 */
@RestController
@RequestMapping("/api/admin/works")
@RequiredArgsConstructor
public class AdminWorkController {

    private static final int MAX_PAGE_SIZE = 100;

    private final WorkService workService;

    /**
     * GET /api/admin/works/pending —— 200 分页信封
     *
     * <p>待审核作品：review_status 为 PENDING 或 NEEDS_REVISION。
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> pendingWorks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<WorkDto> works = workService.getPendingReviewWorks(pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(works));
    }

    // ------------------------------------------------------------ 内部工具

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
