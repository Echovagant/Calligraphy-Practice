package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.DiscussionReplyDto;
import com.calligraphy.platform.model.DiscussionReply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 讨论回复实体 → DTO 的映射。
 *
 * <p>主键 {@code id}（实体是 Long，契约是字符串）、{@code discussionId} 与
 * {@code parentId} 同样由 Long 转成字符串。
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface DiscussionReplyMapper {

    @Mapping(target = "id", source = "id")
    DiscussionReplyDto toDto(DiscussionReply reply);

    List<DiscussionReplyDto> toDtoList(List<DiscussionReply> replies);
}
