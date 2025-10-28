package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object representing a Tracking Identifier.
 * Encapsulates the business rules and validation for tracking IDs.
 *
 * As a Value Object:
 * - Immutable
 * - Equality based on value, not identity
 * - No lifecycle management
 */
@Getter
@EqualsAndHashCode
public class TrackingId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    /**
     * Private constructor to enforce validation through factory method
     */
    private TrackingId(String value) {
        this.value = value;
    }

    /**
     * Factory method to create a TrackingId with validation
     *
     * @param value the tracking ID string
     * @return a validated TrackingId instance
     * @throws IllegalArgumentException if value is invalid
     */
    public static TrackingId of(String value) {
        validateTrackingId(value);
        return new TrackingId(value);
    }

    /**
     * Validates the tracking ID according to business rules
     */
    private static void validateTrackingId(String value) {
        Objects.requireNonNull(value, "Tracking ID cannot be null");

        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking ID cannot be empty");
        }

        if (value.length() > 50) {
            throw new IllegalArgumentException("Tracking ID cannot exceed 50 characters");
        }

        // Additional validation: must contain only alphanumeric characters and basic symbols
        if (!value.matches("^[A-Za-z0-9-_]+$")) {
            throw new IllegalArgumentException(
                "Tracking ID can only contain letters, numbers, hyphens, and underscores"
            );
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
