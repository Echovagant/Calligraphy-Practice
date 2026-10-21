package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.CopybookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 字帖服务，对应契约的 copybookList / copybookPopular / copybookGetById 三个 operation。
 */
public interface CopybookService {

    /**
     * 字帖列表（只返回启用中的字帖）。
     *
     * @param category    分类过滤，null 或空白表示不过滤
     * @param difficulty  难度过滤，null 或空白表示不过滤
     */
    Page<CopybookDto> getCopybooks(Pageable pageable, String category, String difficulty);

    /** 热门字帖，按使用次数倒序。 */
    Page<CopybookDto> getPopularCopybooks(Pageable pageable);

    /** 字帖详情，不存在抛 404。 */
    CopybookDto getCopybookById(String copybookId);
}
