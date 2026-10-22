package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
 * <p>用户评论数走 {@link #countCommentsByUserId}（聚合），不要用
 * {@link #findByUserId} 把全部评论拉进内存再数。
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 某个用户的评论数 —— **聚合查询**，不加载评论实体。
     *
     * <p>用户资料页只需要这个数字。以前是把该用户的全部评论（含 TEXT content 列）
     * 查出来再 {@code .size()}。
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.userId = :userId")
    long countCommentsByUserId(@Param("userId") String userId);

    /**
     * 某个用户的全部评论（含回复）。
     *
     * <p><b>不要在请求路径上用它</b>：它会把该用户所有评论的 content 整列拉进内存。
     * 只要计数就用 {@link #countCommentsByUserId}，要列表就用下面那两条分页查询。
     */
    List<Comment> findByUserId(String userId);

    /**
     * 某个作品下的根评论（不含回复），按状态过滤，按创建时间倒序。
     *
     * <p>公开的作品评论列表用 {@code status = APPROVED}；
     * 用户自己的评论历史用 {@code status <> DELETED}。
     *
     * <p>{@code user / work / reviewer} 走 JOIN 一次取齐：{@code CommentMapper.toDto}
     * 会读这三个关联，不 JOIN 的话每条评论会再发 3 次查询（N+1）。
     */
    @EntityGraph(attributePaths = {"user", "work", "reviewer"})
    Page<Comment> findByWorkIdAndParentIdIsNullAndStatusOrderByCreatedAtDesc(
            Long workId, Comment.CommentStatus status, Pageable pageable);

    /** 某个用户下的根评论（不含回复），排除已删除的，按创建时间倒序。 */
    @EntityGraph(attributePaths = {"user", "work", "reviewer"})
    Page<Comment> findByUserIdAndParentIdIsNullAndStatusNotOrderByCreatedAtDesc(
            String userId, Comment.CommentStatus status, Pageable pageable);

    /** 一批根评论的子评论，按创建时间正序。 */
    @EntityGraph(attributePaths = {"user", "work", "reviewer"})
    List<Comment> findByParentIdInAndStatusOrderByCreatedAtAsc(
            List<Long> parentIds, Comment.CommentStatus status);

    /** 一批根评论的子评论，排除已删除的，按创建时间正序。 */
    @EntityGraph(attributePaths = {"user", "work", "reviewer"})
    List<Comment> findByParentIdInAndStatusNotOrderByCreatedAtAsc(
            List<Long> parentIds, Comment.CommentStatus status);
}
