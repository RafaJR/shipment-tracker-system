package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.Tracking;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.TrackingId;
import org.springframework.stereotype.Component;

/**
 * Mapper between domain objects (Tracking) and persistence entities (TrackingEntity).
 * Handles bidirectional conversion to maintain separation between layers.
 */
@Component
public class TrackingMapper {

    /**
     * Converts a domain Tracking object to a TrackingEntity for persistence.
     *
     * @param tracking the domain object
     * @return the persistence entity
     */
    public TrackingEntity toEntity(Tracking tracking) {
        if (tracking == null) {
            return null;
        }

        return TrackingEntity.builder()
                .trackingId(tracking.getTrackingId().getValue())
                .currentStatus(tracking.getCurrentStatus())
                .previousStatus(tracking.getPreviousStatus())
                .lastLocation(tracking.getLastLocation())
                .carrier(tracking.getCarrier())
                .estimatedDelivery(tracking.getEstimatedDelivery())
                .createdAt(tracking.getCreatedAt())
                .updatedAt(tracking.getUpdatedAt())
                .lastCheckedAt(tracking.getLastCheckedAt())
                .build();
    }

    /**
     * Converts a TrackingEntity from persistence to a domain Tracking object.
     *
     * @param entity the persistence entity
     * @return the domain object
     */
    public Tracking toDomain(TrackingEntity entity) {
        if (entity == null) {
            return null;
        }

        TrackingId trackingId = TrackingId.of(entity.getTrackingId());

        return Tracking.builder()
                .trackingId(trackingId)
                .currentStatus(entity.getCurrentStatus())
                .previousStatus(entity.getPreviousStatus())
                .lastLocation(entity.getLastLocation())
                .carrier(entity.getCarrier())
                .estimatedDelivery(entity.getEstimatedDelivery())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastCheckedAt(entity.getLastCheckedAt())
                .build();
    }
}
