package dev.masadaslam1.shipment_tracker.service;

import dev.masadaslam1.shipment_tracker.domain.Shipment;
import dev.masadaslam1.shipment_tracker.messaging.ShipmentEvent;
import dev.masadaslam1.shipment_tracker.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    @Transactional
    public void upsert(ShipmentEvent event) {
        Optional<Shipment> existing = shipmentRepository.findByTrackingNumber(event.trackingNumber());

        if (existing.isPresent()) {
            update(existing.get(), event);
        } else {
            create(event);
        }
    }

    private void update(Shipment shipment, ShipmentEvent event) {
        shipment.setStatus(event.shipmentStatus());
        shipment.setOrigin(event.origin());
        shipment.setDestination(event.destination());
        shipment.setCurrentLocation(event.currentLocation());
        shipment.setEstimatedDelivery(event.estimatedDelivery());
        shipment.setActualDelivery(event.actualDelivery());
        shipment.setWeight(event.weight());
        shipment.setCarrier(event.carrier());
        shipment.setDescription(event.description());
        shipment.setLastUpdated(Instant.now()); // @PrePersist only runs on insert

        log.info("Updated shipment {}", event.trackingNumber());
    }

    private void create(ShipmentEvent event) {
        Shipment shipment = Shipment.builder()
                .trackingNumber(event.trackingNumber())
                .status(event.shipmentStatus())
                .origin(event.origin())
                .destination(event.destination())
                .currentLocation(event.currentLocation())
                .estimatedDelivery(event.estimatedDelivery())
                .actualDelivery(event.actualDelivery())
                .weight(event.weight())
                .carrier(event.carrier())
                .description(event.description())
                .build();

        shipmentRepository.save(shipment); // createdAt and lastUpdated filled by @PrePersist

        log.info("Created shipment {}", event.trackingNumber());
    }
}
