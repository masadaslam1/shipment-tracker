package dev.masadaslam1.shipment_tracker.simulation;

import java.math.BigDecimal;
import java.util.List;

/**
 * Static pool of shipment templates the simulator draws from.
 * Each template describes a route with intermediate waypoints.
 */
public final class SimulationData {

    private SimulationData() {
    }

    public record ShipmentTemplate(String origin, String destination, List<String> waypoints,
                                   String carrier, BigDecimal weight, String description) {
    }

    public static final List<ShipmentTemplate> TEMPLATES = List.of(
            new ShipmentTemplate("Shanghai", "Rotterdam", List.of("Singapore", "Colombo", "Suez"),
                    "Maersk", new BigDecimal("12.50"), "Container vessel, electronics"),
            new ShipmentTemplate("Hamburg", "New York", List.of("Southampton", "Halifax"),
                    "Hapag-Lloyd", new BigDecimal("8.75"), "Automotive parts"),
            new ShipmentTemplate("Dubai", "Hamburg", List.of("Jeddah", "Valencia"),
                    "MSC", new BigDecimal("20.00"), "Textiles and apparel"),
            new ShipmentTemplate("Singapore", "Los Angeles", List.of("Hong Kong", "Yokohama", "Honolulu"),
                    "CMA CGM", new BigDecimal("15.30"), "Consumer goods"),
            new ShipmentTemplate("Rotterdam", "Singapore", List.of("Algeciras", "Port Said", "Colombo"),
                    "Evergreen", new BigDecimal("18.90"), "Machinery and equipment")
    );
}
