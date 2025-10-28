package com.mpowerplus.shipmenttrackersystem.trackingservice.application.service;

import com.mpowerplus.shipmenttrackersystem.shared.domain.event.ShipmentStatusChangedEvent;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.ExternalShipmentData;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingRequest;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingResponse;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.input.TrackingUseCase;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.EventPublisherPort;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalShipmentApiPort;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.StatusChange;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.Tracking;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.TrackingId;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application Service implementing tracking use cases.
 * Orchestrates domain logic, external API calls, and event publishing.
 * <p>
 * Following Application Service pattern:
 * - Implements use case interfaces
 * - Orchestrates domain objects
 * - Delegates to output ports
 * - Manages transactions
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrackingApplicationService implements TrackingUseCase {

    private final TrackingRepository trackingRepository;
    private final ExternalShipmentApiPort externalApiPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    public TrackingResponse checkTrackingStatus(TrackingRequest request) {
        log.info("Checking tracking status for: {}", request.trackingId());

        TrackingId trackingId = TrackingId.of(request.trackingId());

        // Fetch current status from external API
        ExternalShipmentData externalData = externalApiPort.fetchShipmentStatus(request.trackingId());

        // Find or create tracking record
        Tracking tracking = trackingRepository.findByTrackingId(trackingId)
                .orElseGet(() -> createNewTracking(trackingId, externalData));

        // Update status and check for changes
        ShipmentStatus newStatus = mapToShipmentStatus(externalData.status());
        Optional<StatusChange> statusChange = tracking.updateStatus(newStatus, externalData.location());

        // Save updated tracking
        Tracking savedTracking = trackingRepository.save(tracking);

        // Publish event if status changed
        statusChange.ifPresent(change -> {
            log.info("Status changed from {} to {} for tracking {}",
                    change.getOldStatus(), change.getNewStatus(), trackingId);
            publishStatusChangeEvent(change);
        });

        return mapToResponse(savedTracking);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingResponse getTracking(String trackingId) {
        log.info("Retrieving tracking: {}", trackingId);

        TrackingId id = TrackingId.of(trackingId);
        Tracking tracking = trackingRepository.findByTrackingId(id)
                .orElseThrow(() -> new TrackingNotFoundException("Tracking not found: " + trackingId));

        return mapToResponse(tracking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingResponse> getAllTrackings() {
        log.info("Retrieving all trackings");

        return trackingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TrackingResponse createTracking(TrackingRequest request) {
        log.info("Creating new tracking: {}", request.trackingId());

        TrackingId trackingId = TrackingId.of(request.trackingId());

        // Check if tracking already exists
        if (trackingRepository.existsByTrackingId(trackingId)) {
            throw new TrackingAlreadyExistsException("Tracking already exists: " + request.trackingId());
        }

        // Fetch initial data from external API
        ExternalShipmentData externalData = externalApiPort.fetchShipmentStatus(request.trackingId());

        // Create new tracking
        Tracking tracking = createNewTracking(trackingId, externalData);
        Tracking savedTracking = trackingRepository.save(tracking);

        log.info("Created tracking: {}", trackingId);
        return mapToResponse(savedTracking);
    }

    // ========== Private Helper Methods ==========

    private Tracking createNewTracking(TrackingId trackingId, ExternalShipmentData externalData) {
        ShipmentStatus status = mapToShipmentStatus(externalData.status());
        return Tracking.create(
                trackingId,
                status,
                externalData.location(),
                externalData.carrier(),
                externalData.estimatedDelivery()
        );
    }

    private void publishStatusChangeEvent(StatusChange statusChange) {
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.builder()
                .trackingId(statusChange.getTrackingId().getValue())
                .oldStatus(statusChange.getOldStatus().name())
                .newStatus(statusChange.getNewStatus().name())
                .location(statusChange.getLocation())
                .carrier(statusChange.getCarrier())
                .estimatedDelivery(statusChange.getEstimatedDelivery())
                .timestamp(statusChange.getOccurredAt())
                .source("tracking-service")
                .eventVersion("1.0")
                .build();

        eventPublisherPort.publishStatusChangeEvent(event);
    }

    private TrackingResponse mapToResponse(Tracking tracking) {
        return new TrackingResponse(
                tracking.getTrackingId().getValue(),
                tracking.getCurrentStatus().name(),
                tracking.getPreviousStatus() != null ? tracking.getPreviousStatus().name() : null,
                tracking.getLastLocation(),
                tracking.getCarrier(),
                tracking.getEstimatedDelivery(),
                tracking.getCreatedAt(),
                tracking.getUpdatedAt(),
                tracking.getLastCheckedAt(),
                tracking.isOverdue(),
                tracking.getCurrentStatus().getDisplayName(),
                tracking.getCurrentStatus().getDescription()
        );
    }

    private ShipmentStatus mapToShipmentStatus(String externalStatus) {
        if (externalStatus == null) {
            return ShipmentStatus.PENDING;
        }

        try {
            return ShipmentStatus.valueOf(externalStatus.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown status from external API: {}. Defaulting to PENDING", externalStatus);
            return ShipmentStatus.PENDING;
        }
    }
}
