package com.mpowerplus.shipmenttrackersystem.trackingservice.application.service;

/**
 * Exception thrown when a tracking record is not found.
 */
public class TrackingNotFoundException extends RuntimeException {

    public TrackingNotFoundException(String message) {
        super(message);
    }
}
