package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.FileReviewBatchDto;
import com.calligraphy.platform.dto.FileReviewBatchResultDto;
import com.calligraphy.platform.dto.FileReviewDto;
import com.calligraphy.platform.dto.FileReviewRequestDto;
import com.calligraphy.platform.dto.FileReviewUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 作品审核服务。
 *
 * <p>方法与契约（openapi/contract.yaml 的 file-reviews 域）一一对应，
 * 一共 7 个：fileReviewCreate / fileReviewBatch / fileReviewMyReviews /
 * fileReviewGetByWorkId / fileReviewGetById / fileReviewUpdate / fileReviewDelete。
 * 契约里没有对应端点的查询能力一律不保留（不留桩，也不留死代码）。
 */
public interface FileReviewService {

    /**
     * 提交作品审核（fileReviewCreate）。
     *
     * <p>审核结果会回写到 works.review_status。
     */
    FileReviewDto createReview(FileReviewRequestDto requestDto, String reviewerUsername);

    /**
     * 批量审核作品（fileReviewBatch）。
     *
     * <p>单个作品失败（例如作品不存在）只计入 failed，不影响其它作品。
     */
    FileReviewBatchResultDto batchReviewWorks(FileReviewBatchDto batchDto, String reviewerUsername);

    /**
     * 我提交的审核记录（fileReviewMyReviews），分页。
     */
    Page<FileReviewDto> getMyReviews(String reviewerUsername, Pageable pageable);

    /**
     * 某个作品的审核记录（fileReviewGetByWorkId）—— 最新一条。
     */
    FileReviewDto getReviewByWorkId(Long workId);

    /**
     * 审核记录详情（fileReviewGetById）。
     */
    FileReviewDto getReviewById(String reviewId);

    /**
     * 更新审核结论（fileReviewUpdate），并回写 works.review_status。
     */
    FileReviewDto updateReview(String reviewId, FileReviewUpdateDto updateDto, String reviewerUsername);

    /**
     * 删除审核记录（fileReviewDelete）。
     */
    void deleteReview(String reviewId, String reviewerUsername);
}
