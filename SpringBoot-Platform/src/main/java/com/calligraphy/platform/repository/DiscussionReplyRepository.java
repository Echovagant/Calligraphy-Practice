package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.DiscussionReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 讨论回复的仓储。
 *
 * <p>只保留两个方法：按讨论分页取回复、取单个未删除的回复。
 * 「按用户查」「按父回复查」「热门回复」等没有对应契约端点的方法已删。
 */
@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {

    /** 某个讨论下的回复（排除已删除），按创建时间升序。 */
    Page<DiscussionReply> findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(
            Long discussionId,
            DiscussionReply.ReplyStatus status,
            Pageable pageable);

    /** 取单个未删除的回复；不存在（或已删除）返回空。 */
    Optional<DiscussionReply> findByIdAndStatusNot(Long id, DiscussionReply.ReplyStatus status);
}
