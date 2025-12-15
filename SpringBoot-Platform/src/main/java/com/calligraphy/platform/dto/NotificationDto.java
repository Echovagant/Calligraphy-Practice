package com.calligraphy.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Long recipientId;
    private String recipientType;
    private String title;
    private String content;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String senderName;
    private String relatedId;
    private String relatedType;
}


