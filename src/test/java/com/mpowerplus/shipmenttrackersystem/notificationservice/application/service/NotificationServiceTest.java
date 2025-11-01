package com.mpowerplus.shipmenttrackersystem.notificationservice.application.service;

import com.mpowerplus.shipmenttrackersystem.notificationservice.application.port.output.NotificationSendException;
import com.mpowerplus.shipmenttrackersystem.notificationservice.application.port.output.NotificationSender;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.Notification;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationStatus;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationType;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.repository.NotificationRepository;
import com.mpowerplus.shipmenttrackersystem.shared.domain.event.ShipmentStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private NotificationService notificationService;

    private ShipmentStatusChangedEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = ShipmentStatusChangedEvent.builder()
                .trackingId("TRACK123")
                .oldStatus("PENDING")
                .newStatus("IN_TRANSIT")
                .location("Madrid")
                .carrier("DHL")
                .estimatedDelivery(LocalDateTime.now().plusDays(2))
                .timestamp(LocalDateTime.now())
                .source("tracking-service")
                .eventVersion("1.0")
                .build();
    }

    @Test
    void processStatusChangeEvent_SuccessfulSend_SavesNotificationAsSent() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(notificationSender).send(
                any(NotificationType.class),
                anyString(),
                anyString(),
                anyString()
        );

        // Act
        notificationService.processStatusChangeEvent(sampleEvent);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        Notification firstSave = notificationCaptor.getAllValues().get(0);
        assertThat(firstSave.getTrackingId()).isEqualTo("TRACK123");
        assertThat(firstSave.getStatus()).isEqualTo(NotificationStatus.SENT);

        Notification secondSave = notificationCaptor.getAllValues().get(1);
        assertThat(secondSave.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(secondSave.getSentAt()).isNotNull();

        verify(notificationSender, times(1)).send(
                eq(NotificationType.EMAIL),
                eq("customer@example.com"),
                contains("TRACK123"),
                contains("IN_TRANSIT")
        );
    }

    @Test
    void processStatusChangeEvent_SendFails_SavesNotificationAsFailed() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new NotificationSendException("SMTP connection failed"))
                .when(notificationSender).send(
                        any(NotificationType.class),
                        anyString(),
                        anyString(),
                        anyString()
                );

        // Act
        notificationService.processStatusChangeEvent(sampleEvent);

        // Assert
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        Notification secondSave = notificationCaptor.getAllValues().get(1);
        assertThat(secondSave.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(secondSave.getErrorMessage()).contains("SMTP connection failed");
    }

    @Test
    void processStatusChangeEvent_BuildsCorrectSubject() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        notificationService.processStatusChangeEvent(sampleEvent);

        // Assert
        verify(notificationSender).send(
                any(NotificationType.class),
                anyString(),
                eq("Shipment Update: TRACK123 - IN_TRANSIT"),
                anyString()
        );
    }

    @Test
    void processStatusChangeEvent_BuildsCorrectMessage() {
        // Arrange
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        notificationService.processStatusChangeEvent(sampleEvent);

        // Assert
        verify(notificationSender).send(
                any(NotificationType.class),
                anyString(),
                anyString(),
                messageCaptor.capture()
        );

        String message = messageCaptor.getValue();
        assertThat(message).contains("TRACK123");
        assertThat(message).contains("PENDING → IN_TRANSIT");
        assertThat(message).contains("Madrid");
        assertThat(message).contains("DHL");
    }

    @Test
    void processStatusChangeEvent_WithNullLocation_BuildsMessageWithoutLocation() {
        // Arrange
        ShipmentStatusChangedEvent eventWithoutLocation = ShipmentStatusChangedEvent.builder()
                .trackingId("TRACK456")
                .oldStatus("PENDING")
                .newStatus("CANCELLED")
                .carrier("FedEx")
                .timestamp(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        notificationService.processStatusChangeEvent(eventWithoutLocation);

        // Assert
        verify(notificationSender).send(
                any(NotificationType.class),
                anyString(),
                anyString(),
                messageCaptor.capture()
        );

        String message = messageCaptor.getValue();
        assertThat(message).contains("TRACK456");
        assertThat(message).contains("CANCELLED");
        assertThat(message).doesNotContain("Location:");
    }
}
