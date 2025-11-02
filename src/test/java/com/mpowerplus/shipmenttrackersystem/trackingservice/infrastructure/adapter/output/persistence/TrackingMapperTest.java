package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.Tracking;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.TrackingId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TrackingMapper.
 * Tests bidirectional mapping between domain and persistence entities.
 */
class TrackingMapperTest {

    private TrackingMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TrackingMapper();
    }

    @Test
    void toEntity_ValidTracking_ReturnsValidEntity() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Tracking tracking = Tracking.builder()
                .trackingId(TrackingId.of("TRK12345"))
                .currentStatus(ShipmentStatus.IN_TRANSIT)
                .previousStatus(ShipmentStatus.PENDING)
                .lastLocation("Madrid Distribution Center")
                .carrier("DHL Express")
                .estimatedDelivery(now.plusDays(2))
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .lastCheckedAt(now)
                .build();

        // Act
        TrackingEntity entity = mapper.toEntity(tracking);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTrackingId()).isEqualTo("TRK12345");
        assertThat(entity.getCurrentStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(entity.getPreviousStatus()).isEqualTo(ShipmentStatus.PENDING);
        assertThat(entity.getLastLocation()).isEqualTo("Madrid Distribution Center");
        assertThat(entity.getCarrier()).isEqualTo("DHL Express");
        assertThat(entity.getEstimatedDelivery()).isEqualTo(now.plusDays(2));
        assertThat(entity.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
        assertThat(entity.getLastCheckedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_NullTracking_ReturnsNull() {
        // Act
        TrackingEntity entity = mapper.toEntity(null);

        // Assert
        assertThat(entity).isNull();
    }

    @Test
    void toEntity_TrackingWithNullFields_MapsCorrectly() {
        // Arrange
        Tracking tracking = Tracking.builder()
                .trackingId(TrackingId.of("TRK99999"))
                .currentStatus(ShipmentStatus.PENDING)
                .previousStatus(null)
                .lastLocation(null)
                .carrier(null)
                .estimatedDelivery(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastCheckedAt(null)
                .build();

        // Act
        TrackingEntity entity = mapper.toEntity(tracking);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getTrackingId()).isEqualTo("TRK99999");
        assertThat(entity.getCurrentStatus()).isEqualTo(ShipmentStatus.PENDING);
        assertThat(entity.getPreviousStatus()).isNull();
        assertThat(entity.getLastLocation()).isNull();
        assertThat(entity.getCarrier()).isNull();
        assertThat(entity.getEstimatedDelivery()).isNull();
        assertThat(entity.getLastCheckedAt()).isNull();
    }

    @Test
    void toDomain_ValidEntity_ReturnsValidTracking() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        TrackingEntity entity = TrackingEntity.builder()
                .id(1L)
                .trackingId("TRK67890")
                .currentStatus(ShipmentStatus.DELIVERED)
                .previousStatus(ShipmentStatus.OUT_FOR_DELIVERY)
                .lastLocation("Customer Address")
                .carrier("FedEx")
                .estimatedDelivery(now.minusHours(1))
                .createdAt(now.minusDays(3))
                .updatedAt(now)
                .lastCheckedAt(now.minusMinutes(5))
                .build();

        // Act
        Tracking tracking = mapper.toDomain(entity);

        // Assert
        assertThat(tracking).isNotNull();
        assertThat(tracking.getTrackingId().getValue()).isEqualTo("TRK67890");
        assertThat(tracking.getCurrentStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(tracking.getPreviousStatus()).isEqualTo(ShipmentStatus.OUT_FOR_DELIVERY);
        assertThat(tracking.getLastLocation()).isEqualTo("Customer Address");
        assertThat(tracking.getCarrier()).isEqualTo("FedEx");
        assertThat(tracking.getEstimatedDelivery()).isEqualTo(now.minusHours(1));
        assertThat(tracking.getCreatedAt()).isEqualTo(now.minusDays(3));
        assertThat(tracking.getUpdatedAt()).isEqualTo(now);
        assertThat(tracking.getLastCheckedAt()).isEqualTo(now.minusMinutes(5));
    }

    @Test
    void toDomain_NullEntity_ReturnsNull() {
        // Act
        Tracking tracking = mapper.toDomain(null);

        // Assert
        assertThat(tracking).isNull();
    }

    @Test
    void toDomain_EntityWithNullFields_MapsCorrectly() {
        // Arrange
        TrackingEntity entity = TrackingEntity.builder()
                .trackingId("TRK11111")
                .currentStatus(ShipmentStatus.CANCELLED)
                .previousStatus(null)
                .lastLocation(null)
                .carrier(null)
                .estimatedDelivery(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastCheckedAt(null)
                .build();

        // Act
        Tracking tracking = mapper.toDomain(entity);

        // Assert
        assertThat(tracking).isNotNull();
        assertThat(tracking.getTrackingId().getValue()).isEqualTo("TRK11111");
        assertThat(tracking.getCurrentStatus()).isEqualTo(ShipmentStatus.CANCELLED);
        assertThat(tracking.getPreviousStatus()).isNull();
        assertThat(tracking.getLastLocation()).isNull();
        assertThat(tracking.getCarrier()).isNull();
        assertThat(tracking.getEstimatedDelivery()).isNull();
        assertThat(tracking.getLastCheckedAt()).isNull();
    }

    @Test
    void bidirectionalMapping_TrackingToEntityToDomain_PreservesData() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Tracking original = Tracking.builder()
                .trackingId(TrackingId.of("TRK555"))
                .currentStatus(ShipmentStatus.OUT_FOR_DELIVERY)
                .previousStatus(ShipmentStatus.IN_TRANSIT)
                .lastLocation("Local Hub")
                .carrier("UPS")
                .estimatedDelivery(now.plusHours(4))
                .createdAt(now.minusDays(2))
                .updatedAt(now)
                .lastCheckedAt(now.minusMinutes(30))
                .build();

        // Act
        TrackingEntity entity = mapper.toEntity(original);
        Tracking result = mapper.toDomain(entity);

        // Assert
        assertThat(result.getTrackingId().getValue()).isEqualTo(original.getTrackingId().getValue());
        assertThat(result.getCurrentStatus()).isEqualTo(original.getCurrentStatus());
        assertThat(result.getPreviousStatus()).isEqualTo(original.getPreviousStatus());
        assertThat(result.getLastLocation()).isEqualTo(original.getLastLocation());
        assertThat(result.getCarrier()).isEqualTo(original.getCarrier());
        assertThat(result.getEstimatedDelivery()).isEqualTo(original.getEstimatedDelivery());
        assertThat(result.getCreatedAt()).isEqualTo(original.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(original.getUpdatedAt());
        assertThat(result.getLastCheckedAt()).isEqualTo(original.getLastCheckedAt());
    }
}