package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Copybook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
     */
    List<Copybook> findByIsActiveTrueOrderByUsageCountDesc(Pageable pageable);

    List<Copybook> findByCategory(String category);
}
