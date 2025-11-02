package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model;

/**
 * Value Object representing the possible states of a shipment.
 * Represents the lifecycle of a package from acceptance to final delivery.
 */
public enum ShipmentStatus {
    PENDING("Pending", "Shipment has been created but not yet picked up"),
    IN_TRANSIT("In Transit", "Shipment is currently being transported"),
    OUT_FOR_DELIVERY("Out for Delivery", "Shipment is on the delivery vehicle"),
    DELIVERED("Delivered", "Shipment has been successfully delivered"),
    FAILED_DELIVERY("Failed Delivery", "Delivery attempt was unsuccessful"),
    RETURNED("Returned", "Shipment is being returned to sender"),
    CANCELLED("Cancelled", "Shipment has been cancelled");

    private final String displayName;
    private final String description;

    ShipmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines if this status change is considered significant enough to trigger an event.
     * @param newStatus the new status to compare with
     * @return true if the change should trigger a notification event
     */
    public boolean isSignificantChangeTo(ShipmentStatus newStatus) {
        return this != newStatus && newStatus != null;
    }
}
