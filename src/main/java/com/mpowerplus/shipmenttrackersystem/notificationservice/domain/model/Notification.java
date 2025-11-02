package com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Notification aggregate root.
 * Represents a notification to be sent to a user about shipment status changes.
 */
@Getter
@Builder
@AllArgsConstructor
public class Notification {

    private Long id;
    private String trackingId;
    private NotificationType type;
    private NotificationStatus status;
    private String recipient;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Integer retryCount;
    private String errorMessage;

    /**
     * Factory method to create a new notification.
     */
    public static Notification create(
            String trackingId,
            NotificationType type,
            String recipient,
            String subject,
            String message) {
        return Notification.builder()
                .trackingId(trackingId)
                .type(type)
                .status(NotificationStatus.PENDING)
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }

    /**
     * Marks the notification as successfully sent.
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Marks the notification as failed with an error message.
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    /**
     * Increments retry count and updates status.
     */
    public void retry() {
        this.retryCount++;
        this.status = NotificationStatus.RETRYING;
    }

    /**
     * Checks if notification can be retried (max 3 retries).
     */
    public boolean canRetry() {
        return this.retryCount < 3 && this.status == NotificationStatus.FAILED;
    }
}
