package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentStatusTest {

    /**
     * This test class verifies the behavior of the
     * `isSignificantChangeTo` method in the `ShipmentStatus` enum.
     * This method determines if transitioning from one status to
     * another constitutes a significant change.
     */

    @Test
    void testIsSignificantChangeTo_SameStatus() {
        // Arrange
        ShipmentStatus currentStatus = ShipmentStatus.IN_TRANSIT;
        ShipmentStatus newStatus = ShipmentStatus.IN_TRANSIT;

        // Act
        boolean result = currentStatus.isSignificantChangeTo(newStatus);

        // Assert
        assertFalse(result, "Transitioning to the same status should not be a significant change.");
    }

    @Test
    void testIsSignificantChangeTo_DifferentStatus() {
        // Arrange
        ShipmentStatus currentStatus = ShipmentStatus.PENDING;
        ShipmentStatus newStatus = ShipmentStatus.IN_TRANSIT;

        // Act
        boolean result = currentStatus.isSignificantChangeTo(newStatus);

        // Assert
        assertTrue(result, "Transitioning to a different status should be a significant change.");
    }

    @Test
    void testIsSignificantChangeTo_NewStatusNull() {
        // Arrange
        ShipmentStatus currentStatus = ShipmentStatus.DELIVERED;
        ShipmentStatus newStatus = null;

        // Act
        boolean result = currentStatus.isSignificantChangeTo(newStatus);

        // Assert
        assertFalse(result, "Transitioning to a null status should not be a significant change.");
    }

    @Test
    void testIsSignificantChangeTo_FromNullToNull() {
        // Arrange
        ShipmentStatus currentStatus = null;
        ShipmentStatus newStatus = null;

        // Act and Assert
        assertThrows(NullPointerException.class,
                () -> currentStatus.isSignificantChangeTo(newStatus),
                "Calling isSignificantChangeTo on null should throw NullPointerException.");
    }

    @Test
    void testIsSignificantChangeTo_FromNullToValidStatus() {
        // Arrange
        ShipmentStatus currentStatus = null;
        ShipmentStatus newStatus = ShipmentStatus.CANCELLED;

        // Act and Assert
        assertThrows(NullPointerException.class,
                () -> currentStatus.isSignificantChangeTo(newStatus),
                "Calling isSignificantChangeTo on null should throw NullPointerException.");
    }
}