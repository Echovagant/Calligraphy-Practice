package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.CommentCreateRequestDto;
import com.calligraphy.platform.dto.CommentDto;
import com.calligraphy.platform.dto.CommentUpdateRequestDto;
import com.calligraphy.platform.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(source = "id", target = "commentId")
    @Mapping(source = "userId", target = "userId")
    @Mapping(target = "userNickname", expression = "java(comment.getUser() != null ? comment.getUser().getUsername() : null)")
    @Mapping(target = "username", expression = "java(comment.getUser() != null ? comment.getUser().getUsername() : null)")
    @Mapping(source = "workId", target = "workId")
    @Mapping(target = "workTitle", expression = "java(comment.getWork() != null ? comment.getWork().getTitle() : null)")
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtoList(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "reviewerId", ignore = true)
    @Mapping(target = "reviewNotes", ignore = true)
    @Mapping(target = "reviewTime", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "work", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "workId", ignore = true)
    Comment toEntity(CommentCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "reviewerId", ignore = true)
    @Mapping(target = "reviewNotes", ignore = true)
    @Mapping(target = "reviewTime", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "work", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "workId", ignore = true)
    void updateEntityFromDto(CommentUpdateRequestDto dto, @MappingTarget Comment comment);
}


