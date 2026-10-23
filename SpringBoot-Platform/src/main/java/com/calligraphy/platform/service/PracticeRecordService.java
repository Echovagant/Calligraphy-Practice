package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.PracticeRecordCreateDto;
import com.calligraphy.platform.dto.PracticeRecordDto;
import com.calligraphy.platform.dto.PracticeStatisticsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * 练习记录服务，对应契约的 copybookRecordPractice / copybookPracticeHistory /
 * copybookUserStatistics 三个 operation。
 *
 * <p>接口从最初的 58 个方法裁剪到 5 个 —— 契约里没有对应端点的那些方法
 * （按字体风格查、按完成状态查、批量更新、练习建议……）全部删掉了。
 * 留着的 5 个方法每一个都有调用方。
 */
public interface PracticeRecordService {

    /**
     * 记录一次练习。
     *
     * @param username   当前登录用户（从令牌取，不接受客户端指定）
     * @param copybookId 字帖 ID
     * @param request    练习内容
     */
    PracticeRecordDto recordPractice(String username, String copybookId, PracticeRecordCreateDto request);

    /** 当前用户的练习历史，按练习时间倒序。 */
    Page<PracticeRecordDto> getPracticeHistory(String username, Pageable pageable);

    /**
     * 当前用户的练习历史 + 筛选（分页）。
     *
     * <p>筛选全部在数据库层做（不是查全量再在内存里过滤）：
     * <ul>
     *   <li>{@code from} / {@code to}：练习日期区间，**含边界**，格式 yyyy-MM-dd。
     *       {@code to} 那天的全部时刻都算在内。</li>
     *   <li>{@code category}：分类精确匹配。</li>
     *   <li>{@code keyword}：标题 / 内容 / 备注模糊匹配。</li>
     * </ul>
     *
     * @param username 当前登录用户
     * @param pageable 分页参数
     * @param from     起始日期（含），null 表示不限
     * @param to       结束日期（含），null 表示不限
     * @param category 分类精确匹配，null 或空白表示不过滤
     * @param keyword  模糊匹配关键词，null 或空白表示不过滤
     */
    Page<PracticeRecordDto> getPracticeHistory(String username, Pageable pageable,
                                                LocalDate from, LocalDate to,
                                                String category, String keyword);

    /** 当前用户的练习统计。 */
    PracticeStatisticsDto getUserStatistics(String username);

    /** 某用户的练习记录数。 */
    long countByUserId(String userId);

    /** 全站练习记录数。 */
    long countAll();
}
