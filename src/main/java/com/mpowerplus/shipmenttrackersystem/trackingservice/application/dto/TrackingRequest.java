package com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for Tracking requests.
 * Used for incoming API requests.
 */
@Schema(description = "Request object for tracking a shipment")
public record TrackingRequest(
        @NotBlank(message = "Tracking ID is required")
        @Size(max = 50, message = "Tracking ID cannot exceed 50 characters")
        @Pattern(regexp = "^[A-Za-z0-9-_]+$",
                message = "Tracking ID can only contain letters, numbers, hyphens, and underscores")
        @Schema(description = "Unique tracking identifier",
                example = "TRK001234567890",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String trackingId
) {
}
