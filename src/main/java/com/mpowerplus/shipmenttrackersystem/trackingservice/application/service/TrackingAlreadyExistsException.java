package com.mpowerplus.shipmenttrackersystem.trackingservice.application.service;

/**
 * Exception thrown when attempting to create a tracking that already exists.
 */
public class TrackingAlreadyExistsException extends RuntimeException {

    public TrackingAlreadyExistsException(String message) {
        super(message);
    }
}
