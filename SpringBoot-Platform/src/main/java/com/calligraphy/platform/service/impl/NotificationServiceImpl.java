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
        log.info("鍒涘缓閫氱煡: 鎺ユ敹鑰?{}, 绫诲瀷={}", createRequest.getRecipientId(), createRequest.getType());

        Notification notification = Notification.builder()
                .title(createRequest.getTitle() != null ? createRequest.getTitle() : "閫氱煡")
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
        log.info("閫氱煡鍒涘缓鎴愬姛: ID={}", savedNotification.getId());

        return notificationMapper.toDto(savedNotification);
    }
    @Override
    @Transactional
    public NotificationDto updateNotification(Long notificationId, NotificationUpdateRequestDto updateRequest) {
        log.info("鏇存柊閫氱煡: ID={}", notificationId);

        Notification notification = notificationRepository.findById(( Long) notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("閫氱煡涓嶅瓨鍦? " + notificationId));

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
        log.info("閫氱煡鏇存柊鎴愬姛: ID={}", updatedNotification.getId());

        return notificationMapper.toDto(updatedNotification);
    }
    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("鍒犻櫎閫氱煡: ID={}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("閫氱煡涓嶅瓨鍦? " + notificationId));

        // 杞垹闄?        notification.setIsDeleted(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("閫氱煡鍒犻櫎鎴愬姛: ID={}", notificationId);
    }
    @Override
    public NotificationDto getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("閫氱煡涓嶅瓨鍦? " + notificationId));

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
        log.info("鏍囪閫氱煡涓哄凡璇? ID={}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("閫氱煡涓嶅瓨鍦? " + notificationId));

        notification.setIsRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.info("閫氱煡宸叉爣璁颁负宸茶: ID={}", notificationId);
    }
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("鏍囪鎵€鏈夐€氱煡涓哄凡璇? 鐢ㄦ埛ID={}", userId);

        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndIsReadFalseAndIsDeletedFalse(String.valueOf(userId));

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setUpdatedAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(( Iterable<Notification>) unreadNotifications);

        log.info("鎵€鏈夐€氱煡宸叉爣璁颁负宸茶: 鐢ㄦ埛ID={}, 鏁伴噺={}", userId, unreadNotifications.size());
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
        log.info("鍙戦€佺郴缁熼€氱煡: 鎺ユ敹鑰?{}, 鏍囬={}", recipientId, title);

        NotificationCreateRequestDto createRequest = new NotificationCreateRequestDto(String.valueOf(recipientId),
                content, "SYSTEM", title, null, null);

        createNotification(createRequest);
    }

    @Transactional
    public void sendCommentNotification(Long workId, Long commentId, Long authorId, Long commenterId) {
        log.info("鍙戦€佽瘎璁洪€氱煡: 浣滃搧ID={}, 璇勮ID={}, 浣滆€匢D={}, 璇勮鑰匢D={}", workId, commentId, authorId, commenterId);

        // 閬垮厤缁欒嚜宸卞彂閫侀€氱煡
        if (authorId.equals(commenterId)) {
            return;
        }

        NotificationCreateRequestDto createRequest = new NotificationCreateRequestDto(String.valueOf(authorId),
                "鏈変汉璇勮浜嗘偍鐨勪綔鍝?, "COMMENT", "鏂拌瘎璁?, String.valueOf(workId), "WORK");

        createNotification(createRequest);
    }

    @Transactional
    public void sendLikeNotification(Long workId, Long authorId, Long likerId) {
        log.info("鍙戦€佺偣璧為€氱煡: 浣滃搧ID={}, 浣滆€匢D={}, 鐐硅禐鑰匢D={}", workId, authorId, likerId);

        // 閬垮厤缁欒嚜宸卞彂閫侀€氱煡
        if (authorId.equals(likerId)) {
            return;
        }

        NotificationCreateRequestDto createRequest = new NotificationCreateRequestDto(String.valueOf(authorId),
                "鏈変汉鐐硅禐浜嗘偍鐨勪綔鍝?, "LIKE", "鏂扮偣璧?, String.valueOf(workId), "WORK");

        createNotification(createRequest);
    }

    @Transactional
    public void sendFollowNotification(Long followerId, Long followingId) {
        log.info("鍙戦€佸叧娉ㄩ€氱煡: 鍏虫敞鑰匢D={}, 琚叧娉ㄨ€匢D={}", followerId, followingId);

        // 閬垮厤缁欒嚜宸卞彂閫侀€氱煡
        if (followerId.equals(followingId)) {
            return;
        }

        NotificationCreateRequestDto createRequest = new NotificationCreateRequestDto(String.valueOf(followingId),
                "鏈変汉鍏虫敞浜嗘偍", "FOLLOW", "鏂板叧娉?, String.valueOf(followerId), "USER");

        createNotification(createRequest);
    }

    @Transactional
    public void cleanupExpiredNotifications() {
        log.info("娓呯悊杩囨湡閫氱煡");

        LocalDateTime now = LocalDateTime.now();
        List<Notification> expiredNotifications = java.util.Collections.emptyList();

        for (Notification notification : expiredNotifications) {
            notification.setIsDeleted(true);
            notification.setUpdatedAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(( Iterable<Notification>) expiredNotifications);

        log.info("杩囨湡閫氱煡娓呯悊瀹屾垚: 鏁伴噺={}", expiredNotifications.size());
    }
}


