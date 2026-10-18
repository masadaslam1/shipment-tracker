package dev.masadaslam1.shipment_tracker.dto;

import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ShipmentDto(Long id, String trackingNumber, ShipmentStatus status, String origin, String destination,
                          String currentLocation,
                          Instant estimatedDelivery, Instant actualDelivery, BigDecimal weight, String carrier,
                          String description, Instant createdAt, Instant lastUpdated) {
}
