package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.FileUploadResponseDto;
import com.calligraphy.platform.dto.WorkCreateFormDto;
import com.calligraphy.platform.dto.WorkCreateRequestDto;
import com.calligraphy.platform.dto.WorkDto;
import com.calligraphy.platform.dto.WorkImageFormDto;
import com.calligraphy.platform.dto.WorkUpdateRequestDto;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.service.FileUploadService;
import com.calligraphy.platform.service.WorkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 作品接口，对应契约的 workCreate / workList / workListByUser / workGetById /
 * workUpdate / workUpdateImage / workDelete 七个 operation。
 *
 * <p>成功响应：单资源是裸 WorkDto；列表是
 * {@code {content,page,size,total,totalPages}}。
 * 失败响应由 exception.GlobalExceptionHandler 统一转成 {code, message}。
 */
@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 列表默认排序：最新在前。
     *
     * <p>没有排序时分页顺序由数据库自己决定，翻页可能重复或漏掉；
     * id 作为次级排序键，保证 createdAt 相同的作品也有稳定顺序。
     */
    private static final Sort DEFAULT_SORT =
            Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));

    private final WorkService workService;
    private final FileUploadService fileUploadService;

    /**
     * POST /api/works —— 201 裸 Work
     *
     * <p>multipart 字段：{@code title}（必填）、{@code description}、{@code image}。
     * 图片先传上去拿到 fileUrl，再进作品记录 —— 这样作品的 imageUrl 才有值。
     */
    @PostMapping
    public ResponseEntity<WorkDto> createWork(@Valid @ModelAttribute WorkCreateFormDto form) {
        String username = currentUsername();

        List<String> fileUrls = new ArrayList<>();
        if (form.getImage() != null && !form.getImage().isEmpty()) {
            FileUploadResponseDto uploaded = fileUploadService.uploadWorkFile(form.getImage(), username);
            fileUrls.add(uploaded.getFileUrl());
        }

        // works.content 是 NOT NULL，而前端不传它 —— 用描述兜底
        String content = form.getContent() != null && !form.getContent().isBlank()
                ? form.getContent()
                : (form.getDescription() != null ? form.getDescription() : "");

        WorkCreateRequestDto request = WorkCreateRequestDto.builder()
                .title(form.getTitle())
                .description(form.getDescription())
                .content(content)
                .category(form.getCategory())
                .tags(parseTags(form.getTags()))
                .fileUrls(fileUrls)
                .build();

        WorkDto created = workService.createWork(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/works —— 200 分页信封
     *
     * <p>公开画廊：只展示已发布且已审核通过的作品（草稿不会外泄）。
     * 新上传的作品是 DRAFT + PENDING，先出现在作者的作品列表里；
     * 审核通过后才进公开画廊（审核闭环属于 file-reviews 域）。
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listWorks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<WorkDto> works = workService.getPublicWorks(pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(works));
    }

    /**
     * GET /api/works/user/{userId} —— 200 分页信封
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> listUserWorks(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<WorkDto> works = workService.getWorksByUserId(userId, pageable(page, size));
        return ResponseEntity.ok(toPageEnvelope(works));
    }

    /**
     * GET /api/works/{workId} —— 200 裸 Work
     */
    @GetMapping("/{workId}")
    public ResponseEntity<WorkDto> getWork(@PathVariable String workId) {
        return ResponseEntity.ok(workService.getWorkById(parseWorkId(workId)));
    }

    /**
     * PUT /api/works/{workId} —— 200 裸 Work
     */
    @PutMapping("/{workId}")
    public ResponseEntity<WorkDto> updateWork(
            @PathVariable String workId,
            @Valid @RequestBody WorkUpdateRequestDto request) {
        WorkDto updated = workService.updateWork(parseWorkId(workId), request, currentUsername());
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/works/{workId}/image —— 200 FileUploadResult
     *
     * <p>multipart 字段名是 {@code image}。
     *
     * <p>上传只做一次：由 workService.addFileToWork 负责鉴权 + 上传 + 落库，
     * 返回的 fileUrl 就是写进 works.image_url 的那个 URL。
     * （以前这里先传一遍、Service 内部又传一遍：磁盘上多一份孤儿文件，
     * 而且响应里的 fileUrl 指的根本不是作品主图。）
     */
    @PostMapping("/{workId}/image")
    public ResponseEntity<FileUploadResponseDto> updateImage(
            @PathVariable String workId,
            @Valid @ModelAttribute WorkImageFormDto form) {
        Long id = parseWorkId(workId);
        FileUploadResponseDto uploaded = workService.addFileToWork(id, form.getImage(), currentUsername());
        return ResponseEntity.ok(uploaded);
    }

    /**
     * DELETE /api/works/{workId} —— 204 无响应体
     */
    @DeleteMapping("/{workId}")
    public ResponseEntity<Void> deleteWork(@PathVariable String workId) {
        workService.deleteWork(parseWorkId(workId), currentUsername());
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
     * 解析失败是客户端错误（400），不能变成 500。
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
        return PageRequest.of(Math.max(page, 0), safeSize, DEFAULT_SORT);
    }

    /** 统一分页信封 {content,page,size,total,totalPages}。 */
    private Map<String, Object> toPageEnvelope(Page<WorkDto> page) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", page.getContent());
        body.put("page", page.getNumber());
        body.put("size", page.getSize());
        body.put("total", page.getTotalElements());
        body.put("totalPages", page.getTotalPages());
        return body;
    }

    /** 表单里的 tags 是逗号分隔的字符串。 */
    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        List<String> parsed = new ArrayList<>();
        for (String tag : tags.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                parsed.add(trimmed);
            }
        }
        return parsed;
    }
}
