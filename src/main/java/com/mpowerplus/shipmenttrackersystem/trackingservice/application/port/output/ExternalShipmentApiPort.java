package com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output;

import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.ExternalShipmentData;

/**
 * Output Port (Driven Port) for external shipment API integration.
 * Abstracts communication with external tracking APIs.
 *
 * Following Hexagonal Architecture:
 * - Interface in application layer
 * - Implemented by infrastructure adapters (WebClient, RestTemplate, etc.)
 * - Used by application services
 */
public interface ExternalShipmentApiPort {

    /**
     * Fetches shipment status from external API.
     *
     * @param trackingId the tracking identifier
     * @return the shipment data from external API
     * @throws ExternalApiException if the external API call fails
     */
    ExternalShipmentData fetchShipmentStatus(String trackingId);
}
