package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.CopybookDto;
import com.calligraphy.platform.dto.PracticeRecordCreateDto;
import com.calligraphy.platform.dto.PracticeRecordDto;
import com.calligraphy.platform.dto.PracticeStatisticsDto;
import com.calligraphy.platform.exception.AuthenticationException;
import com.calligraphy.platform.service.CopybookService;
import com.calligraphy.platform.service.PracticeRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * 字帖与练习接口，对应契约的 copybookList / copybookPopular / copybookUserStatistics /
 * copybookPracticeHistory / copybookGetById / copybookRecordPractice 六个 operation。
 *
 * <p>成功响应：单资源是裸对象；列表是 {@code {content,page,size,total,totalPages}}。
 * 失败响应由 exception.GlobalExceptionHandler 统一转成 {code, message}。
 *
 * <p>注意路径优先级：{@code /popular} 是字面量路径，Spring 会让它优先于
 * {@code /{copybookId}} 模板路径 —— 契约测试必须断言这两条互不吞掉。
 */
@RestController
@RequestMapping("/api/copybooks")
@RequiredArgsConstructor
public class CopybookController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CopybookService copybookService;
    private final PracticeRecordService practiceRecordService;

    /**
     * GET /api/copybooks —— 200 分页信封
     *
     * <p>公开：字帖列表是前台内容，不需要令牌。
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listCopybooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty) {
        Page<CopybookDto> copybooks = copybookService.getCopybooks(pageable(page, size), category, difficulty);
        return ResponseEntity.ok(toPageEnvelope(copybooks));
    }

    /**
     * GET /api/copybooks/popular —— 200 分页信封
     *
     * <p>公开：热门字帖。
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> listPopularCopybooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CopybookDto> popular = copybookService.getPopularCopybooks(pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(popular));
    }

    /**
     * GET /api/copybooks/statistics/user —— 200 练习统计
     *
     * <p>需要登录：统计的是「当前用户」的数据。
     */
    @GetMapping("/statistics/user")
    public ResponseEntity<PracticeStatisticsDto> getUserStatistics() {
        return ResponseEntity.ok(practiceRecordService.getUserStatistics(currentUsername()));
    }

    /**
     * GET /api/copybooks/practice/history —— 200 分页信封
     *
     * <p>需要登录：练习历史是「当前用户」的数据。
     */
    @GetMapping("/practice/history")
    public ResponseEntity<Map<String, Object>> getPracticeHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<PracticeRecordDto> history =
                practiceRecordService.getPracticeHistory(currentUsername(), pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(history));
    }

    /**
     * GET /api/copybooks/{copybookId} —— 200 裸字帖
     *
     * <p>公开：字帖详情。
     */
    @GetMapping("/{copybookId}")
    public ResponseEntity<CopybookDto> getCopybook(@PathVariable String copybookId) {
        return ResponseEntity.ok(copybookService.getCopybookById(copybookId));
    }

    /**
     * POST /api/copybooks/{copybookId}/practice —— 201 裸练习记录
     *
     * <p>需要登录：练习记录归属当前用户，用户身份从令牌取。
     */
    @PostMapping("/{copybookId}/practice")
    public ResponseEntity<PracticeRecordDto> recordPractice(
            @PathVariable String copybookId,
            @Valid @RequestBody PracticeRecordCreateDto request) {
        PracticeRecordDto created = practiceRecordService.recordPractice(currentUsername(), copybookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
