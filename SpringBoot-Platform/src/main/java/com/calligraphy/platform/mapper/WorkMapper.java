package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.WorkDto;
import com.calligraphy.platform.model.Work;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface WorkMapper {
    
    @Mapping(target = "fileUrls", expression = "java(work.getImageUrl() != null ? java.util.Collections.singletonList(work.getImageUrl()) : null)")
    @Mapping(target = "workId", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "username", expression = "java(work.getUser() != null ? work.getUser().getUsername() : null)")
    @Mapping(target = "userNickname", expression = "java(work.getUser() != null ? work.getUser().getFullName() : null)")
    @Mapping(target = "userAvatar", expression = "java(work.getUser() != null ? work.getUser().getAvatarUrl() : null)")
    @Mapping(target = "category", constant = "default")
    @Mapping(target = "isLikedByCurrentUser", constant = "false")
    @Mapping(target = "popularityScore", expression = "java(calculatePopularityScore(work))")
    WorkDto toDto(Work work);
    
    @Named("getFirstFileUrl")
    default String getFirstFileUrl(List<String> fileUrls) {
        return fileUrls != null && !fileUrls.isEmpty() ? fileUrls.get(0) : null;
    }
    
    default double calculatePopularityScore(Work work) {
        // 简单的热度计算公式：浏览量 + 点赞数 * 2 + 评论数 * 3
        Long viewCount = work.getViewCount() != null ? work.getViewCount() : 0L;
        Long likeCount = work.getLikeCount() != null ? work.getLikeCount() : 0L;
        Long commentCount = work.getCommentCount() != null ? work.getCommentCount() : 0L;
        return viewCount + likeCount * 2.0 + commentCount * 3.0;
    }
}


