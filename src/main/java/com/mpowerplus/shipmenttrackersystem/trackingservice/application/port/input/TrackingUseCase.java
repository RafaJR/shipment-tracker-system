package com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.input;

import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingRequest;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingResponse;

import java.util.List;

/**
 * Input Port (Driving Port) for Tracking use cases.
 * Defines the operations available to external actors (REST controllers, etc.)
 *
 * Following Hexagonal Architecture:
 * - Interface in application layer
 * - Implemented by application services
 * - Used by adapters (controllers)
 */
public interface TrackingUseCase {

    /**
     * Checks the status of a shipment by querying the external API.
     * If status has changed, publishes an event to Kafka.
     *
     * @param request the tracking request with tracking ID
     * @return the updated tracking information
     */
    TrackingResponse checkTrackingStatus(TrackingRequest request);

    /**
     * Retrieves tracking information by tracking ID.
     *
     * @param trackingId the tracking identifier
     * @return the tracking information if found
     */
    TrackingResponse getTracking(String trackingId);

    /**
     * Retrieves all tracked shipments.
     *
     * @return list of all tracking records
     */
    List<TrackingResponse> getAllTrackings();

    /**
     * Creates a new tracking record for monitoring.
     *
     * @param request the tracking request with initial data
     * @return the created tracking information
     */
    TrackingResponse createTracking(TrackingRequest request);
}
