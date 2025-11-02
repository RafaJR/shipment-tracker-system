package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrackingTest {

    @Test
    void create_ShouldCreateTrackingWithInitialValues() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK123456");
        ShipmentStatus status = ShipmentStatus.IN_TRANSIT;
        String location = "Madrid Distribution Center";
        String carrier = "DHL";
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(3);

        // Act
        Tracking tracking = Tracking.create(trackingId, status, location, carrier, estimatedDelivery);

        // Assert
        assertNotNull(tracking);
        assertEquals(trackingId, tracking.getTrackingId());
        assertEquals(status, tracking.getCurrentStatus());
        assertNull(tracking.getPreviousStatus());
        assertEquals(location, tracking.getLastLocation());
        assertEquals(carrier, tracking.getCarrier());
        assertEquals(estimatedDelivery, tracking.getEstimatedDelivery());
        assertNotNull(tracking.getCreatedAt());
        assertNotNull(tracking.getUpdatedAt());
        assertNotNull(tracking.getLastCheckedAt());
    }

    @Test
    void updateStatus_ShouldUpdateStatusAndReturnStatusChange_WhenStatusIsValidAndDifferent() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK789012");
        ShipmentStatus initialStatus = ShipmentStatus.IN_TRANSIT;
        ShipmentStatus newStatus = ShipmentStatus.OUT_FOR_DELIVERY;
        String initialLocation = "Warehouse";
        String newLocation = "Delivery Truck";
        String carrier = "UPS";
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(2);

        Tracking tracking = Tracking.create(trackingId, initialStatus, initialLocation, carrier, estimatedDelivery);
        LocalDateTime beforeUpdate = LocalDateTime.now();

        // Act
        Optional<StatusChange> result = tracking.updateStatus(newStatus, newLocation);

        // Assert
        assertTrue(result.isPresent());
        StatusChange statusChange = result.get();
        assertEquals(trackingId, statusChange.getTrackingId());
        assertEquals(initialStatus, statusChange.getOldStatus());
        assertEquals(newStatus, statusChange.getNewStatus());
        assertEquals(newLocation, statusChange.getLocation());
        assertEquals(carrier, statusChange.getCarrier());
        assertEquals(estimatedDelivery, statusChange.getEstimatedDelivery());

        assertEquals(newStatus, tracking.getCurrentStatus());
        assertEquals(initialStatus, tracking.getPreviousStatus());
        assertEquals(newLocation, tracking.getLastLocation());
        assertTrue(tracking.getLastCheckedAt().isAfter(beforeUpdate) || tracking.getLastCheckedAt().isEqual(beforeUpdate));
        assertTrue(tracking.getUpdatedAt().isAfter(beforeUpdate) || tracking.getUpdatedAt().isEqual(beforeUpdate));
    }

    @Test
    void updateStatus_ShouldReturnEmpty_WhenStatusDoesNotChange() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK555666");
        ShipmentStatus status = ShipmentStatus.IN_TRANSIT;
        String location = "Barcelona Hub";
        String carrier = "FedEx";
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(1);

        Tracking tracking = Tracking.create(trackingId, status, location, carrier, estimatedDelivery);

        // Act
        Optional<StatusChange> result = tracking.updateStatus(status, location);

        // Assert
        assertFalse(result.isPresent());
        assertEquals(status, tracking.getCurrentStatus());
        assertNull(tracking.getPreviousStatus());
    }

    @Test
    void updateStatus_ShouldThrowException_WhenTransitionFromDeliveredIsAttempted() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK111222");
        ShipmentStatus currentStatus = ShipmentStatus.DELIVERED;
        ShipmentStatus newStatus = ShipmentStatus.IN_TRANSIT;
        String location = "Warehouse";
        String carrier = "DHL";
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(3);

        Tracking tracking = Tracking.create(trackingId, currentStatus, location, carrier, estimatedDelivery);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                tracking.updateStatus(newStatus, location)
        );
        assertTrue(exception.getMessage().contains("Invalid status transition"));
        assertEquals(currentStatus, tracking.getCurrentStatus());
        assertNull(tracking.getPreviousStatus());
    }

    @Test
    void updateStatus_ShouldThrowException_WhenTransitionFromCancelledIsAttempted() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK333444");
        ShipmentStatus currentStatus = ShipmentStatus.CANCELLED;
        ShipmentStatus newStatus = ShipmentStatus.IN_TRANSIT;
        String location = "Origin";
        String carrier = "SEUR";

        Tracking tracking = Tracking.create(trackingId, currentStatus, location, carrier, null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                tracking.updateStatus(newStatus, location)
        );
    }

    @Test
    void updateStatus_ShouldThrowException_WhenTransitionToPendingIsAttempted() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK777888");
        ShipmentStatus currentStatus = ShipmentStatus.IN_TRANSIT;
        ShipmentStatus newStatus = ShipmentStatus.PENDING;
        String location = "Hub";
        String carrier = "Correos";

        Tracking tracking = Tracking.create(trackingId, currentStatus, location, carrier, null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                tracking.updateStatus(newStatus, location)
        );
    }

    @Test
    void markAsChecked_ShouldUpdateLastCheckedAt() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK999000");
        Tracking tracking = Tracking.create(
                trackingId,
                ShipmentStatus.PENDING,
                "Warehouse",
                "MRW",
                LocalDateTime.now().plusDays(5)
        );
        LocalDateTime beforeMark = tracking.getLastCheckedAt();

        // Act
        tracking.markAsChecked();

        // Assert
        assertTrue(tracking.getLastCheckedAt().isAfter(beforeMark) || tracking.getLastCheckedAt().isEqual(beforeMark));
    }

    @Test
    void wasRecentlyChecked_ShouldReturnTrue_WhenCheckedWithinLastHour() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK111333");
        Tracking tracking = Tracking.create(
                trackingId,
                ShipmentStatus.IN_TRANSIT,
                "Location",
                "Carrier",
                LocalDateTime.now().plusDays(1)
        );

        // Act
        boolean result = tracking.wasRecentlyChecked();

        // Assert
        assertTrue(result);
    }

    @Test
    void isOverdue_ShouldReturnTrue_WhenEstimatedDeliveryPassedAndNotDelivered() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK444555");
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Tracking tracking = Tracking.create(
                trackingId,
                ShipmentStatus.IN_TRANSIT,
                "Somewhere",
                "Carrier",
                pastDate
        );

        // Act
        boolean result = tracking.isOverdue();

        // Assert
        assertTrue(result);
    }

    @Test
    void isOverdue_ShouldReturnFalse_WhenDelivered() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK666777");
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        Tracking tracking = Tracking.create(
                trackingId,
                ShipmentStatus.DELIVERED,
                "Customer Address",
                "Carrier",
                pastDate
        );

        // Act
        boolean result = tracking.isOverdue();

        // Assert
        assertFalse(result);
    }

    @Test
    void isOverdue_ShouldReturnFalse_WhenEstimatedDeliveryIsInFuture() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK888999");
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);
        Tracking tracking = Tracking.create(
                trackingId,
                ShipmentStatus.OUT_FOR_DELIVERY,
                "Local Hub",
                "Carrier",
                futureDate
        );

        // Act
        boolean result = tracking.isOverdue();

        // Assert
        assertFalse(result);
    }
}