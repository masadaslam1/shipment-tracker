package dev.masadaslam1.shipment_tracker.simulation;

import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;
import dev.masadaslam1.shipment_tracker.messaging.ShipmentEvent;
import dev.masadaslam1.shipment_tracker.messaging.ShipmentEventConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentSimulatorTest {

    private static final int FULL_LIFECYCLE_TICKS = 40;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ShipmentSimulator simulator;

    private final ArgumentCaptor<ShipmentEvent> eventCaptor = ArgumentCaptor.forClass(ShipmentEvent.class);

    @BeforeEach
    void stubObjectMapper() {
        when(objectMapper.writeValueAsString(any(ShipmentEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0).toString());
    }

    @Test
    void spawnPublishesCreatedEventsKeyedByTrackingNumber() {
        simulator.spawnInitialShipments();

        verify(objectMapper, times(SimulationData.TEMPLATES.size())).writeValueAsString(eventCaptor.capture());

        List<String> trackingNumbers = eventCaptor.getAllValues().stream()
                .map(ShipmentEvent::trackingNumber)
                .toList();
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(SimulationData.TEMPLATES.size()))
                .send(eq(ShipmentEventConsumer.SHIPMENT_EVENTS_TOPIC), keyCaptor.capture(), anyString());

        assertTrue(keyCaptor.getAllValues().containsAll(trackingNumbers),
                "every message must be keyed by its tracking number");
        for (ShipmentEvent event : eventCaptor.getAllValues()) {
            assertEquals(ShipmentStatus.CREATED, event.shipmentStatus());
            assertEquals(event.origin(), event.currentLocation());
        }
    }

    @Test
    void everyShipmentReachesDeliveredWithinFullLifecycle() {
        simulator.spawnInitialShipments();
        runTicks(FULL_LIFECYCLE_TICKS);

        Map<String, List<ShipmentEvent>> eventsByTrackingNumber = eventsByTrackingNumber();

        for (String trackingNumber : List.of("TRK-1000", "TRK-1001", "TRK-1002", "TRK-1003", "TRK-1004")) {
            List<ShipmentEvent> events = eventsByTrackingNumber.get(trackingNumber);
            assertNotNull(events, "no events published for " + trackingNumber);
            assertEquals(ShipmentStatus.CREATED, events.get(0).shipmentStatus());
            assertEquals(ShipmentStatus.DELIVERED, events.get(events.size() - 1).shipmentStatus());
            assertNotNull(events.get(events.size() - 1).actualDelivery(), "delivery must stamp actualDelivery");
        }
    }

    @Test
    void deliveredShipmentsRespawnWithFreshTrackingNumbers() {
        simulator.spawnInitialShipments();
        runTicks(FULL_LIFECYCLE_TICKS);

        verify(objectMapper, atLeast(1)).writeValueAsString(eventCaptor.capture());

        assertTrue(eventCaptor.getAllValues().stream()
                .map(ShipmentEvent::trackingNumber)
                .anyMatch(number -> number.compareTo("TRK-1004") > 0),
                "delivered shipments must respawn under new tracking numbers");
    }

    @Test
    void aDeliveredTrackingNumberNeverSendsAgain() {
        simulator.spawnInitialShipments();
        runTicks(FULL_LIFECYCLE_TICKS);

        for (Map.Entry<String, List<ShipmentEvent>> entry : eventsByTrackingNumber().entrySet()) {
            List<ShipmentEvent> events = entry.getValue();
            for (int i = 0; i < events.size() - 1; i++) {
                assertNotEquals(ShipmentStatus.DELIVERED, events.get(i).shipmentStatus(),
                        entry.getKey() + " published an event after being delivered");
            }
        }
    }

    private void runTicks(int ticks) {
        for (int i = 0; i < ticks; i++) {
            simulator.simulate();
        }
    }

    private Map<String, List<ShipmentEvent>> eventsByTrackingNumber() {
        verify(objectMapper, atLeast(1)).writeValueAsString(eventCaptor.capture());
        Map<String, List<ShipmentEvent>> eventsByTrackingNumber = new LinkedHashMap<>();
        for (ShipmentEvent event : eventCaptor.getAllValues()) {
            eventsByTrackingNumber.computeIfAbsent(event.trackingNumber(), key -> new ArrayList<>()).add(event);
        }
        return eventsByTrackingNumber;
    }
}
