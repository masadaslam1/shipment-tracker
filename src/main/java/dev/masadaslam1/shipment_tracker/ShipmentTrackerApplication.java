package dev.masadaslam1.shipment_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShipmentTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShipmentTrackerApplication.class, args);
	}

}
