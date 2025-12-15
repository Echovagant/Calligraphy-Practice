package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.FileReviewResponseDto;
import com.calligraphy.platform.model.FileReview;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T11:14:24+0800",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class FileReviewMapperImpl implements FileReviewMapper {

    @Override
    public FileReviewResponseDto toDto(FileReview fileReview) {
        if ( fileReview == null ) {
            return null;
        }

        FileReviewResponseDto fileReviewResponseDto = new FileReviewResponseDto();

        fileReviewResponseDto.setCreatedAt( fileReview.getCreatedAt() );
        if ( fileReview.getId() != null ) {
            fileReviewResponseDto.setId( Long.parseLong( fileReview.getId() ) );
        }
        fileReviewResponseDto.setUpdatedAt( fileReview.getUpdatedAt() );
        fileReviewResponseDto.setWorkId( fileReview.getWorkId() );

        fileReviewResponseDto.setReviewerId( fileReview.getReviewer() != null ? Long.parseLong(fileReview.getReviewer().getId()) : null );
        fileReviewResponseDto.setReviewerName( fileReview.getReviewer() != null ? fileReview.getReviewer().getUsername() : null );

        return fileReviewResponseDto;
    }
}
