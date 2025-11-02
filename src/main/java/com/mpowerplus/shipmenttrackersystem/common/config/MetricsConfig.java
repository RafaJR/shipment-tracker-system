package com.mpowerplus.shipmenttrackersystem.common.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer metrics configuration for business operations monitoring.
 * Provides custom metrics for tracking and notification services.
 */
@Configuration
public class MetricsConfig {

    // ========================================
    // Tracking Service Metrics
    // ========================================

    @Bean
    public Counter trackingChecksCounter(MeterRegistry registry) {
        return Counter.builder("shipment.tracking.checks.total")
                .description("Total number of tracking status checks")
                .tag("service", "tracking")
                .register(registry);
    }

    @Bean
    public Counter trackingChecksSuccessCounter(MeterRegistry registry) {
        return Counter.builder("shipment.tracking.checks.success")
                .description("Number of successful tracking checks")
                .tag("service", "tracking")
                .register(registry);
    }

    @Bean
    public Counter trackingChecksFailureCounter(MeterRegistry registry) {
        return Counter.builder("shipment.tracking.checks.failure")
                .description("Number of failed tracking checks")
                .tag("service", "tracking")
                .register(registry);
    }

    @Bean
    public Timer trackingChecksDurationTimer(MeterRegistry registry) {
        return Timer.builder("shipment.tracking.checks.duration")
                .description("Duration of tracking status checks")
                .tag("service", "tracking")
                .register(registry);
    }

    @Bean
    public Counter trackingCreatedCounter(MeterRegistry registry) {
        return Counter.builder("shipment.tracking.created.total")
                .description("Total number of trackings created")
                .tag("service", "tracking")
                .register(registry);
    }

    @Bean
    public Counter trackingStatusChangesCounter(MeterRegistry registry) {
        return Counter.builder("shipment.tracking.status.changes.total")
                .description("Total number of tracking status changes")
                .tag("service", "tracking")
                .register(registry);
    }

    // ========================================
    // Notification Service Metrics
    // ========================================

    @Bean
    public Counter notificationsSentCounter(MeterRegistry registry) {
        return Counter.builder("shipment.notifications.sent.total")
                .description("Total number of notifications sent")
                .tag("service", "notification")
                .register(registry);
    }

    @Bean
    public Counter notificationsFailedCounter(MeterRegistry registry) {
        return Counter.builder("shipment.notifications.failed.total")
                .description("Total number of failed notifications")
                .tag("service", "notification")
                .register(registry);
    }

    @Bean
    public Counter notificationsByTypeCounter(MeterRegistry registry) {
        return Counter.builder("shipment.notifications.by.type")
                .description("Number of notifications by type")
                .tag("service", "notification")
                .register(registry);
    }

    @Bean
    public Counter notificationsRetriesCounter(MeterRegistry registry) {
        return Counter.builder("shipment.notifications.retries.total")
                .description("Total number of notification retry attempts")
                .tag("service", "notification")
                .register(registry);
    }

    @Bean
    public Timer notificationProcessingDurationTimer(MeterRegistry registry) {
        return Timer.builder("shipment.notifications.processing.duration")
                .description("Duration of notification processing")
                .tag("service", "notification")
                .register(registry);
    }

    // ========================================
    // Kafka Event Metrics
    // ========================================

    @Bean
    public Counter kafkaEventsPublishedCounter(MeterRegistry registry) {
        return Counter.builder("shipment.kafka.events.published.total")
                .description("Total number of Kafka events published")
                .tag("service", "kafka")
                .register(registry);
    }

    @Bean
    public Counter kafkaEventsConsumedCounter(MeterRegistry registry) {
        return Counter.builder("shipment.kafka.events.consumed.total")
                .description("Total number of Kafka events consumed")
                .tag("service", "kafka")
                .register(registry);
    }

    @Bean
    public Counter kafkaEventsProcessingErrorsCounter(MeterRegistry registry) {
        return Counter.builder("shipment.kafka.events.errors.total")
                .description("Total number of Kafka event processing errors")
                .tag("service", "kafka")
                .register(registry);
    }

    // ========================================
    // External API Metrics
    // ========================================

    @Bean
    public Counter externalApiCallsCounter(MeterRegistry registry) {
        return Counter.builder("shipment.external.api.calls.total")
                .description("Total number of external API calls")
                .tag("service", "external-api")
                .register(registry);
    }

    @Bean
    public Counter externalApiCallsSuccessCounter(MeterRegistry registry) {
        return Counter.builder("shipment.external.api.calls.success")
                .description("Number of successful external API calls")
                .tag("service", "external-api")
                .register(registry);
    }

    @Bean
    public Counter externalApiCallsFailureCounter(MeterRegistry registry) {
        return Counter.builder("shipment.external.api.calls.failure")
                .description("Number of failed external API calls")
                .tag("service", "external-api")
                .register(registry);
    }

    @Bean
    public Timer externalApiCallsDurationTimer(MeterRegistry registry) {
        return Timer.builder("shipment.external.api.calls.duration")
                .description("Duration of external API calls")
                .tag("service", "external-api")
                .register(registry);
    }
}
