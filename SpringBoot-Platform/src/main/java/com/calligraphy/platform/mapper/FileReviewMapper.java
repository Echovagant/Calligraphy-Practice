package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.FileReviewResponseDto;
import com.calligraphy.platform.model.FileReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface FileReviewMapper {

    @Mapping(target = "reviewerId", expression = "java(fileReview.getReviewer() != null ? Long.parseLong(fileReview.getReviewer().getId()) : null)")
    @Mapping(target = "reviewerName", expression = "java(fileReview.getReviewer() != null ? fileReview.getReviewer().getUsername() : null)")
    FileReviewResponseDto toDto(FileReview fileReview);
}


