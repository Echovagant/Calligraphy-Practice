package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.CommentDto;
import com.calligraphy.platform.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 评论实体 → DTO 的映射。
 *
 * <p>只保留 {@code toDto}：契约里评论域只有「发表 / 按作品查 / 按用户查 / 删除」
 * 四个端点，没有更新端点，所以 {@code toEntity} / {@code updateEntityFromDto}
 * 属于没有调用方的死代码（见 .superpowers/sdd/2026-10-21-p5-community/CONVENTIONS.md §5）。
 *
 * <p>注意：{@code comment.getUser()} 与 {@code comment.getWork()} 是 LAZY 关联，
 * 映射必须在事务内进行（CommentServiceImpl 类级有 {@code @Transactional(readOnly = true)}）。
 *
 * <p>实体的 {@code userId} / {@code workId} 是关联列的只读镜像（insertable = false），
 * 插入后未必立即回填，所以 id 一律从关联对象取。
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface CommentMapper {

    @Mapping(target = "id", expression = "java(comment.getId() == null ? null : String.valueOf(comment.getId()))")
    @Mapping(target = "workId", expression = "java(comment.getWork() != null ? String.valueOf(comment.getWork().getId()) : (comment.getWorkId() == null ? null : String.valueOf(comment.getWorkId())))")
    @Mapping(target = "workTitle", expression = "java(comment.getWork() != null ? comment.getWork().getTitle() : null)")
    @Mapping(target = "userId", expression = "java(comment.getUser() != null ? comment.getUser().getId() : comment.getUserId())")
    @Mapping(target = "username", expression = "java(comment.getUser() != null ? comment.getUser().getUsername() : null)")
    @Mapping(target = "userNickname", expression = "java(comment.getUser() != null ? comment.getUser().getNickname() : null)")
    @Mapping(target = "userAvatar", expression = "java(comment.getUser() != null ? comment.getUser().getAvatarUrl() : null)")
    @Mapping(target = "parentCommentId", expression = "java(comment.getParentId() == null ? null : String.valueOf(comment.getParentId()))")
    @Mapping(target = "reviewerId", expression = "java(comment.getReviewer() != null ? comment.getReviewer().getId() : comment.getReviewerId())")
    @Mapping(target = "reviewerComment", source = "reviewNotes")
    @Mapping(target = "reviewedAt", source = "reviewTime")
    @Mapping(target = "reviewerUsername", expression = "java(comment.getReviewer() != null ? comment.getReviewer().getUsername() : null)")
    @Mapping(target = "reviewerNickname", expression = "java(comment.getReviewer() != null ? comment.getReviewer().getFullName() : null)")
    CommentDto toDto(Comment comment);
}
