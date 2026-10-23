package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.CopybookCreateRequestDto;
import com.calligraphy.platform.dto.CopybookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 字帖服务，对应契约的 copybookList / copybookPopular / copybookGetById
 * 三个既有 operation，外加新增的 copybookCreate / copybookListMine 两个。
 */
public interface CopybookService {

    /**
     * 字帖列表（只返回启用中的**精选**字帖，不含用户自建字帖）。
     *
     * @param category    分类过滤，null 或空白表示不过滤
     * @param difficulty  难度过滤，null 或空白表示不过滤
     */
    Page<CopybookDto> getCopybooks(Pageable pageable, String category, String difficulty);

    /** 热门字帖，按使用次数倒序（同样不含用户自建字帖）。 */
    Page<CopybookDto> getPopularCopybooks(Pageable pageable);

    /** 字帖详情，不存在抛 404。 */
    CopybookDto getCopybookById(String copybookId);

    /**
     * 保存一份自建字帖到当前账号。
     *
     * <p>自建字帖不进公开字帖列表，只在 {@code getMyCopybooks} 里出现。
     *
     * @param request  自建字帖的内容与排版参数
     * @param username 当前登录用户名，字帖归属他
     */
    CopybookDto createCopybook(CopybookCreateRequestDto request, String username);

    /**
     * 「我的字帖」：某个用户保存的自建字帖，按创建时间倒序，分页。
     *
     * @param userId   归属用户的 id（users.id）
     * @param pageable 分页参数
     */
    Page<CopybookDto> getMyCopybooks(String userId, Pageable pageable);
}
