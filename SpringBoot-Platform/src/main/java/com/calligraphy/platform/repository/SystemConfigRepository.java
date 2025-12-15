package com.calligraphy.platform.repository;

import com.calligraphy.platform.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    boolean existsByConfigKey(String configKey);
    
    List<SystemConfig> findByConfigTypeOrderByConfigKeyAsc(String configType);
    
    List<SystemConfig> findByIsEditableTrueOrderByConfigKeyAsc();
    
    List<SystemConfig> findAllByOrderByConfigKeyAsc();
    
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configGroup = ?1 ORDER BY sc.configKey")
    List<SystemConfig> findByConfigGroupOrderByConfigKey(String configGroup);
}


