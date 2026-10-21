package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.NotificationCreateRequestDto;
import com.calligraphy.platform.dto.NotificationDto;
import com.calligraphy.platform.dto.NotificationUpdateRequestDto;
import com.calligraphy.platform.model.Notification;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.mapper.NotificationMapper;
import com.calligraphy.platform.repository.NotificationRepository;
import com.calligraphy.platform.service.NotificationService;
import com.calligraphy.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NotificationMapper notificationMapper;
    @Override
    @Transactional
    public NotificationDto createNotification(NotificationCreateRequestDto createRequest) {
        log.info("创建通知: 接收者={}, 类型={}", createRequest.getRecipientId(), createRequest.getType());

        Notification notification = Notification.builder()
                .title(createRequest.getTitle() != null ? createRequest.getTitle() : "通知")
                .content(createRequest.getContent())
                .type(createRequest.getType())
                .recipientId(createRequest.getRecipientId())
                .relatedId(createRequest.getRelatedId())
                .relatedType(createRequest.getRelatedType())
                .isRead(false)
                .isDeleted(false)
                .senderName("system")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(( Notification) notification);
        log.info("通知创建成功: ID={}", savedNotification.getId());

        return notificationMapper.toDto(savedNotification);
    }
    @Override
    @Transactional
    public NotificationDto updateNotification(Long notificationId, NotificationUpdateRequestDto updateRequest) {
        log.info("更新通知: ID={}", notificationId);

        Notification notification = notificationRepository.findById(( Long) notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知不存在: " + notificationId));

        if (updateRequest.getTitle() != null) {
            notification.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getContent() != null) {
            notification.setContent(updateRequest.getContent());
        }

        if (updateRequest.getIsRead() != null) {
            notification.setIsRead(updateRequest.getIsRead());
        }

        if (updateRequest.getRelatedId() != null) {
            notification.setRelatedId(updateRequest.getRelatedId());
        }
        if (updateRequest.getRelatedType() != null) {
            notification.setRelatedType(updateRequest.getRelatedType());
        }

        notification.setUpdatedAt(LocalDateTime.now());

        Notification updatedNotification = notificationRepository.save(notification);
        log.info("通知更新成功: ID={}", updatedNotification.getId());

        return notificationMapper.toDto(updatedNotification);
    }
    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("删除通知: ID={}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知不存在: " + notificationId));

        // 软删除
        notification.setIsDeleted(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("通知删除成功: ID={}", notificationId);
    }
    @Override
    public NotificationDto getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知不存在: " + notificationId));

        return notificationMapper.toDto(notification);
    }
    @Override
    public List<NotificationDto> getNotificationsByUser(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsDeletedFalseOrderByCreatedAtDesc(String.valueOf(userId));

        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<NotificationDto> getNotificationsByUser(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsDeletedFalseOrderByCreatedAtDesc(String.valueOf(userId), pageable);

        return notifications.map(notificationMapper::toDto);
    }
    @Override
    public List<NotificationDto> getUnreadNotificationsByUser(Long userId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(String.valueOf(userId));

        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public Page<NotificationDto> getUnreadNotificationsByUser(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtDesc(String.valueOf(userId), pageable);

        return notifications.map(notificationMapper::toDto);
    }
    @Override
    public List<NotificationDto> getNotificationsByType(Long userId, String type) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndTypeAndIsDeletedFalseOrderByCreatedAtDesc(String.valueOf(userId), type);

        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<NotificationDto> getNotificationsByRelatedId(String relatedType, Long relatedId) {
        List<Notification> notifications = notificationRepository
                .findByRelatedTypeAndRelatedIdAndIsDeletedFalseOrderByCreatedAtDesc(relatedType,
                        String.valueOf(relatedId));

        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        log.info("标记通知为已读: ID={}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知不存在: " + notificationId));

        notification.setIsRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("通知已标记为已读: ID={}", notificationId);
    }
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("标记所有通知为已读: 用户ID={}", userId);

        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndIsReadFalseAndIsDeletedFalse(String.valueOf(userId));

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setUpdatedAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(( Iterable<Notification>) unreadNotifications);

        log.info("所有通知已标记为已读: 用户ID={}, 数量={}", userId, unreadNotifications.size());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadNotifications(String.valueOf(userId), LocalDateTime.now());
    }

    public long getTotalCount(Long userId) {
        return notificationRepository.findByRecipientIdAndIsDeletedFalseOrderByCreatedAtDesc(String.valueOf(userId))
                .size();
    }

    @Transactional
    public void sendSystemNotification(String title, String content, Long recipientId) {
        log.info("发送系统通知: 接收者={}, 标题={}", recipientId, title);

        NotificationCreateRequestDto createRequest = new NotificationCreateRequestDto(String.valueOf(recipientId),
                content, "SYSTEM", title, null, null);

        createNotification(createRequest);
    }

    @Transactional
    public void sendCommentNotification(Long workId, Long commentId, Long authorId, Long commenterId) {
        log.info("发送评论通知: 作品ID={}, 评论ID={}, 作者ID={}, 评论者ID={}", workId, commentId, authorId, commenterId);

        // 避免给自己发送通知
        if (authorId.equals(commenterId)) {
            return;
        }

        NotificationCreateRequestDto createRequest = new NotificationCreateRequestDto(String.valueOf(authorId),
                "有人评论了您的作品", "COMMENT", "新评论", String.valueOf(workId), "WORK");

        createNotification(createRequest);
    }

    @Transactional
    public void sendLikeNotification(Long workId, Long authorId, Long likerId) {
        log.info("发送点赞通知: 作品ID={}, 作者ID={}, 点赞者ID={}", workId, authorId, likerId);

        // 避免给自己发送通知
        if (authorId.equals(likerId)) {
            return;
        }

        NotificationCreateRequestDto createRequest = new NotificationCreateRequestDto(String.valueOf(authorId),
                "有人点赞了您的作品", "LIKE", "新点赞", String.valueOf(workId), "WORK");

        createNotification(createRequest);
    }

    @Transactional
    public void sendFollowNotification(Long followerId, Long followingId) {
        log.info("发送关注通知: 关注者ID={}, 被关注者ID={}", followerId, followingId);

        // 避免给自己发送通知
        if (followerId.equals(followingId)) {
            return;
        }

        NotificationCreateRequestDto createRequest = new NotificationCreateRequestDto(String.valueOf(followingId),
                "有人关注了您", "FOLLOW", "新关注", String.valueOf(followerId), "USER");

        createNotification(createRequest);
    }

    @Transactional
    public void cleanupExpiredNotifications() {
        log.info("清理过期通知");

        LocalDateTime now = LocalDateTime.now();
        List<Notification> expiredNotifications = java.util.Collections.emptyList();

        for (Notification notification : expiredNotifications) {
            notification.setIsDeleted(true);
            notification.setUpdatedAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(( Iterable<Notification>) expiredNotifications);

        log.info("过期通知清理完成: 数量={}", expiredNotifications.size());
    }
}


