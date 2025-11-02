package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.messaging;

import com.mpowerplus.shipmenttrackersystem.shared.domain.event.ShipmentStatusChangedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KafkaEventPublisher.
 */
@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, ShipmentStatusChangedEvent> kafkaTemplate;

    private KafkaEventPublisher kafkaEventPublisher;

    private static final String TOPIC_NAME = "shipment-status-changes";

    @BeforeEach
    void setUp() {
        kafkaEventPublisher = new KafkaEventPublisher(kafkaTemplate);
        ReflectionTestUtils.setField(kafkaEventPublisher, "topicName", TOPIC_NAME);
    }

    @Test
    void publishStatusChangeEvent_SuccessfulPublish_SendsEventToKafka() {
        // Arrange
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.builder()
                .trackingId("TRACK123")
                .oldStatus("PENDING")
                .newStatus("IN_TRANSIT")
                .location("Madrid")
                .carrier("DHL")
                .timestamp(LocalDateTime.now())
                .source("tracking-service")
                .eventVersion("1.0")
                .build();

        ProducerRecord<String, ShipmentStatusChangedEvent> producerRecord =
                new ProducerRecord<>(TOPIC_NAME, event.getTrackingId(), event);
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(TOPIC_NAME, 0),
                0L, 0, 0L, 0, 0
        );
        SendResult<String, ShipmentStatusChangedEvent> sendResult = new SendResult<>(producerRecord, metadata);
        CompletableFuture<SendResult<String, ShipmentStatusChangedEvent>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq(TOPIC_NAME), eq("TRACK123"), eq(event))).thenReturn(future);

        // Act
        kafkaEventPublisher.publishStatusChangeEvent(event);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ShipmentStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ShipmentStatusChangedEvent.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo(TOPIC_NAME);
        assertThat(keyCaptor.getValue()).isEqualTo("TRACK123");
        assertThat(eventCaptor.getValue().getTrackingId()).isEqualTo("TRACK123");
        assertThat(eventCaptor.getValue().getOldStatus()).isEqualTo("PENDING");
        assertThat(eventCaptor.getValue().getNewStatus()).isEqualTo("IN_TRANSIT");
    }

    @Test
    void publishStatusChangeEvent_KafkaTemplateThrowsException_LogsErrorAndDoesNotThrow() {
        // Arrange
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.builder()
                .trackingId("TRACK456")
                .oldStatus("IN_TRANSIT")
                .newStatus("DELIVERED")
                .timestamp(LocalDateTime.now())
                .build();

        when(kafkaTemplate.send(any(String.class), any(String.class), any(ShipmentStatusChangedEvent.class)))
                .thenThrow(new RuntimeException("Kafka connection failed"));

        // Act - should not throw exception
        kafkaEventPublisher.publishStatusChangeEvent(event);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq(TOPIC_NAME), eq("TRACK456"), eq(event));
    }

    @Test
    void publishStatusChangeEvent_KafkaTemplateFails_CompletableFutureExceptionally() {
        // Arrange
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.builder()
                .trackingId("TRACK789")
                .oldStatus("PENDING")
                .newStatus("CANCELLED")
                .timestamp(LocalDateTime.now())
                .build();

        CompletableFuture<SendResult<String, ShipmentStatusChangedEvent>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Failed to send to Kafka"));

        when(kafkaTemplate.send(eq(TOPIC_NAME), eq("TRACK789"), eq(event))).thenReturn(future);

        // Act
        kafkaEventPublisher.publishStatusChangeEvent(event);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq(TOPIC_NAME), eq("TRACK789"), eq(event));
        // The error is logged but no exception is thrown
    }

    @Test
    void publishStatusChangeEvent_WithAllFields_PublishesCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.builder()
                .trackingId("TRACK999")
                .oldStatus("OUT_FOR_DELIVERY")
                .newStatus("DELIVERED")
                .location("Barcelona")
                .carrier("FedEx")
                .estimatedDelivery(now.plusDays(1))
                .timestamp(now)
                .changeReason("Package delivered successfully")
                .source("tracking-service")
                .eventVersion("1.0")
                .build();

        ProducerRecord<String, ShipmentStatusChangedEvent> producerRecord =
                new ProducerRecord<>(TOPIC_NAME, event.getTrackingId(), event);
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(TOPIC_NAME, 2),
                100L, 5, 0L, 0, 0
        );
        SendResult<String, ShipmentStatusChangedEvent> sendResult = new SendResult<>(producerRecord, metadata);
        CompletableFuture<SendResult<String, ShipmentStatusChangedEvent>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq(TOPIC_NAME), eq("TRACK999"), eq(event))).thenReturn(future);

        // Act
        kafkaEventPublisher.publishStatusChangeEvent(event);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq(TOPIC_NAME), eq("TRACK999"), any(ShipmentStatusChangedEvent.class));
    }
}
