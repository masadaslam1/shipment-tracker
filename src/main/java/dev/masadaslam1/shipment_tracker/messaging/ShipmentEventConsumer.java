package dev.masadaslam1.shipment_tracker.messaging;

import dev.masadaslam1.shipment_tracker.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventConsumer {

    public static final String SHIPMENT_EVENTS_TOPIC = "shipment-events";

    private final ObjectMapper objectMapper;
    private final ShipmentService service;

    @KafkaListener(topics = SHIPMENT_EVENTS_TOPIC)
    public void onShipmentEvent(String payload) {
        try {
            ShipmentEvent event = objectMapper.readValue(payload, ShipmentEvent.class);
            service.upsert(event);
        } catch (JacksonException e) {
            log.error("Skipping malformed shipment event: {}", payload, e);
        }
    }
}
