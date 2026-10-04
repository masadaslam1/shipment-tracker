package dev.masadaslam1.shipment_tracker.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Wire contract for shipment events published to Kafka.
 * Plain data snapshot: producers may add fields over time,
 * consumers must ignore what they don't know yet.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ShipmentEvent(String trackingNumber, ShipmentStatus shipmentStatus, String origin, String destination, String currentLocation,
                            Instant estimatedDelivery, Instant actualDelivery, BigDecimal weight, String carrier, String description) {
}