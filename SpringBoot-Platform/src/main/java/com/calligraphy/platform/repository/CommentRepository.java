package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论仓储。
 *
 * <p>契约的评论域只有四个端点（发表 / 按作品查 / 按用户查 / 删除），本仓储只保留
 * 这四个端点用到的查询方法；原先那批「搜索 / 热门 / 批量改状态 / 按审核员查」的方法
 * 没有对应端点，已随接口一起裁掉
 * （见 .superpowers/sdd/2026-10-21-p5-community/CONVENTIONS.md §5）。
 *
 * <p>{@code findByUserId} 必须保留：{@code UserServiceImpl.getUserCounts} 用它统计
 * 用户的评论数。
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 某个用户的全部评论（含回复），UserServiceImpl 统计评论数用。 */
    List<Comment> findByUserId(String userId);

    /**
     * 某个作品下的根评论（不含回复），按状态过滤，按创建时间倒序。
     *
     * <p>公开的作品评论列表用 {@code status = APPROVED}；
     * 用户自己的评论历史用 {@code status <> DELETED}。
     */
    Page<Comment> findByWorkIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(
            Long workId, Comment.CommentStatus status, Pageable pageable);

    /** 某个用户下的根评论（不含回复），排除已删除的，按创建时间倒序。 */
    Page<Comment> findByUserIdAndParentIdIsNullAndStatusNotOrderByCreatedAtDesc(
            String userId, Comment.CommentStatus status, Pageable pageable);

    /** 一批根评论的子评论，按创建时间正序。 */
    List<Comment> findByParentIdInAndStatusOrderByCreatedAtAsc(
            List<Long> parentIds, Comment.CommentStatus status);

    /** 一批根评论的子评论，排除已删除的，按创建时间正序。 */
    List<Comment> findByParentIdInAndStatusNotOrderByCreatedAtAsc(
            List<Long> parentIds, Comment.CommentStatus status);
}
