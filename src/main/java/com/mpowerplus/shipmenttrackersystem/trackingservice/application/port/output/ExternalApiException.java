package com.mpowerplus.shipmenttrackersystem.trackingservice.application.port.output;

/**
 * Exception thrown when external API communication fails.
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
