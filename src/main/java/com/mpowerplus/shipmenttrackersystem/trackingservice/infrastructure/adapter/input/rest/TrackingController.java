package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.input.rest;

import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingRequest;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.dto.TrackingResponse;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.input.TrackingUseCase;
import com.mpowerplus.shipmenttrackersystem.trackingservice.application.service.TrackingNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for shipment tracking operations.
 * Input adapter in hexagonal architecture.
 */
@RestController
@RequestMapping("/api/v1/trackings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tracking API", description = "Endpoints for managing shipment tracking")
public class TrackingController {

    private final TrackingUseCase trackingUseCase;

    @PostMapping("/check")
    @Operation(
            summary = "Check tracking status",
            description = "Fetches current status from external API and updates tracking record. Creates new tracking if not exists."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status checked successfully",
                    content = @Content(schema = @Schema(implementation = TrackingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid tracking ID format"),
            @ApiResponse(responseCode = "500", description = "External API error or internal server error")
    })
    public ResponseEntity<TrackingResponse> checkTrackingStatus(
            @Valid @RequestBody TrackingRequest request) {
        log.info("REST: Check tracking status request received for: {}", request.trackingId());
        TrackingResponse response = trackingUseCase.checkTrackingStatus(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{trackingId}")
    @Operation(
            summary = "Get tracking by ID",
            description = "Retrieves tracking information from database without calling external API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tracking found",
                    content = @Content(schema = @Schema(implementation = TrackingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tracking not found"),
            @ApiResponse(responseCode = "400", description = "Invalid tracking ID format")
    })
    public ResponseEntity<TrackingResponse> getTracking(
            @Parameter(description = "Tracking ID", example = "TRACK123")
            @PathVariable String trackingId) {
        log.info("REST: Get tracking request received for: {}", trackingId);
        TrackingResponse response = trackingUseCase.getTracking(trackingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Get all trackings",
            description = "Retrieves all tracking records from database"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trackings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TrackingResponse.class)))
    })
    public ResponseEntity<List<TrackingResponse>> getAllTrackings() {
        log.info("REST: Get all trackings request received");
        List<TrackingResponse> responses = trackingUseCase.getAllTrackings();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(
            summary = "Create new tracking",
            description = "Creates a new tracking record by fetching initial data from external API"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tracking created successfully",
                    content = @Content(schema = @Schema(implementation = TrackingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid tracking ID format"),
            @ApiResponse(responseCode = "409", description = "Tracking already exists"),
            @ApiResponse(responseCode = "500", description = "External API error or internal server error")
    })
    public ResponseEntity<TrackingResponse> createTracking(
            @Valid @RequestBody TrackingRequest request) {
        log.info("REST: Create tracking request received for: {}", request.trackingId());
        TrackingResponse response = trackingUseCase.createTracking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(TrackingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrackingNotFound(TrackingNotFoundException ex) {
        log.warn("Tracking not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Tracking Not Found",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error response DTO for REST API error handling
     */
    public record ErrorResponse(
            int status,
            String error,
            String message
    ) {}
}
