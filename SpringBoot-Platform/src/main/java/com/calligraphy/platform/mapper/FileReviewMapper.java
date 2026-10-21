package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.FileReviewDto;
import com.calligraphy.platform.dto.FileReviewStatisticsDto;
import com.calligraphy.platform.model.BackendUser;
import com.calligraphy.platform.model.FileReview;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * FileReview → FileReviewDto 的映射。
 *
 * <p>统计字段（totalReviews / approvedCount / …）不在实体上，它们来自
 * 「这条记录所属作品」的审核汇总，由调用方通过 {@link Context} 传进来。
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface FileReviewMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", expression = "java(fileReview.getStatus() == null ? null : fileReview.getStatus().name())")
    @Mapping(target = "fileId", expression = "java(workIdOf(fileReview))")
    @Mapping(target = "fileUrl", source = "filePath")
    @Mapping(target = "userId", expression = "java(uploaderIdOf(fileReview))")
    @Mapping(target = "username", expression = "java(fileReview.getUploadedBy() == null ? null : fileReview.getUploadedBy().getUsername())")
    @Mapping(target = "userNickname", expression = "java(uploaderNickname(fileReview))")
    @Mapping(target = "reviewerId", expression = "java(reviewerIdOf(fileReview))")
    @Mapping(target = "reviewerUsername", expression = "java(fileReview.getReviewer() == null ? null : fileReview.getReviewer().getUsername())")
    @Mapping(target = "reviewerNickname", expression = "java(reviewerNickname(fileReview))")
    @Mapping(target = "reviewComment", source = "reviewNotes")
    @Mapping(target = "feedback", source = "reviewNotes")
    @Mapping(target = "relatedWorkId", expression = "java(workIdOf(fileReview))")
    @Mapping(target = "relatedWorkTitle", expression = "java(fileReview.getWork() == null ? null : fileReview.getWork().getTitle())")
    @Mapping(target = "submittedAt", source = "uploadTime")
    @Mapping(target = "reviewedAt", source = "reviewTime")
    @Mapping(target = "totalReviews", expression = "java(statistics.getTotalReviews())")
    @Mapping(target = "approvedCount", expression = "java(statistics.getApprovedCount())")
    @Mapping(target = "rejectedCount", expression = "java(statistics.getRejectedCount())")
    @Mapping(target = "revisionRequestedCount", expression = "java(statistics.getRevisionRequestedCount())")
    @Mapping(target = "approvalRate", expression = "java(statistics.getApprovalRate())")
    @Mapping(target = "averageReviewTime", expression = "java(statistics.getAverageReviewTime())")
    FileReviewDto toDto(FileReview fileReview, @Context FileReviewStatisticsDto statistics);

    /**
     * 被审作品 ID。
     *
     * <p>实体上的 workId 字段是只读映射（insertable=false, updatable=false），
     * 新保存的记录上它还是 null，得从关联的 work 上取。
     */
    default String workIdOf(FileReview fileReview) {
        Work work = fileReview.getWork();
        if (work != null && work.getId() != null) {
            return String.valueOf(work.getId());
        }
        return fileReview.getWorkId() == null ? null : String.valueOf(fileReview.getWorkId());
    }

    /**
     * 上传者 ID。
     *
     * <p>实体上的 uploadedById 字段是只读映射（insertable=false, updatable=false），
     * 新保存的记录上它还是 null，得从关联的 uploadedBy 上取。
     */
    default String uploaderIdOf(FileReview fileReview) {
        User uploader = fileReview.getUploadedBy();
        if (uploader != null && uploader.getId() != null) {
            return uploader.getId();
        }
        return fileReview.getUploadedById();
    }

    /** 审核员 ID：同 uploaderIdOf，新保存的记录上 reviewerId 字段还没回填。 */
    default String reviewerIdOf(FileReview fileReview) {
        BackendUser reviewer = fileReview.getReviewer();
        if (reviewer != null && reviewer.getId() != null) {
            return reviewer.getId();
        }
        return fileReview.getReviewerId();
    }

    /** 上传者昵称：优先昵称，其次实名，最后用户名。 */
    default String uploaderNickname(FileReview fileReview) {
        User uploader = fileReview.getUploadedBy();
        if (uploader == null) {
            return null;
        }
        return uploader.getDisplayName();
    }

    /** 审核员昵称：backend_users 里没有 nickname，用 fullName，退回用户名。 */
    default String reviewerNickname(FileReview fileReview) {
        BackendUser reviewer = fileReview.getReviewer();
        if (reviewer == null) {
            return null;
        }
        String fullName = reviewer.getFullName();
        return (fullName != null && !fullName.isBlank()) ? fullName : reviewer.getUsername();
    }
}
