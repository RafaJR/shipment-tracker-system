package com.mpowerplus.shipmenttrackersystem.notificationservice.application.service;

import com.mpowerplus.shipmenttrackersystem.notificationservice.application.port.output.NotificationSendException;
import com.mpowerplus.shipmenttrackersystem.notificationservice.application.port.output.NotificationSender;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.Notification;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationType;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.repository.NotificationRepository;
import com.mpowerplus.shipmenttrackersystem.shared.domain.event.ShipmentStatusChangedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for handling notifications.
 * Orchestrates notification creation and sending.
 * Records metrics for monitoring notification operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    // Metrics
    private final Counter notificationsSentCounter;
    private final Counter notificationsFailedCounter;
    private final Counter notificationsByTypeCounter;
    private final Counter notificationsRetriesCounter;
    private final Timer notificationProcessingDurationTimer;

    /**
     * Processes a shipment status change event and sends notifications.
     */
    @Transactional
    public void processStatusChangeEvent(ShipmentStatusChangedEvent event) {
        log.info("Processing status change event for tracking ID: {}", event.getTrackingId());

        notificationProcessingDurationTimer.record(() -> {
            String recipient = determineRecipient(event.getTrackingId());
            String subject = buildSubject(event);
            String message = buildMessage(event);

            // Create notification for EMAIL
            Notification notification = Notification.create(
                    event.getTrackingId(),
                    NotificationType.EMAIL,
                    recipient,
                    subject,
                    message
            );

            // Save notification
            notification = notificationRepository.save(notification);

            // Send notification
            try {
                notificationSender.send(
                        notification.getType(),
                        notification.getRecipient(),
                        notification.getSubject(),
                        notification.getMessage()
                );

                notification.markAsSent();
                notificationsSentCounter.increment();
                notificationsByTypeCounter.increment();
                log.info("Notification sent successfully for tracking ID: {}", event.getTrackingId());

            } catch (NotificationSendException e) {
                log.error("Failed to send notification for tracking ID: {}", event.getTrackingId(), e);
                notification.markAsFailed(e.getMessage());
                notificationsFailedCounter.increment();

                // Retry if allowed
                if (notification.canRetry()) {
                    notification.retry();
                    notificationsRetriesCounter.increment();
                    log.info("Notification marked for retry. Retry count: {}", notification.getRetryCount());
                }
            }

            // Update notification status
            notificationRepository.save(notification);
        });
    }

    /**
     * Determines the recipient email based on tracking ID.
     * In a real system, this would look up the customer email from a database.
     */
    private String determineRecipient(String trackingId) {
        // Simplified: use a default email
        // In production, query customer database by trackingId
        return "customer@example.com";
    }

    /**
     * Builds the notification subject based on the event.
     */
    private String buildSubject(ShipmentStatusChangedEvent event) {
        return String.format("Shipment Update: %s - %s",
                event.getTrackingId(),
                event.getNewStatus());
    }

    /**
     * Builds the notification message based on the event.
     */
    private String buildMessage(ShipmentStatusChangedEvent event) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Your shipment %s has been updated.\n\n", event.getTrackingId()));
        message.append(String.format("Status: %s → %s\n", event.getOldStatus(), event.getNewStatus()));

        if (event.getLocation() != null) {
            message.append(String.format("Location: %s\n", event.getLocation()));
        }

        if (event.getCarrier() != null) {
            message.append(String.format("Carrier: %s\n", event.getCarrier()));
        }

        if (event.getEstimatedDelivery() != null) {
            message.append(String.format("Estimated Delivery: %s\n", event.getEstimatedDelivery()));
        }

        message.append("\nThank you for tracking with us!");

        return message.toString();
    }
}
