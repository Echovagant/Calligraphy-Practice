package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.DiscussionDto;
import com.calligraphy.platform.dto.DiscussionCreateDto;
import com.calligraphy.platform.dto.DiscussionUpdateDto;
import com.calligraphy.platform.model.Discussion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface DiscussionMapper {
    
    @Mapping(target = "discussionId", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "userAvatar", source = "userAvatar")
    @Mapping(target = "status", source = "status")
    DiscussionDto toDto(Discussion discussion);
    
    List<DiscussionDto> toDtoList(List<Discussion> discussions);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(target = "isPinned", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "replies", ignore = true)
    Discussion toEntity(DiscussionCreateDto dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "userAvatar", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "replyCount", ignore = true)
    @Mapping(target = "isPinned", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "replies", ignore = true)
    void updateEntityFromDto(DiscussionUpdateDto dto, @MappingTarget Discussion discussion);
}


