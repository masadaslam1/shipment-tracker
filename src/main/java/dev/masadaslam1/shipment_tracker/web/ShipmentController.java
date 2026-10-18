package dev.masadaslam1.shipment_tracker.web;

import dev.masadaslam1.shipment_tracker.dto.ShipmentDto;
import dev.masadaslam1.shipment_tracker.mapper.ShipmentMapper;
import dev.masadaslam1.shipment_tracker.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;

    @GetMapping
    public List<ShipmentDto> getShipments() {
        return shipmentMapper.toDtoList(shipmentService.getAllShipments());
    }
}
