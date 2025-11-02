package com.mpowerplus.shipmenttrackersystem.notificationservice.infrastructure.adapter.input.messaging;

import com.mpowerplus.shipmenttrackersystem.notificationservice.application.service.NotificationService;
import com.mpowerplus.shipmenttrackersystem.shared.domain.event.ShipmentStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer adapter for shipment status change events.
 * Input adapter in hexagonal architecture.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentStatusEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.shipment-status-changes}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStatusChangeEvent(
            @Payload ShipmentStatusChangedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received shipment status change event: trackingId={}, oldStatus={}, newStatus={}, partition={}, offset={}",
                event.getTrackingId(), event.getOldStatus(), event.getNewStatus(), partition, offset);

        try {
            notificationService.processStatusChangeEvent(event);

            // Manual commit after successful processing
            acknowledgment.acknowledge();

            log.info("Successfully processed event for tracking ID: {}", event.getTrackingId());

        } catch (Exception e) {
            log.error("Failed to process event for tracking ID: {}. Error: {}",
                    event.getTrackingId(), e.getMessage(), e);

            // Don't acknowledge - message will be redelivered
            // In production, consider: dead letter queue, retry topic, or circuit breaker
        }
    }
}
