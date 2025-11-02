package com.mpowerplus.shipmenttrackersystem.notificationservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.Notification;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationStatus;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationRepositoryAdapter.
 * Tests the adapter logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class NotificationRepositoryAdapterTest {

    @Mock
    private NotificationJpaRepository jpaRepository;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private NotificationRepositoryAdapter repositoryAdapter;

    private Notification sampleNotification;
    private NotificationEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.create(
                "TRK123456",
                NotificationType.EMAIL,
                "user@example.com",
                "Shipment Status Update",
                "Your shipment is in transit"
        );

        sampleEntity = NotificationEntity.builder()
                .id(1L)
                .trackingId("TRK123456")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.PENDING)
                .recipient("user@example.com")
                .subject("Shipment Status Update")
                .message("Your shipment is in transit")
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }

    @Test
    void save_ValidNotification_ReturnsSavedNotification() {
        // Arrange
        when(mapper.toEntity(sampleNotification)).thenReturn(sampleEntity);
        when(jpaRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(mapper.toDomain(sampleEntity)).thenReturn(sampleNotification);

        // Act
        Notification result = repositoryAdapter.save(sampleNotification);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTrackingId()).isEqualTo("TRK123456");
        assertThat(result.getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(result.getRecipient()).isEqualTo("user@example.com");
        verify(mapper).toEntity(sampleNotification);
        verify(jpaRepository).save(sampleEntity);
        verify(mapper).toDomain(sampleEntity);
    }

    @Test
    void findById_ExistingNotification_ReturnsNotification() {
        // Arrange
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(mapper.toDomain(sampleEntity)).thenReturn(sampleNotification);

        // Act
        Optional<Notification> result = repositoryAdapter.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTrackingId()).isEqualTo("TRK123456");
        assertThat(result.get().getType()).isEqualTo(NotificationType.EMAIL);
        verify(jpaRepository).findById(1L);
        verify(mapper).toDomain(sampleEntity);
    }

    @Test
    void findById_NonExistingNotification_ReturnsEmpty() {
        // Arrange
        when(jpaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Notification> result = repositoryAdapter.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(999L);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findByTrackingId_ExistingTrackingId_ReturnsNotifications() {
        // Arrange
        NotificationEntity entity1 = NotificationEntity.builder()
                .id(1L)
                .trackingId("TRK123456")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.SENT)
                .recipient("user1@example.com")
                .build();

        NotificationEntity entity2 = NotificationEntity.builder()
                .id(2L)
                .trackingId("TRK123456")
                .type(NotificationType.SMS)
                .status(NotificationStatus.PENDING)
                .recipient("+34666777888")
                .build();

        Notification notification1 = Notification.create(
                "TRK123456",
                NotificationType.EMAIL,
                "user1@example.com",
                "Subject 1",
                "Message 1"
        );

        Notification notification2 = Notification.create(
                "TRK123456",
                NotificationType.SMS,
                "+34666777888",
                "Subject 2",
                "Message 2"
        );

        when(jpaRepository.findByTrackingId("TRK123456")).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(notification1);
        when(mapper.toDomain(entity2)).thenReturn(notification2);

        // Act
        List<Notification> result = repositoryAdapter.findByTrackingId("TRK123456");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(result.get(1).getType()).isEqualTo(NotificationType.SMS);
        verify(jpaRepository).findByTrackingId("TRK123456");
        verify(mapper, times(2)).toDomain(any(NotificationEntity.class));
    }

    @Test
    void findByTrackingId_NonExistingTrackingId_ReturnsEmptyList() {
        // Arrange
        when(jpaRepository.findByTrackingId("NONEXISTENT")).thenReturn(List.of());

        // Act
        List<Notification> result = repositoryAdapter.findByTrackingId("NONEXISTENT");

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findByTrackingId("NONEXISTENT");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findByStatus_ExistingStatus_ReturnsNotifications() {
        // Arrange
        NotificationEntity entity1 = NotificationEntity.builder()
                .id(1L)
                .trackingId("TRK111")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.FAILED)
                .build();

        NotificationEntity entity2 = NotificationEntity.builder()
                .id(2L)
                .trackingId("TRK222")
                .type(NotificationType.PUSH)
                .status(NotificationStatus.FAILED)
                .build();

        Notification notification1 = Notification.create(
                "TRK111",
                NotificationType.EMAIL,
                "user1@example.com",
                "Subject 1",
                "Message 1"
        );

        Notification notification2 = Notification.create(
                "TRK222",
                NotificationType.PUSH,
                "device-token",
                "Subject 2",
                "Message 2"
        );

        when(jpaRepository.findByStatus(NotificationStatus.FAILED)).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(notification1);
        when(mapper.toDomain(entity2)).thenReturn(notification2);

        // Act
        List<Notification> result = repositoryAdapter.findByStatus(NotificationStatus.FAILED);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTrackingId()).isEqualTo("TRK111");
        assertThat(result.get(1).getTrackingId()).isEqualTo("TRK222");
        verify(jpaRepository).findByStatus(NotificationStatus.FAILED);
        verify(mapper, times(2)).toDomain(any(NotificationEntity.class));
    }

    @Test
    void findByStatus_NoMatchingStatus_ReturnsEmptyList() {
        // Arrange
        when(jpaRepository.findByStatus(NotificationStatus.SENT)).thenReturn(List.of());

        // Act
        List<Notification> result = repositoryAdapter.findByStatus(NotificationStatus.SENT);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findByStatus(NotificationStatus.SENT);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void findAll_MultipleNotifications_ReturnsAllNotifications() {
        // Arrange
        NotificationEntity entity1 = NotificationEntity.builder()
                .id(1L)
                .trackingId("TRK111")
                .type(NotificationType.EMAIL)
                .status(NotificationStatus.SENT)
                .build();

        NotificationEntity entity2 = NotificationEntity.builder()
                .id(2L)
                .trackingId("TRK222")
                .type(NotificationType.SMS)
                .status(NotificationStatus.PENDING)
                .build();

        NotificationEntity entity3 = NotificationEntity.builder()
                .id(3L)
                .trackingId("TRK333")
                .type(NotificationType.IN_APP)
                .status(NotificationStatus.FAILED)
                .build();

        Notification notification1 = Notification.create(
                "TRK111",
                NotificationType.EMAIL,
                "user1@example.com",
                "Subject 1",
                "Message 1"
        );

        Notification notification2 = Notification.create(
                "TRK222",
                NotificationType.SMS,
                "+34666777888",
                "Subject 2",
                "Message 2"
        );

        Notification notification3 = Notification.create(
                "TRK333",
                NotificationType.IN_APP,
                "user-123",
                "Subject 3",
                "Message 3"
        );

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2, entity3));
        when(mapper.toDomain(entity1)).thenReturn(notification1);
        when(mapper.toDomain(entity2)).thenReturn(notification2);
        when(mapper.toDomain(entity3)).thenReturn(notification3);

        // Act
        List<Notification> result = repositoryAdapter.findAll();

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTrackingId()).isEqualTo("TRK111");
        assertThat(result.get(1).getTrackingId()).isEqualTo("TRK222");
        assertThat(result.get(2).getTrackingId()).isEqualTo("TRK333");
        verify(jpaRepository).findAll();
        verify(mapper, times(3)).toDomain(any(NotificationEntity.class));
    }

    @Test
    void findAll_NoNotifications_ReturnsEmptyList() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(List.of());

        // Act
        List<Notification> result = repositoryAdapter.findAll();

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findAll();
        verify(mapper, never()).toDomain(any());
    }
}