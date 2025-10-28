package com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output;

import com.mpowerplus.shipmenttrackersystem.shared.domain.event.ShipmentStatusChangedEvent;

/**
 * Output Port (Driven Port) for publishing domain events.
 * Abstracts the event publishing mechanism (Kafka, RabbitMQ, etc.)
 *
 * Following Hexagonal Architecture:
 * - Interface in application layer
 * - Implemented by infrastructure adapters (Kafka, etc.)
 * - Used by application services
 */
public interface EventPublisherPort {

    /**
     * Publishes a shipment status change event.
     *
     * @param event the status change event to publish
     */
    void publishStatusChangeEvent(ShipmentStatusChangedEvent event);
}
