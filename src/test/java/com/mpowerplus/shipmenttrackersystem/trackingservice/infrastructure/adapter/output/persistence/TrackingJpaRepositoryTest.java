package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TrackingJpaRepository.
 * Uses @DataJpaTest to test JPA operations with an in-memory database.
 */
@DataJpaTest
class TrackingJpaRepositoryTest {

    @Autowired
    private TrackingJpaRepository trackingJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByTrackingId_ExistingEntity_ReturnsEntity() {
        // Arrange
        TrackingEntity entity = TrackingEntity.builder()
                .trackingId("TRACK123")
                .currentStatus(ShipmentStatus.IN_TRANSIT)
                .previousStatus(ShipmentStatus.PENDING)
                .lastLocation("Warehouse A")
                .carrier("DHL Express")
                .estimatedDelivery(LocalDateTime.now().plusDays(3))
                .build();
        entityManager.persistAndFlush(entity);

        // Act
        Optional<TrackingEntity> result = trackingJpaRepository.findByTrackingId("TRACK123");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTrackingId()).isEqualTo("TRACK123");
        assertThat(result.get().getCurrentStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
        assertThat(result.get().getPreviousStatus()).isEqualTo(ShipmentStatus.PENDING);
        assertThat(result.get().getLastLocation()).isEqualTo("Warehouse A");
    }

    @Test
    void findByTrackingId_NonExistingEntity_ReturnsEmpty() {
        // Act
        Optional<TrackingEntity> result = trackingJpaRepository.findByTrackingId("NON_EXISTENT");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void existsByTrackingId_ExistingEntity_ReturnsTrue() {
        // Arrange
        TrackingEntity entity = TrackingEntity.builder()
                .trackingId("TRACK456")
                .currentStatus(ShipmentStatus.DELIVERED)
                .lastLocation("Customer Address")
                .carrier("FedEx")
                .build();
        entityManager.persistAndFlush(entity);

        // Act
        boolean exists = trackingJpaRepository.existsByTrackingId("TRACK456");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTrackingId_NonExistingEntity_ReturnsFalse() {
        // Act
        boolean exists = trackingJpaRepository.existsByTrackingId("NON_EXISTENT");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    void deleteByTrackingId_ExistingEntity_DeletesEntity() {
        // Arrange
        TrackingEntity entity = TrackingEntity.builder()
                .trackingId("TRACK789")
                .currentStatus(ShipmentStatus.CANCELLED)
                .lastLocation("Origin")
                .carrier("UPS")
                .build();
        entityManager.persistAndFlush(entity);

        // Act
        trackingJpaRepository.deleteByTrackingId("TRACK789");
        entityManager.flush();

        // Assert
        Optional<TrackingEntity> result = trackingJpaRepository.findByTrackingId("TRACK789");
        assertThat(result).isEmpty();
    }

    @Test
    void save_ValidEntity_PersistsWithGeneratedId() {
        // Arrange
        TrackingEntity entity = TrackingEntity.builder()
                .trackingId("TRACK999")
                .currentStatus(ShipmentStatus.OUT_FOR_DELIVERY)
                .lastLocation("Local Hub")
                .carrier("Correos")
                .estimatedDelivery(LocalDateTime.now().plusHours(2))
                .build();

        // Act
        TrackingEntity saved = trackingJpaRepository.save(entity);
        entityManager.flush();

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTrackingId()).isEqualTo("TRACK999");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByTrackingId_CaseInsensitive_ShouldBeCaseSensitive() {
        // Arrange
        TrackingEntity entity = TrackingEntity.builder()
                .trackingId("TraCk123")
                .currentStatus(ShipmentStatus.PENDING)
                .lastLocation("Warehouse")
                .carrier("MRW")
                .build();
        entityManager.persistAndFlush(entity);

        // Act
        Optional<TrackingEntity> upperCase = trackingJpaRepository.findByTrackingId("TRACK123");
        Optional<TrackingEntity> correctCase = trackingJpaRepository.findByTrackingId("TraCk123");

        // Assert
        assertThat(upperCase).isEmpty(); // Case sensitive
        assertThat(correctCase).isPresent();
    }
}