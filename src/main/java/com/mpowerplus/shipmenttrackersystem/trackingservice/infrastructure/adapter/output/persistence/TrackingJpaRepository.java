package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for TrackingEntity.
 * Provides CRUD operations and custom queries.
 */
@Repository
public interface TrackingJpaRepository extends JpaRepository<TrackingEntity, Long> {

    /**
     * Finds a tracking entity by its tracking ID.
     *
     * @param trackingId the tracking identifier
     * @return optional containing the entity if found
     */
    Optional<TrackingEntity> findByTrackingId(String trackingId);

    /**
     * Checks if a tracking entity exists by tracking ID.
     *
     * @param trackingId the tracking identifier
     * @return true if exists, false otherwise
     */
    boolean existsByTrackingId(String trackingId);

    /**
     * Deletes a tracking entity by tracking ID.
     *
     * @param trackingId the tracking identifier
     */
    void deleteByTrackingId(String trackingId);
}
