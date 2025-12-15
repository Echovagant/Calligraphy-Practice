package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.DiscussionReplyDto;
import com.calligraphy.platform.dto.DiscussionReplyCreateDto;
import com.calligraphy.platform.model.DiscussionReply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface DiscussionReplyMapper {

    @Mapping(target = "replyId", source = "id")
    DiscussionReplyDto toDto(DiscussionReply reply);

    List<DiscussionReplyDto> toDtoList(List<DiscussionReply> replies);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "discussion", ignore = true)
    @Mapping(target = "discussionId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DiscussionReply toEntity(DiscussionReplyCreateDto dto);
}


