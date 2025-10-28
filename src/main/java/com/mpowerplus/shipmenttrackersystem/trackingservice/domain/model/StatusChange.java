package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Domain Event representing a status change within the Tracking aggregate.
 * This is an internal domain event that will be translated to the shared
 * ShipmentStatusChangedEvent for publishing to Kafka.
 *
 * Domain events are:
 * - Immutable
 * - Represent facts that happened in the domain
 * - Named in past tense
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatusChange {

    private final TrackingId trackingId;
    private final ShipmentStatus oldStatus;
    private final ShipmentStatus newStatus;
    private final String location;
    private final String carrier;
    private final LocalDateTime estimatedDelivery;
    private final LocalDateTime occurredAt;

    /**
     * Factory method to create a status change event
     */
    public static StatusChange of(
            TrackingId trackingId,
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus,
            String location,
            String carrier,
            LocalDateTime estimatedDelivery) {

        return StatusChange.builder()
                .trackingId(trackingId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .location(location)
                .carrier(carrier)
                .estimatedDelivery(estimatedDelivery)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    /**
     * Check if this change is significant enough to notify
     */
    public boolean isSignificant() {
        return oldStatus.isSignificantChangeTo(newStatus);
    }
}
