package com.mpowerplus.shipmenttrackersystem.notificationservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.Notification;
import org.springframework.stereotype.Component;

/**
 * Mapper between Notification domain and NotificationEntity persistence models.
 */
@Component
public class NotificationMapper {

    public NotificationEntity toEntity(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationEntity.builder()
                .id(notification.getId())
                .trackingId(notification.getTrackingId())
                .type(notification.getType())
                .status(notification.getStatus())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .sentAt(notification.getSentAt())
                .retryCount(notification.getRetryCount())
                .errorMessage(notification.getErrorMessage())
                .build();
    }

    public Notification toDomain(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }

        return Notification.builder()
                .id(entity.getId())
                .trackingId(entity.getTrackingId())
                .type(entity.getType())
                .status(entity.getStatus())
                .recipient(entity.getRecipient())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .createdAt(entity.getCreatedAt())
                .sentAt(entity.getSentAt())
                .retryCount(entity.getRetryCount())
                .errorMessage(entity.getErrorMessage())
                .build();
    }
}
