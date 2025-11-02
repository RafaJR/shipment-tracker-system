package com.mpowerplus.shipmenttrackersystem.trackingservice.domain.repository;

import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.Tracking;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.TrackingId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Tracking domain aggregates.
 * Defines operations to query, persist, and delete Tracking entities.
 */
public interface TrackingRepository {

    /**
     * Finds a tracking by its tracking ID
     *
     * @param trackingId the tracking identifier
     * @return Optional containing the tracking if found
     */
    Optional<Tracking> findByTrackingId(TrackingId trackingId);

    /**
     * Saves a tracking (create or update)
     *
     * @param tracking the tracking aggregate to save
     * @return the saved tracking
     */
    Tracking save(Tracking tracking);

    /**
     * Finds all trackings
     *
     * @return list of all trackings
     */
    List<Tracking> findAll();

    /**
     * Checks if a tracking exists by tracking ID
     *
     * @param trackingId the tracking identifier
     * @return true if exists, false otherwise
     */
    boolean existsByTrackingId(TrackingId trackingId);

    /**
     * Deletes a tracking by its tracking ID
     *
     * @param trackingId the tracking identifier
     */
    void deleteByTrackingId(TrackingId trackingId);
}
