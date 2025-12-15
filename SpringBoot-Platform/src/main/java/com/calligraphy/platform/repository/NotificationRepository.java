package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdAndIsDeletedFalseOrderByCreatedAtDesc(String recipientId);

    Page<Notification> findByRecipientIdAndIsDeletedFalseOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(String recipientId);

    Page<Notification> findByRecipientIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndTypeAndIsDeletedFalseOrderByCreatedAtDesc(String recipientId, String type);

    List<Notification> findByRelatedTypeAndRelatedIdAndIsDeletedFalseOrderByCreatedAtDesc(String relatedType, String relatedId);

    List<Notification> findByRecipientIdAndIsReadFalseAndIsDeletedFalse(String recipientId);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.isDeleted = false AND (n.expiresAt IS NULL OR n.expiresAt > :currentTime)")
    List<Notification> findActiveNotifications(@Param("recipientId") String recipientId, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.isRead = false AND n.isDeleted = false AND (n.expiresAt IS NULL OR n.expiresAt > :currentTime)")
    long countUnreadNotifications(@Param("recipientId") String recipientId, @Param("currentTime") LocalDateTime currentTime);

    Optional<Notification> findByIdAndRecipientIdAndIsDeletedFalse(Long id, String recipientId);
}


