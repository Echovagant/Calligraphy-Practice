package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 讨论的仓储。
 *
 * <p>只保留契约的 14 个端点真正用得到的方法：列表（含分类 / 关键词 / 排序）、
 * 热门、精华、置顶、按 id 取。计数、按用户查、按父回复查等没有对应端点的方法已删。
 *
 * <p>列表类查询一律用 {@code @Query} + {@link Pageable} 里的 {@code Sort}，
 * 这样「sort」参数才能在一条查询上切换排序，不用为每种排序再写一个方法。
 */
@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    /** 全部讨论（排除已删除）。排序由 Pageable 的 Sort 决定。 */
    @Query("SELECT d FROM Discussion d WHERE d.status != :status")
    Page<Discussion> findByStatus(@Param("status") Discussion.DiscussionStatus status, Pageable pageable);

    /** 按分类过滤。 */
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND d.category = :category")
    Page<Discussion> findByCategory(@Param("category") String category,
                                    @Param("status") Discussion.DiscussionStatus status,
                                    Pageable pageable);

    /** 关键词搜索：标题 / 正文 / 标签任一命中。 */
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND ("
            + "LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(d.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Discussion> searchByKeyword(@Param("keyword") String keyword,
                                      @Param("status") Discussion.DiscussionStatus status,
                                      Pageable pageable);

    /** 分类 + 关键词。 */
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND d.category = :category AND ("
            + "LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(d.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Discussion> searchByCategoryAndKeyword(@Param("category") String category,
                                                @Param("keyword") String keyword,
                                                @Param("status") Discussion.DiscussionStatus status,
                                                Pageable pageable);

    /** 热门：点赞数、回复数、浏览数依次降序。 */
    @Query("SELECT d FROM Discussion d WHERE d.status != :status "
            + "ORDER BY d.likeCount DESC, d.replyCount DESC, d.viewCount DESC, d.createdAt DESC")
    Page<Discussion> findHot(@Param("status") Discussion.DiscussionStatus status, Pageable pageable);

    /** 精华（加精）。 */
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND d.isFeatured = true "
            + "ORDER BY d.createdAt DESC")
    Page<Discussion> findFeatured(@Param("status") Discussion.DiscussionStatus status, Pageable pageable);

    /** 置顶。 */
    @Query("SELECT d FROM Discussion d WHERE d.status != :status AND d.isPinned = true "
            + "ORDER BY d.createdAt DESC")
    Page<Discussion> findPinned(@Param("status") Discussion.DiscussionStatus status, Pageable pageable);

    /** 取单个未删除的讨论；不存在（或已删除）返回空。 */
    Optional<Discussion> findByIdAndStatusNot(Long id, Discussion.DiscussionStatus status);
}
