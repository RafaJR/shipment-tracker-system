package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.Tracking;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.TrackingId;
import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.repository.TrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing TrackingRepository interface.
 * Bridges between domain layer and infrastructure (JPA).
 * <p>
 * Following Hexagonal Architecture:
 * - Implements domain repository interface
 * - Delegates to Spring Data JPA repository
 * - Maps between domain objects (Tracking) and persistence entities (TrackingEntity)
 */
@Component
@RequiredArgsConstructor
public class TrackingRepositoryAdapter implements TrackingRepository {

    private final TrackingJpaRepository jpaRepository;
    private final TrackingMapper mapper;

    @Override
    public Optional<Tracking> findByTrackingId(TrackingId trackingId) {
        return jpaRepository.findByTrackingId(trackingId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Tracking save(Tracking tracking) {
        TrackingEntity entity = mapper.toEntity(tracking);
        TrackingEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Tracking> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByTrackingId(TrackingId trackingId) {
        return jpaRepository.existsByTrackingId(trackingId.getValue());
    }

    @Override
    public void deleteByTrackingId(TrackingId trackingId) {
        jpaRepository.deleteByTrackingId(trackingId.getValue());
    }
}
