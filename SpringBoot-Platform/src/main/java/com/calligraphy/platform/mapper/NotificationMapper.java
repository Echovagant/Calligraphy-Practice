package com.calligraphy.platform.mapper;

import com.calligraphy.platform.dto.NotificationDto;
import com.calligraphy.platform.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "recipientId", expression = "java(notification.getRecipientId() != null ? Long.parseLong(notification.getRecipientId()) : null)")
    NotificationDto toDto(Notification notification);

    @Mapping(target = "recipientId", expression = "java(notificationDto.getRecipientId() != null ? String.valueOf(notificationDto.getRecipientId()) : null)")
    Notification toEntity(NotificationDto notificationDto);
}


