package com.mpowerplus.shipmenttrackersystem.notificationservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for NotificationEntity.
 */
@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByTrackingId(String trackingId);

    List<NotificationEntity> findByStatus(NotificationStatus status);
}
