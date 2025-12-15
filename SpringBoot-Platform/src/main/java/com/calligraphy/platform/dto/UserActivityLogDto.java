package com.calligraphy.platform.dto;

import com.calligraphy.platform.model.UserActivityLog;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLogDto {
    
    private String logId;
    private String userId;
    private String username;
    private String userNickname;
    private String userAvatar;
    private String activityType;
    private String description;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private String location;
    private String relatedEntityId;
    private String relatedEntityType;
    private String details;
    private LocalDateTime activityTime;
    private LocalDateTime createdAt;
    
    // 缁熻淇℃伅
    private long totalActivities;
    private long activitiesToday;
    private long activitiesThisWeek;
    private long activitiesThisMonth;
}


