package dev.masadaslam1.shipment_tracker.messaging;

import dev.masadaslam1.shipment_tracker.domain.Shipment;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketBroadcastService {

    public static final String SHIPMENTS_DESTINATION = "/topic/shipments";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(Shipment shipment) {
        messagingTemplate.convertAndSend(SHIPMENTS_DESTINATION, shipment);
    }
}