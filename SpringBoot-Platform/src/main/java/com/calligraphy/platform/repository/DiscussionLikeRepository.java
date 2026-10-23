package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.DiscussionLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 点赞记录的仓储。
 *
 * <p>去重依据 = (discussion_id, user_id)。唯一约束在数据库层兜底，
 * 这里再查一次是为了让「点赞 / 取消点赞」变成可切换的操作。
 */
@Repository
public interface DiscussionLikeRepository extends JpaRepository<DiscussionLike, Long> {

    /** 查某个用户对某个讨论的点赞记录。 */
    Optional<DiscussionLike> findByDiscussionIdAndUserId(Long discussionId, String userId);

    /**
     * 批量查某个用户对一批讨论的点赞记录 —— 列表端点填充 {@code liked} 字段用。
     *
     * <p>必须批量：列表一页 20 条，逐条查就是 20 条 SQL（N+1）。
     * 这里一次 IN 查询把当前页的讨论 id 全部取回来，在内存里配对。
     */
    List<DiscussionLike> findByDiscussionIdInAndUserId(List<Long> discussionIds, String userId);

    /** 删掉某个讨论下的全部点赞记录（讨论被清理时用）。 */
    @Transactional
    long deleteByDiscussionId(Long discussionId);
}
