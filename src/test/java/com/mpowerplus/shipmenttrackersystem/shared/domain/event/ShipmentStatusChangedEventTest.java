package com.mpowerplus.shipmenttrackersystem.shared.domain.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentStatusChangedEventTest {

    /**
     * Test class to validate the behavior of the `of` method in `ShipmentStatusChangedEvent`.
     * The `of` method creates a new `ShipmentStatusChangedEvent` instance with a subset of provided attributes
     * and sets default values for some fields.
     */

    @Test
    void testOf_ShouldCreateEventWithCorrectValues() {
        // Arrange
        String trackingId = "TRK123";
        String oldStatus = "In Transit";
        String newStatus = "Delivered";
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.of(trackingId, oldStatus, newStatus, timestamp);

        // Assert
        assertNotNull(event);
        assertEquals(trackingId, event.getTrackingId());
        assertEquals(oldStatus, event.getOldStatus());
        assertEquals(newStatus, event.getNewStatus());
        assertEquals(timestamp, event.getTimestamp());
        assertEquals("tracking-service", event.getSource());
        assertEquals("1.0", event.getEventVersion());
        assertNull(event.getLocation());
        assertNull(event.getCarrier());
        assertNull(event.getEstimatedDelivery());
        assertNull(event.getChangeReason());
    }

    @Test
    void testOf_ShouldHandleNullValuesProperly() {
        // Arrange
        String trackingId = null;
        String oldStatus = null;
        String newStatus = null;
        LocalDateTime timestamp = null;

        // Act
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.of(trackingId, oldStatus, newStatus, timestamp);

        // Assert
        assertNotNull(event);
        assertNull(event.getTrackingId());
        assertNull(event.getOldStatus());
        assertNull(event.getNewStatus());
        assertNull(event.getTimestamp());
        assertEquals("tracking-service", event.getSource());
        assertEquals("1.0", event.getEventVersion());
        assertNull(event.getLocation());
        assertNull(event.getCarrier());
        assertNull(event.getEstimatedDelivery());
        assertNull(event.getChangeReason());
    }

    @Test
    void testOf_ShouldSetDefaultSourceAndEventVersion() {
        // Arrange
        String trackingId = "TRK456";
        String oldStatus = "Picked Up";
        String newStatus = "In Transit";
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.of(trackingId, oldStatus, newStatus, timestamp);

        // Assert
        assertEquals("tracking-service", event.getSource());
        assertEquals("1.0", event.getEventVersion());
    }
}