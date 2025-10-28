package com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Tracking responses.
 * Used for outgoing API responses.
 */
@Schema(description = "Response object containing shipment tracking information")
public record TrackingResponse(
        @Schema(description = "Unique tracking identifier", example = "TRK001234567890")
        String trackingId,

        @Schema(description = "Current shipment status", example = "IN_TRANSIT")
        String currentStatus,

        @Schema(description = "Previous shipment status", example = "PENDING")
        String previousStatus,

        @Schema(description = "Last known location", example = "Madrid Distribution Center")
        String lastLocation,

        @Schema(description = "Carrier handling the shipment", example = "DHL Express")
        String carrier,

        @Schema(description = "Estimated delivery date and time", example = "2025-10-29T14:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime estimatedDelivery,

        @Schema(description = "Timestamp when tracking was created", example = "2025-10-25T08:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of last update", example = "2025-10-27T10:15:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,

        @Schema(description = "Timestamp of last external API check", example = "2025-10-27T10:15:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime lastCheckedAt,

        @Schema(description = "Indicates if delivery is overdue", example = "false")
        Boolean isOverdue,

        @Schema(description = "Status display name", example = "In Transit")
        String statusDisplayName,

        @Schema(description = "Status description", example = "Shipment is currently being transported")
        String statusDescription
) {
}
