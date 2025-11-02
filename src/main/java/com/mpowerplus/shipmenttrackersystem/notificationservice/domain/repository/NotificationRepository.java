package com.mpowerplus.shipmenttrackersystem.notificationservice.domain.repository;

import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.Notification;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Notification aggregate.
 * Following DDD repository pattern.
 */
public interface NotificationRepository {

    /**
     * Saves a notification.
     */
    Notification save(Notification notification);

    /**
     * Finds a notification by ID.
     */
    Optional<Notification> findById(Long id);

    /**
     * Finds all notifications for a specific tracking ID.
     */
    List<Notification> findByTrackingId(String trackingId);

    /**
     * Finds notifications by status.
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Finds all notifications.
     */
    List<Notification> findAll();
}
