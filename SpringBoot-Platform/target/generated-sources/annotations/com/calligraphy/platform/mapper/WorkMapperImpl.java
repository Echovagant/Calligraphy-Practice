package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.WorkDto;
import com.calligraphy.platform.model.Work;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T11:14:24+0800",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class WorkMapperImpl implements WorkMapper {

    @Override
    public WorkDto toDto(Work work) {
        if ( work == null ) {
            return null;
        }

        WorkDto.WorkDtoBuilder workDto = WorkDto.builder();

        if ( work.getId() != null ) {
            workDto.workId( String.valueOf( work.getId() ) );
        }
        workDto.userId( work.getUserId() );
        if ( work.getCommentCount() != null ) {
            workDto.commentCount( work.getCommentCount() );
        }
        workDto.content( work.getContent() );
        workDto.createdAt( work.getCreatedAt() );
        workDto.description( work.getDescription() );
        if ( work.getIsFeatured() != null ) {
            workDto.isFeatured( work.getIsFeatured() );
        }
        if ( work.getLikeCount() != null ) {
            workDto.likeCount( work.getLikeCount() );
        }
        workDto.reviewerId( work.getReviewerId() );
        workDto.title( work.getTitle() );
        workDto.updatedAt( work.getUpdatedAt() );
        if ( work.getViewCount() != null ) {
            workDto.viewCount( work.getViewCount() );
        }

        workDto.fileUrls( work.getImageUrl() != null ? java.util.Collections.singletonList(work.getImageUrl()) : null );
        workDto.username( work.getUser() != null ? work.getUser().getUsername() : null );
        workDto.userNickname( work.getUser() != null ? work.getUser().getFullName() : null );
        workDto.userAvatar( work.getUser() != null ? work.getUser().getAvatarUrl() : null );
        workDto.category( "default" );
        workDto.isLikedByCurrentUser( false );
        workDto.popularityScore( calculatePopularityScore(work) );

        return workDto.build();
    }
}
