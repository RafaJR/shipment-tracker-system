package com.mpowerplus.shipmenttrackersystem.notificationservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.Notification;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationStatus;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NotificationMapper.
 * Tests bidirectional mapping between domain and persistence entities.
 */
class NotificationMapperTest {

    private NotificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
    }

    @Test
    void toEntity_ValidNotification_ReturnsValidEntity() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Notification notification = Notification.builder()
                .id(1L)
                .trackingId("TRK12345")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.SENT)
                .recipient("user@example.com")
                .subject("Shipment Status Update")
                .message("Your shipment is in transit")
                .createdAt(now.minusHours(2))
                .sentAt(now)
                .retryCount(0)
                .errorMessage(null)
                .build();

        // Act
        NotificationEntity entity = mapper.toEntity(notification);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getTrackingId()).isEqualTo("TRK12345");
        assertThat(entity.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(entity.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(entity.getRecipient()).isEqualTo("user@example.com");
        assertThat(entity.getSubject()).isEqualTo("Shipment Status Update");
        assertThat(entity.getMessage()).isEqualTo("Your shipment is in transit");
        assertThat(entity.getCreatedAt()).isEqualTo(now.minusHours(2));
        assertThat(entity.getSentAt()).isEqualTo(now);
        assertThat(entity.getRetryCount()).isEqualTo(0);
        assertThat(entity.getErrorMessage()).isNull();
    }

    @Test
    void toEntity_NullNotification_ReturnsNull() {
        // Act
        NotificationEntity entity = mapper.toEntity(null);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    void toEntity_NotificationWithNullFields_MapsCorrectly() {
        // Arrange
        Notification notification = Notification.builder()
                .id(null)
                .trackingId("TRK99999")
                .type(NotificationType.SMS)
                .status(NotificationStatus.PENDING)
                .recipient("+34666777888")
                .subject(null)
                .message("SMS notification")
                .createdAt(LocalDateTime.now())
                .sentAt(null)
                .retryCount(0)
                .errorMessage(null)
                .build();

        // Act
        NotificationEntity entity = mapper.toEntity(notification);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getTrackingId()).isEqualTo("TRK99999");
        assertThat(entity.getType()).isEqualTo(NotificationType.SMS);
        assertThat(entity.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(entity.getRecipient()).isEqualTo("+34666777888");
        assertThat(entity.getSubject()).isNull();
        assertThat(entity.getMessage()).isEqualTo("SMS notification");
        assertThat(entity.getSentAt()).isNull();
        assertThat(entity.getRetryCount()).isEqualTo(0);
        assertThat(entity.getErrorMessage()).isNull();
    }

    @Test
    void toEntity_FailedNotificationWithError_MapsCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Notification notification = Notification.builder()
                .id(2L)
                .trackingId("TRK55555")
                .type(NotificationType.PUSH)
                .status(NotificationStatus.FAILED)
                .recipient("device-token-123")
                .subject("Delivery Alert")
                .message("Package delivered")
                .createdAt(now.minusHours(1))
                .sentAt(null)
                .retryCount(3)
                .errorMessage("Connection timeout")
                .build();

        // Act
        NotificationEntity entity = mapper.toEntity(notification);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(entity.getRetryCount()).isEqualTo(3);
        assertThat(entity.getErrorMessage()).isEqualTo("Connection timeout");
        assertThat(entity.getSentAt()).isNull();
    }

    @Test
    void toDomain_ValidEntity_ReturnsValidNotification() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        NotificationEntity entity = NotificationEntity.builder()
                .id(3L)
                .trackingId("TRK67890")
                .type(NotificationType.IN_APP)
                .status(NotificationStatus.SENT)
                .recipient("user-123")
                .subject("Shipment Update")
                .message("Your package is out for delivery")
                .createdAt(now.minusHours(3))
                .sentAt(now.minusMinutes(30))
                .retryCount(1)
                .errorMessage(null)
                .build();

        // Act
        Notification notification = mapper.toDomain(entity);

        // Assert
        assertThat(notification).isNotNull();
        assertThat(notification.getId()).isEqualTo(3L);
        assertThat(notification.getTrackingId()).isEqualTo("TRK67890");
        assertThat(notification.getType()).isEqualTo(NotificationType.IN_APP);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getRecipient()).isEqualTo("user-123");
        assertThat(notification.getSubject()).isEqualTo("Shipment Update");
        assertThat(notification.getMessage()).isEqualTo("Your package is out for delivery");
        assertThat(notification.getCreatedAt()).isEqualTo(now.minusHours(3));
        assertThat(notification.getSentAt()).isEqualTo(now.minusMinutes(30));
        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getErrorMessage()).isNull();
    }

    @Test
    void toDomain_NullEntity_ReturnsNull() {
        // Act
        Notification notification = mapper.toDomain(null);

        // Assert
        assertThat(notification).isNull();
    }

    @Test
    void toDomain_EntityWithNullFields_MapsCorrectly() {
        // Arrange
        NotificationEntity entity = NotificationEntity.builder()
                .id(4L)
                .trackingId("TRK11111")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.RETRYING)
                .recipient("test@test.com")
                .subject(null)
                .message(null)
                .createdAt(LocalDateTime.now())
                .sentAt(null)
                .retryCount(2)
                .errorMessage("Previous failure")
                .build();

        // Act
        Notification notification = mapper.toDomain(entity);

        // Assert
        assertThat(notification).isNotNull();
        assertThat(notification.getId()).isEqualTo(4L);
        assertThat(notification.getTrackingId()).isEqualTo("TRK11111");
        assertThat(notification.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.RETRYING);
        assertThat(notification.getSubject()).isNull();
        assertThat(notification.getMessage()).isNull();
        assertThat(notification.getSentAt()).isNull();
        assertThat(notification.getRetryCount()).isEqualTo(2);
        assertThat(notification.getErrorMessage()).isEqualTo("Previous failure");
    }

    @Test
    void bidirectionalMapping_NotificationToEntityToDomain_PreservesData() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Notification original = Notification.builder()
                .id(5L)
                .trackingId("TRK555")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.SENT)
                .recipient("customer@example.com")
                .subject("Your Package Update")
                .message("Package arrived at distribution center")
                .createdAt(now.minusDays(1))
                .sentAt(now.minusHours(2))
                .retryCount(0)
                .errorMessage(null)
                .build();

        // Act
        NotificationEntity entity = mapper.toEntity(original);
        Notification result = mapper.toDomain(entity);

        // Assert
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getTrackingId()).isEqualTo(original.getTrackingId());
        assertThat(result.getType()).isEqualTo(original.getType());
        assertThat(result.getStatus()).isEqualTo(original.getStatus());
        assertThat(result.getRecipient()).isEqualTo(original.getRecipient());
        assertThat(result.getSubject()).isEqualTo(original.getSubject());
        assertThat(result.getMessage()).isEqualTo(original.getMessage());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(result.getSentAt()).isEqualTo(original.getSentAt());
        assertThat(result.getRetryCount()).isEqualTo(original.getRetryCount());
        assertThat(result.getErrorMessage()).isEqualTo(original.getErrorMessage());
    }

    @Test
    void bidirectionalMapping_EntityToDomainToEntity_PreservesData() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        NotificationEntity original = NotificationEntity.builder()
                .id(6L)
                .trackingId("TRK777")
                .type(NotificationType.SMS)
                .status(NotificationStatus.FAILED)
                .recipient("+34611222333")
                .subject("SMS Alert")
                .message("Delivery attempted")
                .createdAt(now.minusHours(5))
                .sentAt(null)
                .retryCount(3)
                .errorMessage("SMS service unavailable")
                .build();

        // Act
        Notification domain = mapper.toDomain(original);
        NotificationEntity result = mapper.toEntity(domain);

        // Assert
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getTrackingId()).isEqualTo(original.getTrackingId());
        assertThat(result.getType()).isEqualTo(original.getType());
        assertThat(result.getStatus()).isEqualTo(original.getStatus());
        assertThat(result.getRecipient()).isEqualTo(original.getRecipient());
        assertThat(result.getSubject()).isEqualTo(original.getSubject());
        assertThat(result.getMessage()).isEqualTo(original.getMessage());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(result.getSentAt()).isEqualTo(original.getSentAt());
        assertThat(result.getRetryCount()).isEqualTo(original.getRetryCount());
        assertThat(result.getErrorMessage()).isEqualTo(original.getErrorMessage());
    }
}
