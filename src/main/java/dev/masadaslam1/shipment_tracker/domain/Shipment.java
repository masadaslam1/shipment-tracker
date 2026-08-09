package dev.masadaslam1.shipment_tracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private ShipmentStatus status;

    @NotBlank
    @Column(nullable = false)
    private String origin;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String destination;


    private String currentLocation;

    @NotNull
    @Column(nullable = false)
    private Instant estimatedDelivery;

    private Instant actualDelivery;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastUpdated;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    private String carrier;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.lastUpdated == null) {
            this.lastUpdated = this.createdAt;
        }
    }
}
