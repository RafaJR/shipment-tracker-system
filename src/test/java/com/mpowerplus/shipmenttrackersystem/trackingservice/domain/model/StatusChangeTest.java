package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StatusChangeTest {

    @Test
    void isSignificant_ShouldReturnTrue_WhenStatusChanges() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK123456");
        ShipmentStatus oldStatus = ShipmentStatus.IN_TRANSIT;
        ShipmentStatus newStatus = ShipmentStatus.DELIVERED;

        StatusChange statusChange = StatusChange.of(
                trackingId,
                oldStatus,
                newStatus,
                "New York Distribution Center",
                "UPS",
                LocalDateTime.now().plusDays(1)
        );

        // Act
        boolean result = statusChange.isSignificant();

        // Assert
        assertTrue(result, "Expected isSignificant to return true when statuses are different");
    }

    @Test
    void isSignificant_ShouldReturnFalse_WhenStatusDoesNotChange() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK789012");
        ShipmentStatus oldStatus = ShipmentStatus.IN_TRANSIT;
        ShipmentStatus newStatus = ShipmentStatus.IN_TRANSIT;

        StatusChange statusChange = StatusChange.of(
                trackingId,
                oldStatus,
                newStatus,
                "San Francisco Hub",
                "FedEx",
                LocalDateTime.now().plusDays(2)
        );

        // Act
        boolean result = statusChange.isSignificant();

        // Assert
        assertFalse(result, "Expected isSignificant to return false when statuses are the same");
    }

    @Test
    void of_ShouldCreateStatusChangeWithCorrectValues() {
        // Arrange
        TrackingId trackingId = TrackingId.of("TRK555666");
        ShipmentStatus oldStatus = ShipmentStatus.PENDING;
        ShipmentStatus newStatus = ShipmentStatus.OUT_FOR_DELIVERY;
        String location = "Chicago Warehouse";
        String carrier = "DHL";
        LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(3);

        // Act
        StatusChange statusChange = StatusChange.of(
                trackingId,
                oldStatus,
                newStatus,
                location,
                carrier,
                estimatedDelivery
        );

        // Assert
        assertNotNull(statusChange);
        assertEquals(trackingId, statusChange.getTrackingId());
        assertEquals(oldStatus, statusChange.getOldStatus());
        assertEquals(newStatus, statusChange.getNewStatus());
        assertEquals(location, statusChange.getLocation());
        assertEquals(carrier, statusChange.getCarrier());
        assertEquals(estimatedDelivery, statusChange.getEstimatedDelivery());
        assertNotNull(statusChange.getOccurredAt());
    }
}