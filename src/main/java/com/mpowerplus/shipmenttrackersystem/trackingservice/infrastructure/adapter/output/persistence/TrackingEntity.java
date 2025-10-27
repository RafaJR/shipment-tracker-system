package com.mpowerplus.shipmenttrackersystem.trackingservice.infrastructure.adapter.output.persistence;

import com.mpowerplus.shipmenttrackersystem.trackingservice.domain.model.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity for persisting tracking information.
 * Infrastructure layer entity that maps to the database table.
 */
@Entity
@Table(name = "trackings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_id", nullable = false, unique = true, length = 50)
    private String trackingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private ShipmentStatus currentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private ShipmentStatus previousStatus;

    @Column(name = "last_location", length = 200)
    private String lastLocation;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;
}
