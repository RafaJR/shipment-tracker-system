package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.Tracking;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.TrackingId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrackingRepositoryAdapter.
 * Tests the adapter logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class TrackingRepositoryAdapterTest {

    @Mock
    private TrackingJpaRepository jpaRepository;

    @Mock
    private TrackingMapper mapper;

    @InjectMocks
    private TrackingRepositoryAdapter trackingRepositoryAdapter;

    private Tracking sampleTracking;
    private TrackingEntity sampleEntity;
    private TrackingId trackingId;

    @BeforeEach
    void setUp() {
        trackingId = TrackingId.of("TRK123456");

        sampleTracking = Tracking.create(
                trackingId,
                ShipmentStatus.IN_TRANSIT,
                "Madrid Distribution Center",
                "DHL Express",
                LocalDateTime.now().plusDays(2)
        );

        sampleEntity = TrackingEntity.builder()
                .id(1L)
                .trackingId("TRK123456")
                .currentStatus(ShipmentStatus.IN_TRANSIT)
                .lastLocation("Madrid Distribution Center")
                .carrier("DHL Express")
                .estimatedDelivery(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void findByTrackingId_ExistingTracking_ReturnsTracking() {
        // Arrange
        when(jpaRepository.findByTrackingId("TRK123456")).thenReturn(Optional.of(sampleEntity));
        when(mapper.toDomain(sampleEntity)).thenReturn(sampleTracking);

        // Act
        Optional<Tracking> result = trackingRepositoryAdapter.findByTrackingId(trackingId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTrackingId()).isEqualTo(trackingId);
        assertThat(result.get().getCurrentStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        verify(jpaRepository).findByTrackingId("TRK123456");
        verify(mapper).toDomain(sampleEntity);
    }

    @Test
    void findByTrackingId_NonExistingTracking_ReturnsEmpty() {
        // Arrange
        TrackingId nonExistingId = TrackingId.of("NONEXISTENT");
        when(jpaRepository.findByTrackingId("NONEXISTENT")).thenReturn(Optional.empty());

        // Act
        Optional<Tracking> result = trackingRepositoryAdapter.findByTrackingId(nonExistingId);

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository).findByTrackingId("NONEXISTENT");
        verify(mapper, never()).toDomain(any());
    }

    @Test
    void save_ValidTracking_ReturnsSavedTracking() {
        // Arrange
        when(mapper.toEntity(sampleTracking)).thenReturn(sampleEntity);
        when(jpaRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(mapper.toDomain(sampleEntity)).thenReturn(sampleTracking);

        // Act
        Tracking result = trackingRepositoryAdapter.save(sampleTracking);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTrackingId()).isEqualTo(trackingId);
        assertThat(result.getLastLocation()).isEqualTo("Madrid Distribution Center");
        verify(mapper).toEntity(sampleTracking);
        verify(jpaRepository).save(sampleEntity);
        verify(mapper).toDomain(sampleEntity);
    }

    @Test
    void findAll_MultipleTrackings_ReturnsAllTrackings() {
        // Arrange
        TrackingEntity entity1 = TrackingEntity.builder()
                .trackingId("TRK111")
                .currentStatus(ShipmentStatus.DELIVERED)
                .build();
        TrackingEntity entity2 = TrackingEntity.builder()
                .trackingId("TRK222")
                .currentStatus(ShipmentStatus.PENDING)
                .build();

        Tracking tracking1 = Tracking.create(
                TrackingId.of("TRK111"),
                ShipmentStatus.DELIVERED,
                "Location1",
                "Carrier1",
                null
        );
        Tracking tracking2 = Tracking.create(
                TrackingId.of("TRK222"),
                ShipmentStatus.PENDING,
                "Location2",
                "Carrier2",
                null
        );

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(tracking1);
        when(mapper.toDomain(entity2)).thenReturn(tracking2);

        // Act
        List<Tracking> result = trackingRepositoryAdapter.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTrackingId().getValue()).isEqualTo("TRK111");
        assertThat(result.get(1).getTrackingId().getValue()).isEqualTo("TRK222");
        verify(jpaRepository).findAll();
        verify(mapper, times(2)).toDomain(any(TrackingEntity.class));
    }

    @Test
    void existsByTrackingId_ExistingTracking_ReturnsTrue() {
        // Arrange
        when(jpaRepository.existsByTrackingId("TRK123456")).thenReturn(true);

        // Act
        boolean result = trackingRepositoryAdapter.existsByTrackingId(trackingId);

        // Assert
        assertThat(result).isTrue();
        verify(jpaRepository).existsByTrackingId("TRK123456");
    }

    @Test
    void existsByTrackingId_NonExistingTracking_ReturnsFalse() {
        // Arrange
        TrackingId nonExistingId = TrackingId.of("NONEXISTENT");
        when(jpaRepository.existsByTrackingId("NONEXISTENT")).thenReturn(false);

        // Act
        boolean result = trackingRepositoryAdapter.existsByTrackingId(nonExistingId);

        // Assert
        assertThat(result).isFalse();
        verify(jpaRepository).existsByTrackingId("NONEXISTENT");
    }

    @Test
    void deleteByTrackingId_ExistingTracking_DeletesTracking() {
        // Arrange
        doNothing().when(jpaRepository).deleteByTrackingId("TRK123456");

        // Act
        trackingRepositoryAdapter.deleteByTrackingId(trackingId);

        // Assert
        verify(jpaRepository).deleteByTrackingId("TRK123456");
    }
}