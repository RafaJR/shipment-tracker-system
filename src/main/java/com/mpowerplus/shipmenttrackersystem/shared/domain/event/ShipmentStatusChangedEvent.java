package com.mpowerplus.shipmenttrackersystem.shared.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Domain Event representing a significant change in shipment status.
 * This event is published to Kafka when a shipment transitions between states.
 *
 * Shared Kernel - Used by both TrackingService (producer) and NotificationService (consumer).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusChangedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the shipment (from external API)
     */
    private String trackingId;

    /**
     * Previous status before the change
     */
    private String oldStatus;

    /**
     * New status after the change
     */
    private String newStatus;

    /**
     * Current location of the shipment
     */
    private String location;

    /**
     * Carrier handling the shipment
     */
    private String carrier;

    /**
     * Estimated delivery timestamp
     */
    private LocalDateTime estimatedDelivery;

    /**
     * Timestamp when the status change occurred
     */
    private LocalDateTime timestamp;

    /**
     * Additional context or notes about the status change
     */
    private String changeReason;

    /**
     * Source system that detected the change (e.g., "tracking-service")
     */
    private String source;

    /**
     * Event version for schema evolution
     */
    private String eventVersion;

    /**
     * Factory method to create an event with default values
     */
    public static ShipmentStatusChangedEvent of(
            String trackingId,
            String oldStatus,
            String newStatus,
            LocalDateTime timestamp) {
        return ShipmentStatusChangedEvent.builder()
                .trackingId(trackingId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .timestamp(timestamp)
                .source("tracking-service")
                .eventVersion("1.0")
                .build();
    }
}
