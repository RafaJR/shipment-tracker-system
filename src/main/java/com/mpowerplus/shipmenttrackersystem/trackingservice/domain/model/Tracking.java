package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Aggregate Root for Tracking domain.
 * Represents a shipment tracking entity with its lifecycle and business rules.
 *
 * Responsibilities:
 * - Maintain tracking state consistency
 * - Enforce business rules for status transitions
 * - Detect significant status changes
 * - Generate domain events
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tracking {

    private final TrackingId trackingId;
    private ShipmentStatus currentStatus;
    private ShipmentStatus previousStatus;
    private String lastLocation;
    private String carrier;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime lastCheckedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create a new Tracking from external API data
     */
    public static Tracking create(
            TrackingId trackingId,
            ShipmentStatus status,
            String location,
            String carrier,
            LocalDateTime estimatedDelivery) {

        LocalDateTime now = LocalDateTime.now();

        return Tracking.builder()
                .trackingId(trackingId)
                .currentStatus(status)
                .previousStatus(null)
                .lastLocation(location)
                .carrier(carrier)
                .estimatedDelivery(estimatedDelivery)
                .lastCheckedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates the shipment status if there's a change.
     * This is the core business logic that determines if a status change event should be emitted.
     *
     * @param newStatus the new status from external API
     * @param location  the current location
     * @return Optional containing StatusChange if status changed, empty otherwise
     */
    public Optional<StatusChange> updateStatus(ShipmentStatus newStatus, String location) {
        // No change detected
        if (this.currentStatus == newStatus) {
            this.lastCheckedAt = LocalDateTime.now();
            return Optional.empty();
        }

        // Status change detected - business rule validation
        if (!isValidTransition(this.currentStatus, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s for tracking %s",
                    this.currentStatus, newStatus, this.trackingId)
            );
        }

        // Create domain event before changing state
        StatusChange statusChange = StatusChange.of(
                this.trackingId,
                this.currentStatus,
                newStatus,
                location,
                this.carrier,
                this.estimatedDelivery
        );

        // Update aggregate state
        this.previousStatus = this.currentStatus;
        this.currentStatus = newStatus;
        this.lastLocation = location;
        this.lastCheckedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        return Optional.of(statusChange);
    }

    /**
     * Business rule: Validates if a status transition is allowed.
     * Can be extended with more complex transition rules.
     */
    private boolean isValidTransition(ShipmentStatus from, ShipmentStatus to) {
        // Business rule: Cannot transition from DELIVERED or CANCELLED to any other status
        if (from == ShipmentStatus.DELIVERED || from == ShipmentStatus.CANCELLED) {
            return false;
        }

        // Business rule: Cannot go back to PENDING
        if (to == ShipmentStatus.PENDING && from != ShipmentStatus.PENDING) {
            return false;
        }

        // All other transitions are valid
        return true;
    }

    /**
     * Updates the last checked timestamp without changing status
     */
    public void markAsChecked() {
        this.lastCheckedAt = LocalDateTime.now();
    }

    /**
     * Check if this tracking has been checked recently (within last hour)
     */
    public boolean wasRecentlyChecked() {
        if (lastCheckedAt == null) {
            return false;
        }
        return lastCheckedAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Check if delivery is overdue
     */
    public boolean isOverdue() {
        return estimatedDelivery != null
                && estimatedDelivery.isBefore(LocalDateTime.now())
                && currentStatus != ShipmentStatus.DELIVERED
                && currentStatus != ShipmentStatus.CANCELLED;
    }
}
