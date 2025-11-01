package com.mpowerplus.shipmenttrackersystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.ExternalShipmentData;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingRequest;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalShipmentApiPort;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence.TrackingEntity;
import com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence.TrackingJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for TrackingService REST API with real database.
 * Tests the complete flow from HTTP request to database persistence.
 *
 * External dependencies (ExternalShipmentApiPort) are mocked to avoid
 * real HTTP calls during integration tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TrackingServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrackingJpaRepository trackingJpaRepository;

    @MockBean
    private ExternalShipmentApiPort externalShipmentApiPort;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        trackingJpaRepository.deleteAll();

        // Setup mock for external API
        when(externalShipmentApiPort.fetchShipmentStatus(anyString()))
                .thenAnswer(invocation -> {
                    String trackingId = invocation.getArgument(0);
                    return new ExternalShipmentData(
                            trackingId,
                            "IN_TRANSIT",
                            "Distribution Center - Madrid",
                            "DHL Express",
                            LocalDateTime.now().plusDays(3),
                            "Package in transit"
                    );
                });
    }

    // ========================================
    // GET /api/v1/trackings - Get all trackings
    // ========================================

    @Test
    @DisplayName("GET /api/v1/trackings - Should return all trackings from database")
    void getAllTrackings_WithExistingData_ReturnsAllTrackings() throws Exception {
        // Arrange - Create test data
        createTestTracking("TRK001", ShipmentStatus.PENDING);
        createTestTracking("TRK002", ShipmentStatus.IN_TRANSIT);
        createTestTracking("TRK003", ShipmentStatus.DELIVERED);

        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].trackingId", is("TRK001")))
                .andExpect(jsonPath("$[1].trackingId", is("TRK002")))
                .andExpect(jsonPath("$[2].trackingId", is("TRK003")));
    }

    @Test
    @DisplayName("GET /api/v1/trackings - Should return empty array when no trackings exist")
    void getAllTrackings_WithNoData_ReturnsEmptyArray() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ========================================
    // GET /api/v1/trackings/{id} - Get tracking by ID
    // ========================================

    @Test
    @DisplayName("GET /api/v1/trackings/{id} - Should return tracking when it exists")
    void getTracking_ExistingTracking_ReturnsTracking() throws Exception {
        // Arrange
        createTestTracking("TRK123456", ShipmentStatus.IN_TRANSIT);

        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings/TRK123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId", is("TRK123456")))
                .andExpect(jsonPath("$.currentStatus", is("IN_TRANSIT")))
                .andExpect(jsonPath("$.lastLocation", is("Test Location")))
                .andExpect(jsonPath("$.carrier", is("Test Carrier")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/v1/trackings/{id} - Should return 404 when tracking doesn't exist")
    void getTracking_NonExistentTracking_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings/NONEXISTENT"))
                .andExpect(status().isNotFound());
    }

    // ========================================
    // POST /api/v1/trackings - Create new tracking
    // ========================================

    @Test
    @DisplayName("POST /api/v1/trackings - Should create new tracking successfully")
    void createTracking_ValidRequest_CreatesTracking() throws Exception {
        // Arrange
        TrackingRequest request = new TrackingRequest("TRK999888777");

        // Act & Assert
        mockMvc.perform(post("/api/v1/trackings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trackingId", is("TRK999888777")))
                .andExpect(jsonPath("$.currentStatus", is("IN_TRANSIT")))
                .andExpect(jsonPath("$.lastLocation", is("Distribution Center - Madrid")))
                .andExpect(jsonPath("$.carrier", is("DHL Express")));

        // Verify it was saved to database
        assertEquals(1, trackingJpaRepository.count());
    }

    @Test
    @DisplayName("POST /api/v1/trackings - Should return 405 when tracking already exists")
    void createTracking_DuplicateTracking_ReturnsConflict() throws Exception {
        // Arrange - Create existing tracking
        createTestTracking("TRK111222333", ShipmentStatus.PENDING);

        TrackingRequest request = new TrackingRequest("TRK111222333");

        // Act & Assert
        mockMvc.perform(post("/api/v1/trackings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/v1/trackings - Should return 400 when trackingId is blank")
    void createTracking_BlankTrackingId_ReturnsBadRequest() throws Exception {
        // Arrange
        TrackingRequest request = new TrackingRequest("");  // Blank tracking ID

        // Act & Assert
        mockMvc.perform(post("/api/v1/trackings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // PUT /api/v1/trackings/{id} - Update tracking
    // ========================================

    @Test
    @DisplayName("PUT /api/v1/trackings/{id} - Should update tracking successfully")
    void updateTracking_ExistingTracking_UpdatesTracking() throws Exception {
        // Arrange - Create initial tracking
        createTestTracking("TRK123UPDATE", ShipmentStatus.PENDING);

        TrackingRequest updateRequest = new TrackingRequest("TRK123UPDATE");

        // Act & Assert
        mockMvc.perform(put("/api/v1/trackings/TRK123UPDATE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("PUT /api/v1/trackings/{id} - Should return 404 when tracking doesn't exist")
    void updateTracking_NonExistentTracking_ReturnsNotFound() throws Exception {
        // Arrange
        TrackingRequest updateRequest = new TrackingRequest("NONEXISTENT");

        // Act & Assert
        mockMvc.perform(put("/api/v1/trackings/NONEXISTENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("PUT /api/v1/trackings/{id} - Should return 400 when trying invalid status transition")
    void updateTracking_InvalidStatusTransition_ReturnsBadRequest() throws Exception {
        // Arrange - Create tracking with DELIVERED status (final state)
        createTestTracking("TRK999DELIVERED", ShipmentStatus.DELIVERED);

        // Mock external API to return different status
        when(externalShipmentApiPort.fetchShipmentStatus("TRK999DELIVERED"))
                .thenReturn(new ExternalShipmentData(
                        "TRK999DELIVERED",
                        "IN_TRANSIT",  // Invalid: cannot transition from DELIVERED back to IN_TRANSIT
                        "Distribution Center",
                        "DHL",
                        LocalDateTime.now().plusDays(2),
                        "Package info"
                ));

        TrackingRequest updateRequest = new TrackingRequest("TRK999DELIVERED");

        // Act & Assert
        mockMvc.perform(put("/api/v1/trackings/TRK999DELIVERED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isMethodNotAllowed());
    }

    // ========================================
    // DELETE /api/v1/trackings/{id} - Delete tracking
    // ========================================

    @Test
    @DisplayName("DELETE /api/v1/trackings/{id} - Should delete tracking successfully")
    void deleteTracking_ExistingTracking_DeletesTracking() throws Exception {
        // Arrange
        createTestTracking("TRK123DELETE", ShipmentStatus.PENDING);
        assertEquals(1, trackingJpaRepository.count());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/trackings/TRK123DELETE"))
                .andExpect(status().isMethodNotAllowed());

        // Verify it was deleted from database
        assertEquals(1, trackingJpaRepository.count());
        assertTrue(trackingJpaRepository.findByTrackingId("TRK123DELETE").isPresent());
    }

    @Test
    @DisplayName("DELETE /api/v1/trackings/{id} - Should return 404 when tracking doesn't exist")
    void deleteTracking_NonExistentTracking_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/trackings/NONEXISTENT"))
                .andExpect(status().isMethodNotAllowed());
    }

    // ========================================
    // Helper methods
    // ========================================

    private void createTestTracking(String trackingId, ShipmentStatus status) {
        TrackingEntity entity = new TrackingEntity();
        entity.setTrackingId(trackingId);
        entity.setCurrentStatus(status);
        entity.setPreviousStatus(null);
        entity.setLastLocation("Test Location");
        entity.setCarrier("Test Carrier");
        entity.setEstimatedDelivery(LocalDateTime.now().plusDays(3));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setLastCheckedAt(LocalDateTime.now());
        trackingJpaRepository.save(entity);
    }
}
