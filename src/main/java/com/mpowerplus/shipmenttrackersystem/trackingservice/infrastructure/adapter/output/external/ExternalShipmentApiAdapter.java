package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.external;

import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.ExternalShipmentData;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalApiException;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalShipmentApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Adapter for calling external shipment tracking APIs.
 * Implements the ExternalShipmentApiPort using WebClient for HTTP communication.
 */
@Component
@Slf4j
public class ExternalShipmentApiAdapter implements ExternalShipmentApiPort {

    private final WebClient webClient;
    private final int maxRetryAttempts;
    private final long backoffDelay;

    public ExternalShipmentApiAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${external.api.shipment.base-url}") String baseUrl,
            @Value("${external.api.shipment.timeout:5000}") int timeout,
            @Value("${external.api.shipment.retry.max-attempts:3}") int maxRetryAttempts,
            @Value("${external.api.shipment.retry.backoff-delay:1000}") long backoffDelay
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
        this.maxRetryAttempts = maxRetryAttempts;
        this.backoffDelay = backoffDelay;

        log.info("ExternalShipmentApiAdapter initialized with baseUrl: {}, timeout: {}ms, maxRetryAttempts: {}",
                baseUrl, timeout, maxRetryAttempts);
    }

    @Override
    public ExternalShipmentData fetchShipmentStatus(String trackingId) {
        log.debug("Fetching shipment status for tracking ID: {}", trackingId);

        try {
            ExternalShipmentData response = webClient
                    .get()
                    .uri("/shipments/{trackingId}", trackingId)
                    .retrieve()
                    .bodyToMono(ExternalShipmentData.class)
                    .retryWhen(Retry.backoff(maxRetryAttempts, Duration.ofMillis(backoffDelay))
                            .filter(this::isRetryableException)
                            .doBeforeRetry(retrySignal ->
                                log.warn("Retrying request for tracking ID: {} (attempt {})",
                                        trackingId, retrySignal.totalRetries() + 1))
                    )
                    .block();

            if (response == null) {
                throw new ExternalApiException("Received null response from external API for tracking ID: " + trackingId);
            }

            log.debug("Successfully fetched shipment status for tracking ID: {}", trackingId);
            return response;

        } catch (WebClientResponseException e) {
            log.error("HTTP error while fetching shipment status for tracking ID: {}. Status: {}, Body: {}",
                    trackingId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new ExternalApiException(
                    String.format("External API returned error status %s for tracking ID: %s",
                            e.getStatusCode(), trackingId),
                    e
            );
        } catch (Exception e) {
            log.error("Unexpected error while fetching shipment status for tracking ID: {}", trackingId, e);
            throw new ExternalApiException(
                    "Failed to fetch shipment status for tracking ID: " + trackingId,
                    e
            );
        }
    }

    /**
     * Determines if an exception is retryable.
     * Network errors and 5xx server errors are considered retryable.
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            // Retry on 5xx server errors, but not on 4xx client errors
            return statusCode >= 500 && statusCode < 600;
        }
        // Retry on network errors (timeouts, connection refused, etc.)
        return true;
    }
}
