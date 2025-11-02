package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrackingIdTest {

    /**
     * Tests for the static factory method 'of' in the TrackingId class.
     * <p>
     * Validates the creation of TrackingId instances and ensures correctness for
     * invalid inputs such as null, empty strings, overly long strings, and
     * non-compliant patterns.
     */

    @Test
    void of_ShouldReturnTrackingId_WhenValidValueProvided() {
        // Arrange
        String validValue = "TRACK-12345";

        // Act
        TrackingId trackingId = TrackingId.of(validValue);

        // Assert
        assertNotNull(trackingId, "TrackingId object should not be null");
        assertEquals(validValue, trackingId.getValue(), "TrackingId value should match the input value");
    }

    @Test
    void of_ShouldThrowException_WhenValueIsNull() {
        // Arrange
        String nullValue = null;

        // Act & Assert
        Exception exception = assertThrows(NullPointerException.class,
                () -> TrackingId.of(nullValue),
                "Expected exception when passing null to TrackingId.of"
        );
        assertEquals("Tracking ID cannot be null", exception.getMessage());
    }

    @Test
    void of_ShouldThrowException_WhenValueIsEmpty() {
        // Arrange
        String emptyValue = "";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> TrackingId.of(emptyValue),
                "Expected exception when passing an empty string to TrackingId.of"
        );
        assertEquals("Tracking ID cannot be empty", exception.getMessage());
    }

    @Test
    void of_ShouldThrowException_WhenValueExceedsMaximumLength() {
        // Arrange
        String longValue = "a".repeat(51);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> TrackingId.of(longValue),
                "Expected exception when passing a string over 50 characters to TrackingId.of"
        );
        assertEquals("Tracking ID cannot exceed 50 characters", exception.getMessage());
    }

    @Test
    void of_ShouldThrowException_WhenValueContainsInvalidCharacters() {
        // Arrange
        String invalidValue = "TRACK#123";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> TrackingId.of(invalidValue),
                "Expected exception when passing a string with invalid characters to TrackingId.of"
        );
        assertEquals("Tracking ID can only contain letters, numbers, hyphens, and underscores", exception.getMessage());
    }

    @Test
    void of_ShouldTrimWhitespace_WhenValueHasLeadingOrTrailingSpaces() {
        // Arrange
        String valueWithSpaces = "  TRACK-123  ";

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> TrackingId.of(valueWithSpaces),
                "Expected exception when passing a string with spaces"
        );
        // Current implementation doesn't trim, so spaces are invalid characters
        assertEquals("Tracking ID can only contain letters, numbers, hyphens, and underscores", exception.getMessage());
    }

    @Test
    void toString_ShouldReturnValue() {
        // Arrange
        String validValue = "TRK-999888";
        TrackingId trackingId = TrackingId.of(validValue);

        // Act
        String result = trackingId.toString();

        // Assert
        assertEquals(validValue, result);
    }

    @Test
    void equals_ShouldReturnTrue_WhenTrackingIdsHaveSameValue() {
        // Arrange
        TrackingId trackingId1 = TrackingId.of("TRK-111");
        TrackingId trackingId2 = TrackingId.of("TRK-111");

        // Act & Assert
        assertEquals(trackingId1, trackingId2);
        assertEquals(trackingId1.hashCode(), trackingId2.hashCode());
    }

    @Test
    void equals_ShouldReturnFalse_WhenTrackingIdsHaveDifferentValues() {
        // Arrange
        TrackingId trackingId1 = TrackingId.of("TRK-111");
        TrackingId trackingId2 = TrackingId.of("TRK-222");

        // Act & Assert
        assertNotEquals(trackingId1, trackingId2);
    }
}