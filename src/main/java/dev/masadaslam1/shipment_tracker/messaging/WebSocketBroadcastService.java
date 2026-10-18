package dev.masadaslam1.shipment_tracker.messaging;

import dev.masadaslam1.shipment_tracker.domain.Shipment;
import dev.masadaslam1.shipment_tracker.dto.ShipmentDto;
import dev.masadaslam1.shipment_tracker.mapper.ShipmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketBroadcastService {

    public static final String SHIPMENTS_DESTINATION = "/topic/shipments";

    private final SimpMessagingTemplate messagingTemplate;
    private final ShipmentMapper shipmentMapper;

    public void broadcast(Shipment shipment) {
        ShipmentDto dto = shipmentMapper.toDto(shipment);
        messagingTemplate.convertAndSend(SHIPMENTS_DESTINATION, dto);
        log.info("Sent shipment to broadcast: {}", dto);
    }
}