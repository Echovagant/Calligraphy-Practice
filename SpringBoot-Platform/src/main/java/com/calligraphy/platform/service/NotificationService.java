package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.NotificationCreateRequestDto;
import com.calligraphy.platform.dto.NotificationDto;
import com.calligraphy.platform.dto.NotificationUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationDto createNotification(NotificationCreateRequestDto createRequest);
    NotificationDto updateNotification(Long notificationId, NotificationUpdateRequestDto updateRequest);
    void deleteNotification(Long notificationId);
    NotificationDto getNotificationById(Long notificationId);
    List<NotificationDto> getNotificationsByUser(Long userId);
    Page<NotificationDto> getNotificationsByUser(Long userId, Pageable pageable);
    List<NotificationDto> getUnreadNotificationsByUser(Long userId);
    Page<NotificationDto> getUnreadNotificationsByUser(Long userId, Pageable pageable);
    List<NotificationDto> getNotificationsByType(Long userId, String type);
    List<NotificationDto> getNotificationsByRelatedId(String relatedType, Long relatedId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}


