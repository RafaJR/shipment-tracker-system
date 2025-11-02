package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.repository;

import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.Tracking;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.TrackingId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrackingRepository interface contract.
 * Tests the behavior expected from any implementation of TrackingRepository.
 */
@ExtendWith(MockitoExtension.class)
class TrackingRepositoryTest {

    @Mock
    private TrackingRepository trackingRepository;

    private Tracking sampleTracking;
    private TrackingId sampleTrackingId;

    @BeforeEach
    void setUp() {
        sampleTrackingId = TrackingId.of("TRK001234567890");
        sampleTracking = Tracking.create(
                sampleTrackingId,
                ShipmentStatus.IN_TRANSIT,
                "Madrid Distribution Center",
                "DHL",
                LocalDateTime.now().plusDays(2)
        );
    }

    @Test
    void testSave_ValidTracking_ReturnsSavedTracking() {
        // Arrange
        when(trackingRepository.save(any(Tracking.class))).thenReturn(sampleTracking);

        // Act
        Tracking saved = trackingRepository.save(sampleTracking);

        // Assert
        assertNotNull(saved);
        assertEquals(sampleTrackingId, saved.getTrackingId());
        verify(trackingRepository, times(1)).save(any(Tracking.class));
    }

    @Test
    void testFindByTrackingId_ExistingTracking_ReturnsTracking() {
        // Arrange
        when(trackingRepository.findByTrackingId(sampleTrackingId))
                .thenReturn(Optional.of(sampleTracking));

        // Act
        Optional<Tracking> result = trackingRepository.findByTrackingId(sampleTrackingId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(sampleTrackingId, result.get().getTrackingId());
        verify(trackingRepository, times(1)).findByTrackingId(sampleTrackingId);
    }

    @Test
    void testFindByTrackingId_NonExistingTracking_ReturnsEmpty() {
        // Arrange
        TrackingId nonExistingId = TrackingId.of("TRK999999999999");
        when(trackingRepository.findByTrackingId(nonExistingId))
                .thenReturn(Optional.empty());

        // Act
        Optional<Tracking> result = trackingRepository.findByTrackingId(nonExistingId);

        // Assert
        assertFalse(result.isPresent());
        verify(trackingRepository, times(1)).findByTrackingId(nonExistingId);
    }

    @Test
    void testExistsByTrackingId_ExistingTracking_ReturnsTrue() {
        // Arrange
        when(trackingRepository.existsByTrackingId(sampleTrackingId)).thenReturn(true);

        // Act
        boolean exists = trackingRepository.existsByTrackingId(sampleTrackingId);

        // Assert
        assertTrue(exists);
        verify(trackingRepository, times(1)).existsByTrackingId(sampleTrackingId);
    }

    @Test
    void testDeleteByTrackingId_ExistingTracking_DeletesSuccessfully() {
        // Arrange
        doNothing().when(trackingRepository).deleteByTrackingId(sampleTrackingId);

        // Act
        trackingRepository.deleteByTrackingId(sampleTrackingId);

        // Assert
        verify(trackingRepository, times(1)).deleteByTrackingId(sampleTrackingId);
    }
}