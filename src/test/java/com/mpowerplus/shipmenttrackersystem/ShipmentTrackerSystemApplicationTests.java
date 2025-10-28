package com.mpowerplus.shipmenttrackersystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Basic smoke test for the main application class.
 * Full context loading tests will be added once all adapters are implemented.
 */
class ShipmentTrackerSystemApplicationTests {

	@Test
	void mainClassExists() {
		// Verify that the main application class can be loaded
		assertDoesNotThrow(() -> {
			Class.forName("com.mpowerplus.shipmenttrackersystem.ShipmentTrackerSystemApplication");
		});
	}

}
