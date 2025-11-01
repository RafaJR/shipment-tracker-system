package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.messaging;

import com.mpowerplus.shipmenttrackersystem.shared.domain.event.ShipmentStatusChangedEvent;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter for publishing domain events.
 * Implements EventPublisherPort using Spring Kafka.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, ShipmentStatusChangedEvent> kafkaTemplate;

    @Value("${kafka.topics.shipment-status-changes}")
    private String topicName;

    @Override
    public void publishStatusChangeEvent(ShipmentStatusChangedEvent event) {
        log.info("Publishing status change event for tracking ID: {} from {} to {}",
                event.getTrackingId(), event.getOldStatus(), event.getNewStatus());

        try {
            CompletableFuture<SendResult<String, ShipmentStatusChangedEvent>> future =
                    kafkaTemplate.send(topicName, event.getTrackingId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published event for tracking ID: {} to topic: {} [partition: {}, offset: {}]",
                            event.getTrackingId(),
                            topicName,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event for tracking ID: {} to topic: {}",
                            event.getTrackingId(), topicName, ex);
                }
            });

        } catch (Exception e) {
            log.error("Error publishing event for tracking ID: {} to Kafka topic: {}",
                    event.getTrackingId(), topicName, e);
            // Note: We log the error but don't throw to avoid blocking the main transaction
            // In production, consider: dead letter queue, retry queue, or circuit breaker
        }
    }
}
