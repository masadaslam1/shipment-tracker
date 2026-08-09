package dev.masadaslam1.shipment_tracker.repository;

import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PrePersist;

import java.util.List;

public class CustomShipmentRepositoryImpl implements CustomShipmentRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void bulkUpdateStatus(List<String> trackingNumbers, ShipmentStatus newStatus) {
        em.createQuery(""" 
                UPDATE Shipment s set s.status = :status, s.lastUpdated = CURRENT_TIMESTAMP where s.trackingNumber IN :trackingNumber
                """).setParameter("status", newStatus).setParameter("trackingNumbers", trackingNumbers).executeUpdate();
    }
}
