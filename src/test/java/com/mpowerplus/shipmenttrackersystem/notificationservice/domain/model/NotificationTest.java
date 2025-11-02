package com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Notification aggregate root.
 */
class NotificationTest {

    @Test
    void create_ValidData_CreatesNotificationWithPendingStatus() {
        // Act
        Notification notification = Notification.create(
                "TRACK123",
                NotificationType.EMAIL,
                "user@example.com",
                "Shipment Update",
                "Your shipment has been updated"
        );

        // Assert
        assertThat(notification.getTrackingId()).isEqualTo("TRACK123");
        assertThat(notification.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRecipient()).isEqualTo("user@example.com");
        assertThat(notification.getSubject()).isEqualTo("Shipment Update");
        assertThat(notification.getMessage()).isEqualTo("Your shipment has been updated");
        assertThat(notification.getRetryCount()).isEqualTo(0);
        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.getSentAt()).isNull();
    }

    @Test
    void markAsSent_UpdatesStatusAndSetsSentAt() {
        // Arrange
        Notification notification = Notification.create(
                "TRACK123",
                NotificationType.EMAIL,
                "user@example.com",
                "Subject",
                "Message"
        );

        // Act
        notification.markAsSent();

        // Assert
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isNotNull();
    }

    @Test
    void markAsFailed_UpdatesStatusAndSetsErrorMessage() {
        // Arrange
        Notification notification = Notification.create(
                "TRACK123",
                NotificationType.EMAIL,
                "user@example.com",
                "Subject",
                "Message"
        );

        // Act
        notification.markAsFailed("SMTP server connection timeout");

        // Assert
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(notification.getErrorMessage()).isEqualTo("SMTP server connection timeout");
    }

    @Test
    void retry_IncrementsRetryCountAndUpdatesStatus() {
        // Arrange
        Notification notification = Notification.create(
                "TRACK123",
                NotificationType.EMAIL,
                "user@example.com",
                "Subject",
                "Message"
        );
        notification.markAsFailed("First attempt failed");

        // Act
        notification.retry();

        // Assert
        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.RETRYING);
    }

    @Test
    void canRetry_WithLessThan3Retries_ReturnsTrue() {
        // Arrange
        Notification notification = Notification.create(
                "TRACK123",
                NotificationType.EMAIL,
                "user@example.com",
                "Subject",
                "Message"
        );
        notification.markAsFailed("Failed");

        // Act & Assert
        assertThat(notification.canRetry()).isTrue();
    }

    @Test
    void canRetry_With3Retries_ReturnsFalse() {
        // Arrange
        Notification notification = Notification.builder()
                .trackingId("TRACK123")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.FAILED)
                .recipient("user@example.com")
                .subject("Subject")
                .message("Message")
                .retryCount(3)
                .build();

        // Act & Assert
        assertThat(notification.canRetry()).isFalse();
    }

    @Test
    void canRetry_WithSentStatus_ReturnsFalse() {
        // Arrange
        Notification notification = Notification.create(
                "TRACK123",
                NotificationType.EMAIL,
                "user@example.com",
                "Subject",
                "Message"
        );
        notification.markAsSent();

        // Act & Assert
        assertThat(notification.canRetry()).isFalse();
    }

    @Test
    void create_DifferentTypes_CreatesNotificationCorrectly() {
        // Test EMAIL
        Notification emailNotification = Notification.create(
                "TRACK123", NotificationType.EMAIL, "user@example.com", "Subject", "Message"
        );
        assertThat(emailNotification.getType()).isEqualTo(NotificationType.EMAIL);

        // Test SMS
        Notification smsNotification = Notification.create(
                "TRACK123", NotificationType.SMS, "+1234567890", "Subject", "Message"
        );
        assertThat(smsNotification.getType()).isEqualTo(NotificationType.SMS);

        // Test PUSH
        Notification pushNotification = Notification.create(
                "TRACK123", NotificationType.PUSH, "device-token-123", "Subject", "Message"
        );
        assertThat(pushNotification.getType()).isEqualTo(NotificationType.PUSH);
    }
}
