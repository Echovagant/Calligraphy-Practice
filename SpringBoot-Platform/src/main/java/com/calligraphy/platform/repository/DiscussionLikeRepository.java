package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.DiscussionLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    /** 删掉某个讨论下的全部点赞记录（讨论被清理时用）。 */
    @Transactional
    long deleteByDiscussionId(Long discussionId);
}
