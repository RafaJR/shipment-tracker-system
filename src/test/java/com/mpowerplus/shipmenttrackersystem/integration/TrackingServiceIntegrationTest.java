package com.mpowerplus.shipmenttrackersystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingRequest;
import com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence.TrackingJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for TrackingService REST API with real database.
 * Tests the complete flow from HTTP request to database persistence.
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

    @BeforeEach
    void setUp() {
        // Clean database before each test
        trackingJpaRepository.deleteAll();
    }

    @Test
    void getTracking_NonExistentTracking_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/trackings/NON-EXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Tracking Not Found"));
    }
}
