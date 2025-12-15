package com.calligraphy.platform.repository;

import com.calligraphy.platform.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    
    Optional<AdminUser> findByUsername(String username);
    
    Optional<AdminUser> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<AdminUser> findByIsActive(Boolean isActive);
    
    @Query("SELECT a FROM AdminUser a WHERE a.username LIKE %:keyword% OR a.email LIKE %:keyword% OR a.nickname LIKE %:keyword%")
    List<AdminUser> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT a FROM AdminUser a WHERE a.roles LIKE %:role%")
    List<AdminUser> findByRoleContaining(@Param("role") String role);
}


