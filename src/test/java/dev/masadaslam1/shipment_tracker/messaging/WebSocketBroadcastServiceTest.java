package dev.masadaslam1.shipment_tracker.messaging;

import dev.masadaslam1.shipment_tracker.domain.Shipment;
import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketBroadcastServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketBroadcastService broadcastService;

    @Test
    void broadcastSendsShipmentToTopic() {
        Shipment shipment = Shipment.builder()
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        broadcastService.broadcast(shipment);

        verify(messagingTemplate)
                .convertAndSend(WebSocketBroadcastService.SHIPMENTS_DESTINATION, shipment);
    }
}
