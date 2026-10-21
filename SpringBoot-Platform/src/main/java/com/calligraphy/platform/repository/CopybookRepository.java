package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Copybook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopybookRepository extends JpaRepository<Copybook, String> {

    List<Copybook> findByIsActiveTrueOrderByUsageCountDesc();

    List<Copybook> findByCategory(String category);
}
