package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Copybook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopybookRepository extends JpaRepository<Copybook, String>, JpaSpecificationExecutor<Copybook> {

    /**
     * 启用中的字帖，按使用次数倒序，**取前 N 条**。
     *
     * <p>必须带 {@link Pageable}：首页概览只要前 5 个热门字帖，
     * 以前是把全部启用字帖查出来在内存里 {@code limit(5)}。
     * 带上分页参数后 LIMIT 下推到 SQL，返回行数与字帖总数无关。
     *
     * <p>自建字帖（{@code is_custom = true}）不算精选字帖，但这条查询是给首页概览用的，
     * 精选字帖按使用次数排序在前，自建字帖使用次数从 0 起，排不到前面去。
     */
    List<Copybook> findByIsActiveTrueOrderByUsageCountDesc(Pageable pageable);

    /**
     * 某个用户的自建字帖（分页），按创建时间倒序。
     *
     * <p>「我的字帖」端点用。{@code created_at} 在 MySQL 里是秒级精度，
     * 同一秒建的多份字帖时间戳相同 —— 所以再补一个 {@code id} 倒序作次级键，
     * 否则翻页时同一页里的顺序会飘。
     */
    @Query("SELECT c FROM Copybook c WHERE c.userId = :userId ORDER BY c.createdAt DESC, c.id DESC")
    Page<Copybook> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

    List<Copybook> findByCategory(String category);
}
