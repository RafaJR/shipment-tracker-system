package com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model;

/**
 * Enum representing the status of a notification.
 */
public enum NotificationStatus {
    PENDING("Notification is pending to be sent"),
    SENT("Notification has been sent successfully"),
    FAILED("Notification failed to send"),
    RETRYING("Notification is being retried after failure");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
