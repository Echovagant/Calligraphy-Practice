package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.PracticeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    /**
     * 某用户的练习历史，按练习时间倒序，分页。
     *
     * <p>{@code user} 走 JOIN 一次取齐：映射 DTO 时会读 {@code record.getUser()}，
     * 不 JOIN 的话每条记录会再查一次 users（N+1）。
     */
    @EntityGraph(attributePaths = {"user"})
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

    /**
     * 某用户练习过的**不同日期**（去重后的日期，不含时间），连续天数的计算在服务层做。
     *
     * <p>投影查询：{@code SELECT DISTINCT DATE(practice_date)}。
     * 同一天练 10 次也只返回 1 行，返回行数与练习次数无关。
     *
     * <p>用原生 SQL 是因为 {@code DATE()} 是 MySQL 的函数；返回值声明成
     * {@link java.sql.Date}（JDBC 的 DATE 类型）就不用经过任何类型转换，
     * 服务层再转成 {@link LocalDate}。
     */
    @Query(value = "SELECT DISTINCT DATE(practice_date) FROM practice_records "
            + "WHERE user_id = :userId AND practice_date IS NOT NULL", nativeQuery = true)
    List<java.sql.Date> findDistinctPracticeDatesByUserId(@Param("userId") String userId);

    /**
     * 练习日期列表（倒序），连续天数的计算在服务层做。
     *
     * <p><b>不要在请求路径上用它</b>：它返回该用户的**全部**练习记录的时间戳，
     * 同一天练几次就返回几行。只算连续天数用
     * {@link #findDistinctPracticeDatesByUserId}（按天去重的投影）。
     */
    @Query("SELECT p.practiceDate FROM PracticeRecord p "
            + "WHERE p.userId = :userId AND p.practiceDate IS NOT NULL "
            + "ORDER BY p.practiceDate DESC")
    List<LocalDateTime> findPracticeDatesByUserId(@Param("userId") String userId);
}
