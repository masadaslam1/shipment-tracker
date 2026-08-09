package dev.masadaslam1.shipment_tracker.repository;

import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;

import java.util.List;

public interface CustomShipmentRepository {
    void bulkUpdateStatus(List<String> trackingNumbers, ShipmentStatus newStatus);
}
