package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

       Optional<User> findByUsername(String username);

       Optional<User> findByEmail(String email);

       Boolean existsByUsername(String username);

       Boolean existsByEmail(String email);

       @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
       Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

       @Query("SELECT u FROM User u WHERE u.role = :role")
       List<User> findByRole(@Param("role") User.Role role);

       @Query("SELECT u FROM User u WHERE u.status = :status")
       List<User> findByStatus(@Param("status") User.UserStatus status);

       @Query("SELECT u FROM User u WHERE u.accountLocked = true")
       List<User> findLockedUsers();

       @Query("SELECT u FROM User u WHERE u.emailVerified = false")
       List<User> findUnverifiedUsers();

       @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = true")
       List<User> findUsersWithTwoFactorEnabled();

       @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date")
       List<User> findInactiveUsers(@Param("date") LocalDateTime date);

       @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
       List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       @Query("SELECT u FROM User u WHERE u.passwordChangedAt < :date")
       List<User> findUsersWithExpiredPasswords(@Param("date") LocalDateTime date);

       @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :attempts")
       List<User> findUsersWithFailedAttempts(@Param("attempts") int attempts);

       // 缁熻鐩稿叧鏌ヨ
       @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
       long countByRole(@Param("role") User.Role role);

       @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
       long countByStatus(@Param("status") User.UserStatus status);

       @Query("SELECT COUNT(u) FROM User u WHERE u.accountLocked = true")
       long countLockedUsers();

       @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = false")
       long countUnverifiedUsers();

       @Query("SELECT COUNT(u) FROM User u WHERE u.twoFactorEnabled = true")
       long countUsersWithTwoFactorEnabled();

       @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
       long countNewUsersSince(@Param("date") LocalDateTime date);

       // 鎼滅储鐩稿叧鏌ヨ
       @Query("SELECT u FROM User u WHERE " +
                     "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       List<User> searchUsers(@Param("keyword") String keyword);

       // 瀹夊叏鐩稿叧鏌ヨ
       @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
       Optional<User> findByEmailVerificationToken(@Param("token") String token);

       @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token")
       Optional<User> findByPasswordResetToken(@Param("token") String token);

       @Query("SELECT u FROM User u WHERE u.twoFactorSecret = :secret")
       Optional<User> findByTwoFactorSecret(@Param("secret") String secret);

       // 批量操作
       @Query("UPDATE User u SET u.accountLocked = false, u.failedLoginAttempts = 0 " +
                     "WHERE u.accountLocked = true AND u.accountLockedUntil < :now")
       void unlockExpiredAccounts(@Param("now") LocalDateTime now);

       @Query("UPDATE User u SET u.status = 'INACTIVE' WHERE u.lastLoginAt < :date")
       void deactivateInactiveUsers(@Param("date") LocalDateTime date);
}


