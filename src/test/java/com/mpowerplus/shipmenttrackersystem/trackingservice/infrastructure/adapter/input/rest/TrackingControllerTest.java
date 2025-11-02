package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingRequest;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingResponse;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.input.TrackingUseCase;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.service.TrackingAlreadyExistsException;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.service.TrackingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for TrackingController using standalone MockMvc setup.
 */
@ExtendWith(MockitoExtension.class)
class TrackingControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TrackingUseCase trackingUseCase;

    @InjectMocks
    private TrackingController trackingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trackingController)
                .setControllerAdvice(trackingController) // Register exception handlers
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void checkTrackingStatus_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        TrackingRequest request = new TrackingRequest("TRACK123");
        TrackingResponse response = new TrackingResponse(
                "TRACK123",
                "IN_TRANSIT",
                "PENDING",
                "Madrid",
                "DHL",
                LocalDateTime.of(2025, 11, 5, 14, 0),
                LocalDateTime.of(2025, 11, 1, 10, 0),
                LocalDateTime.of(2025, 11, 1, 12, 0),
                LocalDateTime.of(2025, 11, 1, 10, 0),
                false,
                "In Transit",
                "Shipment is currently being transported"
        );

        when(trackingUseCase.checkTrackingStatus(any(TrackingRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/trackings/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId", is("TRACK123")))
                .andExpect(jsonPath("$.currentStatus", is("IN_TRANSIT")))
                .andExpect(jsonPath("$.lastLocation", is("Madrid")))
                .andExpect(jsonPath("$.carrier", is("DHL")));
    }

    @Test
    void checkTrackingStatus_InvalidTrackingId_ReturnsBadRequest() throws Exception {
        // Arrange
        TrackingRequest request = new TrackingRequest("INVALID#ID");

        // Act & Assert - Validation happens at controller level
        mockMvc.perform(post("/api/v1/trackings/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTracking_ExistingTracking_ReturnsOk() throws Exception {
        // Arrange
        TrackingResponse response = new TrackingResponse(
                "TRACK456",
                "DELIVERED",
                "OUT_FOR_DELIVERY",
                "Barcelona",
                "FedEx",
                LocalDateTime.of(2025, 10, 30, 14, 0),
                LocalDateTime.of(2025, 10, 25, 10, 0),
                LocalDateTime.of(2025, 11, 1, 12, 0),
                LocalDateTime.of(2025, 11, 1, 12, 0),
                false,
                "Delivered",
                "Package has been delivered successfully"
        );

        when(trackingUseCase.getTracking("TRACK456")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings/TRACK456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingId", is("TRACK456")))
                .andExpect(jsonPath("$.currentStatus", is("DELIVERED")))
                .andExpect(jsonPath("$.lastLocation", is("Barcelona")));
    }

    @Test
    void getTracking_NonExistentTracking_ReturnsNotFound() throws Exception {
        // Arrange
        when(trackingUseCase.getTracking("NONEXISTENT"))
                .thenThrow(new TrackingNotFoundException("Tracking not found: NONEXISTENT"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings/NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Tracking Not Found")))
                .andExpect(jsonPath("$.message", containsString("NONEXISTENT")));
    }

    @Test
    void getAllTrackings_ReturnsListOfTrackings() throws Exception {
        // Arrange
        TrackingResponse response1 = new TrackingResponse(
                "TRACK111",
                "PENDING",
                null,
                "Valencia",
                "UPS",
                LocalDateTime.of(2025, 11, 4, 14, 0),
                LocalDateTime.of(2025, 11, 1, 10, 0),
                LocalDateTime.of(2025, 11, 1, 10, 0),
                null,
                false,
                "Pending",
                "Shipment is awaiting pickup"
        );

        TrackingResponse response2 = new TrackingResponse(
                "TRACK222",
                "IN_TRANSIT",
                "PENDING",
                "Sevilla",
                "DHL",
                LocalDateTime.of(2025, 11, 2, 14, 0),
                LocalDateTime.of(2025, 10, 31, 10, 0),
                LocalDateTime.of(2025, 11, 1, 12, 0),
                LocalDateTime.of(2025, 11, 1, 9, 0),
                false,
                "In Transit",
                "Shipment is on its way"
        );

        when(trackingUseCase.getAllTrackings()).thenReturn(List.of(response1, response2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].trackingId", is("TRACK111")))
                .andExpect(jsonPath("$[1].trackingId", is("TRACK222")));
    }

    @Test
    void createTracking_ValidRequest_ReturnsCreated() throws Exception {
        // Arrange
        TrackingRequest request = new TrackingRequest("TRACK789");
        TrackingResponse response = new TrackingResponse(
                "TRACK789",
                "PENDING",
                null,
                "Madrid",
                "DHL",
                LocalDateTime.of(2025, 11, 4, 14, 0),
                LocalDateTime.of(2025, 11, 1, 10, 0),
                LocalDateTime.of(2025, 11, 1, 10, 0),
                null,
                false,
                "Pending",
                "Shipment is awaiting pickup"
        );

        when(trackingUseCase.createTracking(any(TrackingRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/trackings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trackingId", is("TRACK789")))
                .andExpect(jsonPath("$.currentStatus", is("PENDING")));
    }

    @Test
    void createTracking_AlreadyExists_ReturnsInternalServerError() throws Exception {
        // Arrange
        TrackingRequest request = new TrackingRequest("TRACK999");

        when(trackingUseCase.createTracking(any(TrackingRequest.class)))
                .thenThrow(new TrackingAlreadyExistsException("Tracking already exists: TRACK999"));

        // Act & Assert - Generic exception handler catches it as 500
        mockMvc.perform(post("/api/v1/trackings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)));
    }

    @Test
    void checkTrackingStatus_EmptyTrackingId_ReturnsBadRequest() throws Exception {
        // Arrange
        TrackingRequest request = new TrackingRequest("");

        // Act & Assert - Validation fails
        mockMvc.perform(post("/api/v1/trackings/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTracking_ServiceThrowsIllegalArgument_ReturnsBadRequest() throws Exception {
        // Arrange
        when(trackingUseCase.getTracking("INVALIDID"))
                .thenThrow(new IllegalArgumentException("Invalid tracking ID format"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings/INVALIDID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Invalid Request")))
                .andExpect(jsonPath("$.message", containsString("Invalid tracking ID format")));
    }

    @Test
    void createTracking_GenericException_ReturnsInternalServerError() throws Exception {
        // Arrange
        TrackingRequest request = new TrackingRequest("TRACK888");

        when(trackingUseCase.createTracking(any(TrackingRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/trackings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")));
    }
}
