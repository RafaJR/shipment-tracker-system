package com.mpowerplus.shipmenttrackersystem.trackingservice.application.service;

import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.ExternalShipmentData;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingRequest;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingResponse;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.EventPublisherPort;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalShipmentApiPort;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.Tracking;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.TrackingId;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.repository.TrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TrackingApplicationService.
 * Tests the application service logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class TrackingApplicationServiceTest {

    @Mock
    private TrackingRepository trackingRepository;

    @Mock
    private ExternalShipmentApiPort externalApiPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private TrackingApplicationService trackingApplicationService;

    private TrackingRequest validRequest;
    private ExternalShipmentData externalData;
    private Tracking sampleTracking;

    @BeforeEach
    void setUp() {
        validRequest = new TrackingRequest("TRK001234567890");

        externalData = new ExternalShipmentData(
                "TRK001234567890",
                "IN_TRANSIT",
                "Madrid Distribution Center",
                "DHL Express",
                LocalDateTime.now().plusDays(2),
                null
        );

        sampleTracking = Tracking.create(
                TrackingId.of("TRK001234567890"),
                ShipmentStatus.IN_TRANSIT,
                "Madrid Distribution Center",
                "DHL Express",
                LocalDateTime.now().plusDays(2)
        );
    }

    @Test
    void checkTrackingStatus_NewTracking_CreatesAndReturnsTracking() {
        // Arrange
        when(externalApiPort.fetchShipmentStatus("TRK001234567890")).thenReturn(externalData);
        when(trackingRepository.findByTrackingId(any(TrackingId.class))).thenReturn(Optional.empty());
        when(trackingRepository.save(any(Tracking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TrackingResponse response = trackingApplicationService.checkTrackingStatus(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("TRK001234567890", response.trackingId());
        assertEquals("IN_TRANSIT", response.currentStatus());
        assertNull(response.previousStatus());
        assertEquals("Madrid Distribution Center", response.lastLocation());
        verify(trackingRepository, times(1)).save(any(Tracking.class));
        verify(eventPublisherPort, never()).publishStatusChangeEvent(any());
    }

    @Test
    void checkTrackingStatus_ExistingTrackingWithStatusChange_PublishesEvent() {
        // Arrange
        Tracking existingTracking = Tracking.create(
                TrackingId.of("TRK001234567890"),
                ShipmentStatus.PENDING,
                "Warehouse",
                "DHL Express",
                LocalDateTime.now().plusDays(2)
        );

        ExternalShipmentData changedData = new ExternalShipmentData(
                "TRK001234567890",
                "IN_TRANSIT",
                "Madrid Distribution Center",
                "DHL Express",
                LocalDateTime.now().plusDays(2),
                null
        );

        when(externalApiPort.fetchShipmentStatus("TRK001234567890")).thenReturn(changedData);
        when(trackingRepository.findByTrackingId(any(TrackingId.class))).thenReturn(Optional.of(existingTracking));
        when(trackingRepository.save(any(Tracking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TrackingResponse response = trackingApplicationService.checkTrackingStatus(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("IN_TRANSIT", response.currentStatus());
        assertEquals("PENDING", response.previousStatus());
        verify(eventPublisherPort, times(1)).publishStatusChangeEvent(any());
    }

    @Test
    void checkTrackingStatus_ExistingTrackingNoChange_DoesNotPublishEvent() {
        // Arrange
        when(externalApiPort.fetchShipmentStatus("TRK001234567890")).thenReturn(externalData);
        when(trackingRepository.findByTrackingId(any(TrackingId.class))).thenReturn(Optional.of(sampleTracking));
        when(trackingRepository.save(any(Tracking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TrackingResponse response = trackingApplicationService.checkTrackingStatus(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("IN_TRANSIT", response.currentStatus());
        verify(eventPublisherPort, never()).publishStatusChangeEvent(any());
    }

    @Test
    void getTracking_ExistingTracking_ReturnsTracking() {
        // Arrange
        when(trackingRepository.findByTrackingId(any(TrackingId.class)))
                .thenReturn(Optional.of(sampleTracking));

        // Act
        TrackingResponse response = trackingApplicationService.getTracking("TRK001234567890");

        // Assert
        assertNotNull(response);
        assertEquals("TRK001234567890", response.trackingId());
        assertEquals("IN_TRANSIT", response.currentStatus());
    }

    @Test
    void getTracking_NonExistingTracking_ThrowsException() {
        // Arrange
        when(trackingRepository.findByTrackingId(any(TrackingId.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TrackingNotFoundException.class, () ->
                trackingApplicationService.getTracking("TRK999999999999")
        );
    }

    @Test
    void getAllTrackings_ReturnsAllTrackings() {
        // Arrange
        Tracking tracking1 = Tracking.create(
                TrackingId.of("TRK111"),
                ShipmentStatus.IN_TRANSIT,
                "Location1",
                "Carrier1",
                LocalDateTime.now().plusDays(1)
        );
        Tracking tracking2 = Tracking.create(
                TrackingId.of("TRK222"),
                ShipmentStatus.DELIVERED,
                "Location2",
                "Carrier2",
                LocalDateTime.now()
        );

        when(trackingRepository.findAll()).thenReturn(List.of(tracking1, tracking2));

        // Act
        List<TrackingResponse> responses = trackingApplicationService.getAllTrackings();

        // Assert
        assertEquals(2, responses.size());
        assertEquals("TRK111", responses.get(0).trackingId());
        assertEquals("TRK222", responses.get(1).trackingId());
    }

    @Test
    void createTracking_NewTracking_CreatesSuccessfully() {
        // Arrange
        when(trackingRepository.existsByTrackingId(any(TrackingId.class))).thenReturn(false);
        when(externalApiPort.fetchShipmentStatus("TRK001234567890")).thenReturn(externalData);
        when(trackingRepository.save(any(Tracking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TrackingResponse response = trackingApplicationService.createTracking(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("TRK001234567890", response.trackingId());
        verify(trackingRepository, times(1)).save(any(Tracking.class));
    }

    @Test
    void createTracking_ExistingTracking_ThrowsException() {
        // Arrange
        when(trackingRepository.existsByTrackingId(any(TrackingId.class))).thenReturn(true);

        // Act & Assert
        assertThrows(TrackingAlreadyExistsException.class, () ->
                trackingApplicationService.createTracking(validRequest)
        );
        verify(trackingRepository, never()).save(any(Tracking.class));
    }

    @Test
    void checkTrackingStatus_InvalidTrackingId_ThrowsException() {
        // Arrange
        TrackingRequest invalidRequest = new TrackingRequest("INVALID#ID");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                trackingApplicationService.checkTrackingStatus(invalidRequest)
        );
    }

    @Test
    void checkTrackingStatus_ExternalApiFailure_ThrowsExternalApiException() {
        // Arrange
        when(externalApiPort.fetchShipmentStatus("TRK001234567890"))
                .thenThrow(new com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalApiException(
                        "External API is unavailable"
                ));

        // Act & Assert
        com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalApiException exception =
                assertThrows(com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalApiException.class, () ->
                        trackingApplicationService.checkTrackingStatus(validRequest)
                );
        assertTrue(exception.getMessage().contains("External API is unavailable"));
    }

    @Test
    void createTracking_ExternalApiFailureWithCause_ThrowsExternalApiException() {
        // Arrange
        when(trackingRepository.existsByTrackingId(any(TrackingId.class))).thenReturn(false);
        RuntimeException cause = new RuntimeException("Network timeout");
        when(externalApiPort.fetchShipmentStatus("TRK001234567890"))
                .thenThrow(new com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalApiException(
                        "Failed to fetch shipment data", cause
                ));

        // Act & Assert
        com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalApiException exception =
                assertThrows(com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalApiException.class, () ->
                        trackingApplicationService.createTracking(validRequest)
                );
        assertTrue(exception.getMessage().contains("Failed to fetch shipment data"));
        assertEquals(cause, exception.getCause());
    }
}
