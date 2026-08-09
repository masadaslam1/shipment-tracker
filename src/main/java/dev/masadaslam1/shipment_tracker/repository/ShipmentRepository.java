package dev.masadaslam1.shipment_tracker.repository;

import dev.masadaslam1.shipment_tracker.domain.Shipment;
import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long>, CustomShipmentRepository {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findAllByOrderByCreatedAtDesc();

}
