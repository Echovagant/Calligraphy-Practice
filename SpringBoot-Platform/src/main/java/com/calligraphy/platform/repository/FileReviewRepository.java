package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.FileReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileReviewRepository extends JpaRepository<FileReview, String> {

    /**
     * 某个作品的**最新**一条审核记录。
     *
     * <p>一个作品可以有多条审核记录（每次提交/更新结论都留痕），
     * 「某个作品的审核记录」端点返回的是最新那条。
     */
    Optional<FileReview> findFirstByWorkIdOrderByCreatedAtDesc(Long workId);

    /** 某个作品的全部审核记录（统计字段用）。 */
    List<FileReview> findByWorkId(Long workId);

    /** 批量取多个作品的审核记录（分页列表的统计字段用，避免 N+1）。 */
    List<FileReview> findByWorkIdIn(Collection<Long> workIds);

    /** 某个审核员提交的审核记录（分页）。 */
    Page<FileReview> findByReviewerId(String reviewerId, Pageable pageable);
}
