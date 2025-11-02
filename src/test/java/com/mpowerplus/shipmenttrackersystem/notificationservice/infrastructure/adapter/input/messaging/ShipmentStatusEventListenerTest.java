package com.mpowerplus.shipmenttrackersystem.notificationservice.infrastructure.adapter.input.messaging;

import com.mpowerplus.shipmenttrackersystem.notificationservice.application.service.NotificationService;
import com.mpowerplus.shipmenttrackersystem.shared.domain.event.ShipmentStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShipmentStatusEventListener.
 * Tests the Kafka consumer adapter with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class ShipmentStatusEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private ShipmentStatusEventListener eventListener;

    private ShipmentStatusChangedEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = ShipmentStatusChangedEvent.builder()
                .trackingId("TRK123456")
                .oldStatus("PENDING")
                .newStatus("IN_TRANSIT")
                .location("Madrid Distribution Center")
                .carrier("DHL Express")
                .estimatedDelivery(LocalDateTime.now().plusDays(2))
                .timestamp(LocalDateTime.now())
                .changeReason("Package picked up from origin")
                .source("tracking-service")
                .eventVersion("1.0")
                .build();
    }

    @Test
    void handleStatusChangeEvent_ValidEvent_ProcessesSuccessfully() {
        // Arrange
        doNothing().when(notificationService).processStatusChangeEvent(sampleEvent);
        doNothing().when(acknowledgment).acknowledge();

        // Act
        eventListener.handleStatusChangeEvent(sampleEvent, 0, 100L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(sampleEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_ServiceThrowsException_DoesNotAcknowledge() {
        // Arrange
        doThrow(new RuntimeException("Notification service error"))
                .when(notificationService).processStatusChangeEvent(sampleEvent);

        // Act
        eventListener.handleStatusChangeEvent(sampleEvent, 0, 100L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(sampleEvent);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_ServiceProcessesSuccessfully_AcknowledgesMessage() {
        // Arrange
        ShipmentStatusChangedEvent deliveredEvent = ShipmentStatusChangedEvent.builder()
                .trackingId("TRK789012")
                .oldStatus("OUT_FOR_DELIVERY")
                .newStatus("DELIVERED")
                .location("Customer Address")
                .carrier("FedEx")
                .timestamp(LocalDateTime.now())
                .source("tracking-service")
                .eventVersion("1.0")
                .build();

        doNothing().when(notificationService).processStatusChangeEvent(deliveredEvent);
        doNothing().when(acknowledgment).acknowledge();

        // Act
        eventListener.handleStatusChangeEvent(deliveredEvent, 2, 500L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(deliveredEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_WithAllFields_ProcessesCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ShipmentStatusChangedEvent fullEvent = ShipmentStatusChangedEvent.builder()
                .trackingId("TRK999888")
                .oldStatus("IN_TRANSIT")
                .newStatus("OUT_FOR_DELIVERY")
                .location("Barcelona Local Hub")
                .carrier("UPS")
                .estimatedDelivery(now.plusHours(4))
                .timestamp(now)
                .changeReason("Package arrived at local hub")
                .source("tracking-service")
                .eventVersion("1.0")
                .build();

        doNothing().when(notificationService).processStatusChangeEvent(fullEvent);
        doNothing().when(acknowledgment).acknowledge();

        // Act
        eventListener.handleStatusChangeEvent(fullEvent, 1, 250L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(fullEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_NullFieldsInEvent_StillProcesses() {
        // Arrange
        ShipmentStatusChangedEvent eventWithNulls = ShipmentStatusChangedEvent.builder()
                .trackingId("TRK555666")
                .oldStatus("PENDING")
                .newStatus("CANCELLED")
                .location(null)
                .carrier(null)
                .estimatedDelivery(null)
                .timestamp(LocalDateTime.now())
                .changeReason(null)
                .source("tracking-service")
                .eventVersion("1.0")
                .build();

        doNothing().when(notificationService).processStatusChangeEvent(eventWithNulls);
        doNothing().when(acknowledgment).acknowledge();

        // Act
        eventListener.handleStatusChangeEvent(eventWithNulls, 0, 75L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(eventWithNulls);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_MultiplePartitions_ProcessesCorrectly() {
        // Arrange
        ShipmentStatusChangedEvent event1 = ShipmentStatusChangedEvent.builder()
                .trackingId("TRK111")
                .oldStatus("PENDING")
                .newStatus("IN_TRANSIT")
                .timestamp(LocalDateTime.now())
                .build();

        ShipmentStatusChangedEvent event2 = ShipmentStatusChangedEvent.builder()
                .trackingId("TRK222")
                .oldStatus("IN_TRANSIT")
                .newStatus("DELIVERED")
                .timestamp(LocalDateTime.now())
                .build();

        doNothing().when(notificationService).processStatusChangeEvent(any());
        doNothing().when(acknowledgment).acknowledge();

        // Act - Simulate events from different partitions
        eventListener.handleStatusChangeEvent(event1, 0, 100L, acknowledgment);
        eventListener.handleStatusChangeEvent(event2, 1, 200L, acknowledgment);

        // Assert
        verify(notificationService, times(2)).processStatusChangeEvent(any(ShipmentStatusChangedEvent.class));
        verify(acknowledgment, times(2)).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_ServiceThrowsIllegalArgumentException_DoesNotAcknowledge() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid event data"))
                .when(notificationService).processStatusChangeEvent(sampleEvent);

        // Act
        eventListener.handleStatusChangeEvent(sampleEvent, 0, 300L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(sampleEvent);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_ServiceThrowsNullPointerException_DoesNotAcknowledge() {
        // Arrange
        doThrow(new NullPointerException("Null value encountered"))
                .when(notificationService).processStatusChangeEvent(sampleEvent);

        // Act
        eventListener.handleStatusChangeEvent(sampleEvent, 2, 450L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(sampleEvent);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_HighOffsetValue_ProcessesCorrectly() {
        // Arrange
        doNothing().when(notificationService).processStatusChangeEvent(sampleEvent);
        doNothing().when(acknowledgment).acknowledge();

        // Act - Test with high offset value
        eventListener.handleStatusChangeEvent(sampleEvent, 5, 999999L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(sampleEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void handleStatusChangeEvent_EventWithMinimalData_ProcessesSuccessfully() {
        // Arrange
        ShipmentStatusChangedEvent minimalEvent = ShipmentStatusChangedEvent.builder()
                .trackingId("TRK000")
                .oldStatus("UNKNOWN")
                .newStatus("PENDING")
                .timestamp(LocalDateTime.now())
                .build();

        doNothing().when(notificationService).processStatusChangeEvent(minimalEvent);
        doNothing().when(acknowledgment).acknowledge();

        // Act
        eventListener.handleStatusChangeEvent(minimalEvent, 0, 1L, acknowledgment);

        // Assert
        verify(notificationService).processStatusChangeEvent(minimalEvent);
        verify(acknowledgment).acknowledge();
    }
}
