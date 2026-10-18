package dev.masadaslam1.shipment_tracker.messaging;

import dev.masadaslam1.shipment_tracker.domain.Shipment;
import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;
import dev.masadaslam1.shipment_tracker.mapper.ShipmentMapper;
import dev.masadaslam1.shipment_tracker.mapper.ShipmentMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketBroadcastServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private final ShipmentMapper mapper = new ShipmentMapperImpl();

    private WebSocketBroadcastService broadcastService;

    @BeforeEach
    void setUp() {
        broadcastService = new WebSocketBroadcastService(messagingTemplate, mapper);
    }

    @Test
    void broadcastSendsShipmentDtoToTopic() {
        Shipment shipment = Shipment.builder()
                .trackingNumber("TRACK-123")
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        broadcastService.broadcast(shipment);

        verify(messagingTemplate)
                .convertAndSend(WebSocketBroadcastService.SHIPMENTS_DESTINATION, mapper.toDto(shipment));
    }
}
