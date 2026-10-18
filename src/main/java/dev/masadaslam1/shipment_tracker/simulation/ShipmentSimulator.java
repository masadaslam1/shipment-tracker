package dev.masadaslam1.shipment_tracker.simulation;

import dev.masadaslam1.shipment_tracker.domain.ShipmentStatus;
import dev.masadaslam1.shipment_tracker.messaging.ShipmentEvent;
import dev.masadaslam1.shipment_tracker.messaging.ShipmentEventConsumer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Produces shipment events into the shipment-events topic so the dashboard
 * has live data without a console producer.
 * Only active when the "simulation" profile is enabled.
 */
@Profile("simulation")
@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentSimulator {

    /** Chance per tick that an in-transit shipment flips into EXCEPTION. */
    private static final double EXCEPTION_PROBABILITY = 0.04;
    /** Ticks a delivered shipment rests before it respawns as a new one. */
    private static final int RESPAWN_DELAY_TICKS = 8;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, ShipmentState> shipments = new LinkedHashMap<>();
    private final Random random = new Random(42); // fixed seed: reproducible demos
    private final AtomicInteger nextTrackingNumber = new AtomicInteger(1000);

    @PostConstruct
    void spawnInitialShipments() {
        for (SimulationData.ShipmentTemplate template : SimulationData.TEMPLATES) {
            spawn(template);
        }
        log.info("Simulator ready with {} shipments", shipments.size());
    }

    @Scheduled(fixedDelayString = "${simulation.tick-interval-ms:3000}")
    public void simulate() {
        for (Map.Entry<String, ShipmentState> entry : new ArrayList<>(shipments.entrySet())) {
            String trackingNumber = entry.getKey();
            ShipmentState state = entry.getValue();

            if (state.status == ShipmentStatus.DELIVERED) {
                if (--state.ticksUntilRespawn <= 0) {
                    shipments.remove(trackingNumber);
                    spawn(randomTemplate());
                }
                continue;
            }

            if (state.status == ShipmentStatus.EXCEPTION) {
                if (--state.exceptionTicksRemaining <= 0) {
                    state.status = ShipmentStatus.IN_TRANSIT;
                    send(trackingNumber, state);
                }
                continue;
            }

            advance(trackingNumber, state);
        }
    }

    private void advance(String trackingNumber, ShipmentState state) {
        switch (state.status) {
            case CREATED -> {
                state.status = ShipmentStatus.IN_TRANSIT;
                state.currentLocation = state.template.origin();
            }
            case IN_TRANSIT -> {
                if (random.nextDouble() < EXCEPTION_PROBABILITY) {
                    state.status = ShipmentStatus.EXCEPTION;
                    state.exceptionTicksRemaining = 1 + random.nextInt(2);
                } else if (state.waypointIndex < state.template.waypoints().size()) {
                    state.currentLocation = state.template.waypoints().get(state.waypointIndex++);
                } else {
                    state.status = ShipmentStatus.OUT_FOR_DELIVERY;
                    state.currentLocation = state.template.destination();
                }
            }
            case OUT_FOR_DELIVERY -> {
                state.status = ShipmentStatus.DELIVERED;
                state.currentLocation = state.template.destination();
                state.actualDelivery = Instant.now();
                state.ticksUntilRespawn = RESPAWN_DELAY_TICKS;
            }
            default -> {
                // DELIVERED and EXCEPTION are handled before advance() is called
            }
        }
        send(trackingNumber, state);
    }

    private void spawn(SimulationData.ShipmentTemplate template) {
        String trackingNumber = "TRK-" + nextTrackingNumber.getAndIncrement();
        ShipmentState state = new ShipmentState(template);
        state.status = ShipmentStatus.CREATED;
        state.currentLocation = template.origin();
        state.estimatedDelivery = Instant.now().plus(Duration.ofDays(2 + random.nextInt(4)));
        shipments.put(trackingNumber, state);
        send(trackingNumber, state);
    }

    private SimulationData.ShipmentTemplate randomTemplate() {
        return SimulationData.TEMPLATES.get(random.nextInt(SimulationData.TEMPLATES.size()));
    }

    private void send(String trackingNumber, ShipmentState state) {
        ShipmentEvent event = new ShipmentEvent(
                trackingNumber,
                state.status,
                state.template.origin(),
                state.template.destination(),
                state.currentLocation,
                state.estimatedDelivery,
                state.actualDelivery,
                state.template.weight(),
                state.template.carrier(),
                state.template.description());
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(ShipmentEventConsumer.SHIPMENT_EVENTS_TOPIC, trackingNumber, json);
            log.info("Simulated {} -> {}", trackingNumber, state.status);
        } catch (Exception e) {
            log.warn("Failed to publish simulated event for {}", trackingNumber, e);
        }
    }

    private static class ShipmentState {
        private final SimulationData.ShipmentTemplate template;
        private ShipmentStatus status;
        private String currentLocation;
        private int waypointIndex;
        private Instant estimatedDelivery;
        private Instant actualDelivery;
        private int ticksUntilRespawn;
        private int exceptionTicksRemaining;

        private ShipmentState(SimulationData.ShipmentTemplate template) {
            this.template = template;
        }
    }
}
