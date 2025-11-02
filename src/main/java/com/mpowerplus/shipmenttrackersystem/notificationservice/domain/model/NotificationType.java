package com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model;

/**
 * Enum representing the type of notification channel.
 */
public enum NotificationType {
    EMAIL("Email notification"),
    SMS("SMS notification"),
    PUSH("Push notification"),
    IN_APP("In-app notification");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
