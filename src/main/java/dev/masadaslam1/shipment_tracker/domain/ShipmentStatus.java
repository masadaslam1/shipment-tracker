package dev.masadaslam1.shipment_tracker.domain;

/**
 * Lifecycle of a shipment through the tracking system.
 * The simulator cycles through these states, publishes to Kafka,
 * and the consumer updates the database + pushes to the UI via WebSocket.
 */
public enum ShipmentStatus {
    CREATED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION
}