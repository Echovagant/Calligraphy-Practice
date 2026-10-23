package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.CopybookCreateRequestDto;
import com.calligraphy.platform.dto.CopybookDto;
import com.calligraphy.platform.exception.BusinessException;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.exception.UserNotFoundException;
import com.calligraphy.platform.model.Copybook;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.CopybookRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.service.CopybookService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 字帖服务实现。
 *
 * <p>列表与热门榜都只返回 {@code is_active = true} 的字册 —— 下架的字帖不该出现在前台。
 * 过滤条件（分类 / 难度）用 Specification 组合，避免为每种组合各写一条派生查询。
 *
 * <p>另外两件事：
 * <ul>
 *   <li><b>自建字帖不进公开列表</b>：用户自己生成的字帖只在自己账号下可见，
 *       混进公共列表等于把某个人的草稿推给所有人。</li>
 *   <li><b>自建字帖有封面图</b>：{@code copybooks.image_url} 是 NOT NULL，
 *       自建字帖没有上传封面，所以落库时给一张占位图（前端 public/sample 下的三张之一），
 *       而不是把列改成可空 —— 改已有库的列约束并不可靠。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CopybookServiceImpl implements CopybookService {

    /**
     * 自建字帖的占位封面。copybooks.image_url 是 NOT NULL，
     * 而自建字帖没有上传封面 —— 指向前端 public/sample 下真实存在的静态资源。
     */
    private static final String CUSTOM_COVER_IMAGE_URL = "/sample/copybook-1.png";

    private final CopybookRepository copybookRepository;
    private final UserRepository userRepository;

    @Override
    public Page<CopybookDto> getCopybooks(Pageable pageable, String category, String difficulty) {
        return copybookRepository.findAll(featuredSpec(category, difficulty), pageable).map(this::toDto);
    }

    @Override
    public Page<CopybookDto> getPopularCopybooks(Pageable pageable) {
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "usageCount").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        return copybookRepository.findAll(featuredSpec(null, null), sorted).map(this::toDto);
    }

    @Override
    public CopybookDto getCopybookById(String copybookId) {
        Copybook copybook = copybookRepository.findById(copybookId)
                .orElseThrow(() -> new ResourceNotFoundException("字帖不存在: " + copybookId));
        return toDto(copybook);
    }

    @Override
    @Transactional
    public CopybookDto createCopybook(CopybookCreateRequestDto request, String username) {
        String title = trimToNull(request.getTitle());
        String content = trimToNull(request.getContent());
        if (title == null) {
            throw new BusinessException("字帖标题不能为空");
        }
        if (content == null) {
            throw new BusinessException("练习内容不能为空");
        }
        if (title.length() > 200) {
            throw new BusinessException("字帖标题长度不能超过200个字符");
        }
        if (content.length() > 10000) {
            throw new BusinessException("练习内容长度不能超过10000个字符");
        }

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("用户不存在: " + username));

        Copybook copybook = new Copybook();
        copybook.setTitle(title);
        copybook.setContent(content);
        copybook.setDescription(request.getDescription());
        copybook.setImageUrl(CUSTOM_COVER_IMAGE_URL);
        copybook.setFont(request.getFont());
        copybook.setLayout(request.getLayout());
        copybook.setCharactersPerPage(request.getCharactersPerPage());
        copybook.setFontSize(request.getFontSize());
        copybook.setPaperColor(request.getPaperColor());
        copybook.setGridColor(request.getGridColor());
        copybook.setCategory(request.getCategory());
        copybook.setDifficulty(request.getDifficulty());
        // 字数按「去掉空白后的字符数」算 —— 生成的正文里换行与空格不算练习的字。
        copybook.setCharacterCount(content.replaceAll("\\s", "").length());
        copybook.setUsageCount(0);
        copybook.setIsActive(true);
        copybook.setIsCustom(true);
        copybook.setUserId(owner.getId());

        Copybook saved = copybookRepository.save(copybook);
        return toDto(saved);
    }

    @Override
    public Page<CopybookDto> getMyCopybooks(String userId, Pageable pageable) {
        return copybookRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toDto);
    }

    // ------------------------------------------------------------ 内部工具

    /**
     * 公开可见的字帖：启用中的**精选**字帖（{@code is_custom} 不是 true），
     * 再加上可选的分类 / 难度过滤。
     *
     * <p>{@code is_custom} 允许为 NULL —— ddl-auto=update 给老库补列时不会补默认值，
     * 补出来的历史行这一列是 NULL。NULL 的行是播种的精选字帖，要照常出现在公开列表里，
     * 所以这里判「不是 true」而不是「等于 false」。
     */
    private Specification<Copybook> featuredSpec(String category, String difficulty) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            predicates.add(cb.or(
                    cb.isNull(root.get("isCustom")),
                    cb.isFalse(root.get("isCustom"))));
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (difficulty != null && !difficulty.isBlank()) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CopybookDto toDto(Copybook copybook) {
        return CopybookDto.builder()
                .id(copybook.getId())
                .title(copybook.getTitle())
                .description(copybook.getDescription())
                .imageUrl(copybook.getImageUrl())
                .difficulty(copybook.getDifficulty())
                .category(copybook.getCategory())
                .characterCount(copybook.getCharacterCount())
                .usageCount(copybook.getUsageCount())
                .isActive(copybook.getIsActive())
                .isCustom(copybook.getIsCustom())
                .content(copybook.getContent())
                .font(copybook.getFont())
                .layout(copybook.getLayout())
                .charactersPerPage(copybook.getCharactersPerPage())
                .fontSize(copybook.getFontSize())
                .paperColor(copybook.getPaperColor())
                .gridColor(copybook.getGridColor())
                .userId(copybook.getUserId())
                .createdAt(copybook.getCreatedAt())
                .updatedAt(copybook.getUpdatedAt())
                .build();
    }
}
