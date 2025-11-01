package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.external;

import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.ExternalShipmentData;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output.ExternalApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for ExternalShipmentApiAdapter using MockWebServer.
 */
class ExternalShipmentApiAdapterTest {

    private MockWebServer mockWebServer;
    private ExternalShipmentApiAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        adapter = new ExternalShipmentApiAdapter(
                WebClient.builder(),
                baseUrl,
                5000,  // timeout
                3,     // max retry attempts
                100    // backoff delay
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void fetchShipmentStatus_SuccessfulResponse_ReturnsExternalShipmentData() throws InterruptedException {
        // Arrange
        String trackingId = "TRACK123";
        String responseBody = """
                {
                    "trackingId": "TRACK123",
                    "status": "IN_TRANSIT",
                    "location": "Madrid",
                    "estimatedDelivery": "2024-01-20T10:00:00",
                    "carrier": "DHL"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        // Act
        ExternalShipmentData result = adapter.fetchShipmentStatus(trackingId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.trackingId()).isEqualTo("TRACK123");
        assertThat(result.status()).isEqualTo("IN_TRANSIT");
        assertThat(result.location()).isEqualTo("Madrid");
        assertThat(result.carrier()).isEqualTo("DHL");

        // Verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/shipments/TRACK123");
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    void fetchShipmentStatus_404NotFound_ThrowsExternalApiException() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        // Act & Assert
        ExternalApiException exception = assertThrows(ExternalApiException.class, () ->
                adapter.fetchShipmentStatus("NONEXISTENT")
        );

        assertThat(exception.getMessage()).contains("404");
        assertThat(exception.getMessage()).contains("NONEXISTENT");
        assertThat(exception.getCause()).isNotNull();
    }

    @Test
    void fetchShipmentStatus_500ServerError_RetriesAndThrowsException() {
        // Arrange - Mock 3 consecutive server errors (max retry attempts)
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));

        // Act & Assert
        ExternalApiException exception = assertThrows(ExternalApiException.class, () ->
                adapter.fetchShipmentStatus("TRACK456")
        );

        assertThat(exception.getMessage()).contains("Failed to fetch shipment status");
        assertThat(exception.getMessage()).contains("TRACK456");

        // Verify that retries occurred (1 initial + 3 retries = 4 total requests)
        assertThat(mockWebServer.getRequestCount()).isEqualTo(4);
    }

    @Test
    void fetchShipmentStatus_500ThenSuccess_RetriesAndSucceeds() throws InterruptedException {
        // Arrange - First call fails with 500, second succeeds
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("""
                        {
                            "trackingId": "TRACK789",
                            "status": "DELIVERED",
                            "location": "Barcelona",
                            "estimatedDelivery": "2024-01-20T10:00:00",
                            "carrier": "FedEx"
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        // Act
        ExternalShipmentData result = adapter.fetchShipmentStatus("TRACK789");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.trackingId()).isEqualTo("TRACK789");
        assertThat(result.status()).isEqualTo("DELIVERED");
        assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void fetchShipmentStatus_NullResponse_ThrowsExternalApiException() {
        // Arrange - Return empty response body
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        ExternalApiException exception = assertThrows(ExternalApiException.class, () ->
                adapter.fetchShipmentStatus("TRACK999")
        );

        assertThat(exception.getMessage()).contains("Failed to fetch shipment status for tracking ID: TRACK999");
        assertThat(exception.getMessage()).contains("TRACK999");
    }

    @Test
    void fetchShipmentStatus_RequestHeaders_ContainsCorrectHeaders() throws InterruptedException {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("""
                        {
                            "trackingId": "TRACK111",
                            "status": "PENDING",
                            "location": "Valencia",
                            "estimatedDelivery": "2024-01-20T10:00:00",
                            "carrier": "UPS"
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        // Act
        adapter.fetchShipmentStatus("TRACK111");

        // Assert
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    }
}
