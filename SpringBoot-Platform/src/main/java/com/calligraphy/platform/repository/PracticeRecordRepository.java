package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.PracticeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习记录仓储。
 *
 * <p>只保留契约端点真正用到的查询。其余历史查询（按字体风格、按完成状态、
 * 按满意度分数等）随接口裁剪一起删掉了 —— 契约里没有对应端点，
 * 留着就是「看起来做了」的假象。
 */
@Repository
public interface PracticeRecordRepository extends JpaRepository<PracticeRecord, Long> {

    /** 某用户的全部练习记录（用户资料页的计数用）。 */
    List<PracticeRecord> findByUserId(String userId);

    /** 某用户的练习历史，按练习时间倒序，分页。 */
    Page<PracticeRecord> findByUserIdOrderByPracticeDateDesc(String userId, Pageable pageable);

    long countByUserId(String userId);

    @Query("SELECT COALESCE(SUM(p.duration), 0) FROM PracticeRecord p WHERE p.userId = :userId")
    Long sumDurationByUserId(@Param("userId") String userId);

    @Query("SELECT COALESCE(AVG(p.duration), 0) FROM PracticeRecord p WHERE p.userId = :userId")
    Double avgDurationByUserId(@Param("userId") String userId);

    @Query("SELECT COALESCE(SUM(p.characterCount), 0) FROM PracticeRecord p WHERE p.userId = :userId")
    Long sumCharacterCountByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(DISTINCT p.copybookId) FROM PracticeRecord p "
            + "WHERE p.userId = :userId AND p.copybookId IS NOT NULL")
    long countDistinctCopybookByUserId(@Param("userId") String userId);

    /** 练习日期列表（倒序），连续天数的计算在服务层做。 */
    @Query("SELECT p.practiceDate FROM PracticeRecord p "
            + "WHERE p.userId = :userId AND p.practiceDate IS NOT NULL "
            + "ORDER BY p.practiceDate DESC")
    List<LocalDateTime> findPracticeDatesByUserId(@Param("userId") String userId);
}
