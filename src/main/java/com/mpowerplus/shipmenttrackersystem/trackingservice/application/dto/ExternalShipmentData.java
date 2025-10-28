package com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing shipment data from external API.
 * Maps external API response to internal structure.
 */
public record ExternalShipmentData(
        String trackingId,
        String status,
        String location,
        String carrier,
        LocalDateTime estimatedDelivery,
        String additionalInfo
) {
}
