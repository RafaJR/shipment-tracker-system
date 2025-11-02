package com.mpowerplus.shipmenttrackersystem.notificationservice.infrastructure.adapter.output.notification;

import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for LoggingNotificationSender.
 * Tests the logging notification adapter implementation.
 */
@ExtendWith(MockitoExtension.class)
class LoggingNotificationSenderTest {

    private LoggingNotificationSender notificationSender;

    @BeforeEach
    void setUp() {
        notificationSender = new LoggingNotificationSender();
    }

    @Test
    void send_EmailNotification_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.EMAIL;
        String recipient = "customer@example.com";
        String subject = "Shipment Update: TRK123456";
        String message = "Your shipment is now in transit";

        // Act & Assert - Should not throw any exception
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_SmsNotification_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.SMS;
        String recipient = "+34666777888";
        String subject = "Shipment Alert";
        String message = "Package TRK789012 delivered";

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_PushNotification_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.PUSH;
        String recipient = "device-token-12345";
        String subject = "Delivery Alert";
        String message = "Your package will arrive today";

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_InAppNotification_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.IN_APP;
        String recipient = "user-123";
        String subject = "Package Update";
        String message = "Your package is out for delivery";

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_NotificationWithLongMessage_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.EMAIL;
        String recipient = "customer@example.com";
        String subject = "Detailed Shipment Update";
        String message = """
                Dear Customer,

                Your shipment with tracking ID TRK123456 has been updated.

                Status Change: PENDING → IN_TRANSIT
                Location: Madrid Distribution Center
                Carrier: DHL Express
                Estimated Delivery: 2025-11-05

                Package Details:
                - Weight: 2.5 kg
                - Dimensions: 30x20x15 cm
                - Contents: Electronics

                You can track your package at: https://tracking.example.com/TRK123456

                Best regards,
                Shipment Tracking System
                """;

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_NotificationWithSpecialCharacters_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.EMAIL;
        String recipient = "user+test@example.com";
        String subject = "Envío: TRK999 - Actualización";
        String message = "Su paquete está en tránsito. Location: Madrid, España. €50 valor.";

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_NotificationWithNullSubject_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.SMS;
        String recipient = "+34611222333";
        String subject = null;
        String message = "Package delivered";

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_NotificationWithEmptyMessage_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.IN_APP;
        String recipient = "user-456";
        String subject = "Notification";
        String message = "";

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_MultipleNotifications_LogsAllSuccessfully() {
        // Arrange & Act & Assert
        assertThatCode(() -> {
            notificationSender.send(NotificationType.EMAIL, "user1@example.com", "Subject 1", "Message 1");
            notificationSender.send(NotificationType.SMS, "+34666111222", "Subject 2", "Message 2");
            notificationSender.send(NotificationType.PUSH, "device-token-1", "Subject 3", "Message 3");
            notificationSender.send(NotificationType.IN_APP, "user-789", "Subject 4", "Message 4");
        }).doesNotThrowAnyException();
    }

    @Test
    void send_NotificationWithHtmlContent_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.EMAIL;
        String recipient = "customer@example.com";
        String subject = "HTML Notification";
        String message = """
                <html>
                <body>
                    <h1>Shipment Update</h1>
                    <p>Your package <strong>TRK123456</strong> is <em>in transit</em>.</p>
                    <ul>
                        <li>Status: IN_TRANSIT</li>
                        <li>Location: Madrid</li>
                        <li>Carrier: DHL</li>
                    </ul>
                </body>
                </html>
                """;

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_NotificationWithMinimalData_LogsSuccessfully() {
        // Arrange
        NotificationType type = NotificationType.EMAIL;
        String recipient = "test@test.com";
        String subject = "Test";
        String message = "Test message";

        // Act & Assert
        assertThatCode(() -> notificationSender.send(type, recipient, subject, message))
                .doesNotThrowAnyException();
    }

    @Test
    void send_AllNotificationTypes_LogsSuccessfully() {
        // Act & Assert - Test all notification types
        for (NotificationType type : NotificationType.values()) {
            assertThatCode(() -> notificationSender.send(
                    type,
                    "recipient@example.com",
                    "Test Subject for " + type,
                    "Test message for notification type: " + type
            )).doesNotThrowAnyException();
        }
    }
}
