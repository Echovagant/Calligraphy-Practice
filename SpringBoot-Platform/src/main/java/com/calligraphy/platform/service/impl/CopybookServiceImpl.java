package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.CopybookDto;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.model.Copybook;
import com.calligraphy.platform.repository.CopybookRepository;
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
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CopybookServiceImpl implements CopybookService {

    private final CopybookRepository copybookRepository;

    @Override
    public Page<CopybookDto> getCopybooks(Pageable pageable, String category, String difficulty) {
        return copybookRepository.findAll(activeSpec(category, difficulty), pageable).map(this::toDto);
    }

    @Override
    public Page<CopybookDto> getPopularCopybooks(Pageable pageable) {
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "usageCount").and(Sort.by(Sort.Direction.DESC, "createdAt")));
        return copybookRepository.findAll(activeSpec(null, null), sorted).map(this::toDto);
    }

    @Override
    public CopybookDto getCopybookById(String copybookId) {
        Copybook copybook = copybookRepository.findById(copybookId)
                .orElseThrow(() -> new ResourceNotFoundException("字帖不存在: " + copybookId));
        return toDto(copybook);
    }

    // ------------------------------------------------------------ 内部工具

    /** 启用中的字帖，加上可选的分类 / 难度过滤。 */
    private Specification<Copybook> activeSpec(String category, String difficulty) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isActive")));
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (difficulty != null && !difficulty.isBlank()) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
                .createdAt(copybook.getCreatedAt())
                .updatedAt(copybook.getUpdatedAt())
                .build();
    }
}
