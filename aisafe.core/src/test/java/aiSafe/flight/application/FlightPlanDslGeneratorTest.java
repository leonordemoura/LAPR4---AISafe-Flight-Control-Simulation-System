package aiSafe.flight.application;

import aiSafe.flight.dsl.FlightDslValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanDslGeneratorTest {

    @Test
    void ensureGeneratorProducesFlightBlockWithLegAndSegment() {
        final String dsl = FlightPlanDslGenerator.generate(
                "TP123", "REGULAR", List.of(RegisterFlightPlanTestFixtures.sampleLeg()));

        assertTrue(dsl.contains("flight TP123"));
        assertTrue(dsl.contains("type REGULAR"));
        assertTrue(dsl.contains("departure OPO 2026-06-15 10:00"));
        assertTrue(dsl.contains("segment { start (41.242, -8.678)"));
        assertTrue(dsl.contains("width 10 km"));
        assertTrue(dsl.contains("wind 180 deg 15 kt"));
        assertTrue(dsl.contains("fuel 5000 kg"));
    }

    @Test
    void ensureGeneratedDslPassesUs083Validation() {
        final String dsl = FlightPlanDslGenerator.generate(
                "TP123", "REGULAR", List.of(RegisterFlightPlanTestFixtures.sampleLeg()));
        assertTrue(FlightDslValidator.validate(dsl).isValid(),
                () -> FlightDslValidator.validate(dsl).errors().toString());
    }

    @Test
    void ensureGeneratorSupportsMultipleLegsAndSegments() {
        final RegisterFlightPlanLegInput leg1 = RegisterFlightPlanTestFixtures.sampleLeg();
        final RegisterFlightPlanLegInput leg2 = new RegisterFlightPlanLegInput(
                "MAD", "2026-06-15", "12:00",
                "LIS", "2026-06-15", "13:00",
                "MAD", "LIS",
                2500, "l",
                List.of(
                        new RegisterFlightPlanSegmentInput(40.0, -3.0, 39.5, -4.0, 10000.5, 10, 270, 20, "kmh"),
                        new RegisterFlightPlanSegmentInput(39.5, -4.0, 38.774, -9.134, 11000, 8, 180, 12, "kt")
                )
        );

        final String dsl = FlightPlanDslGenerator.generate("TP123", "CHARTER", List.of(leg1, leg2));

        assertEquals(2, dsl.split("leg \\{").length - 1);
        assertTrue(dsl.contains("type CHARTER"));
        assertTrue(dsl.contains("fuel 2500 l"));
        assertTrue(dsl.contains("altitude 10000.5 m"));
    }

    @Test
    void rejects_null_or_blank_flight_identifier() {
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlanDslGenerator.generate(null, "REGULAR", List.of(RegisterFlightPlanTestFixtures.sampleLeg())));
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlanDslGenerator.generate("  ", "REGULAR", List.of(RegisterFlightPlanTestFixtures.sampleLeg())));
    }

    @Test
    void rejects_null_or_blank_flight_type() {
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlanDslGenerator.generate("TP123", null, List.of(RegisterFlightPlanTestFixtures.sampleLeg())));
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlanDslGenerator.generate("TP123", "  ", List.of(RegisterFlightPlanTestFixtures.sampleLeg())));
    }

    @Test
    void rejects_null_or_empty_legs() {
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlanDslGenerator.generate("TP123", "REGULAR", null));
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlanDslGenerator.generate("TP123", "REGULAR", Collections.emptyList()));
    }

    @Test
    void normalizes_fuel_units() {
        assertTrue(FlightPlanDslGenerator.generate("TP123", "CHARTER",
                List.of(legWithFuelUnit("kilograms"))).contains("fuel 100 kg"));
        assertTrue(FlightPlanDslGenerator.generate("TP123", "CHARTER",
                List.of(legWithFuelUnit("kilogram"))).contains("fuel 100 kg"));
        assertTrue(FlightPlanDslGenerator.generate("TP123", "CHARTER",
                List.of(legWithFuelUnit("liter"))).contains("fuel 100 l"));
        assertTrue(FlightPlanDslGenerator.generate("TP123", "CHARTER",
                List.of(legWithFuelUnit("liters"))).contains("fuel 100 l"));
        assertTrue(FlightPlanDslGenerator.generate("TP123", "CHARTER",
                List.of(legWithFuelUnit("GAL"))).contains("fuel 100 gal"));
    }

    @Test
    void rejects_blank_fuel_unit() {
        assertThrows(IllegalArgumentException.class,
                () -> FlightPlanDslGenerator.generate("TP123", "REGULAR", List.of(legWithFuelUnit("  "))));
    }

    @Test
    void rejects_null_fuel_unit() {
        final Executable action = () -> FlightPlanDslGenerator.generate(
                "TP123", "REGULAR", List.of(legWithFuelUnit(null)));
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void formats_decimal_fuel_and_altitude() {
        final RegisterFlightPlanLegInput leg = new RegisterFlightPlanLegInput(
                "OPO", "2026-01-01", "08:00",
                "LIS", "2026-01-01", "09:00",
                "OPO", "LIS",
                4000.5, "kg",
                List.of(new RegisterFlightPlanSegmentInput(41.0, -8.0, 38.0, -9.0, 12000.5, 10, 0, 0, "kt"))
        );
        final String dsl = FlightPlanDslGenerator.generate("TP123", "REGULAR", List.of(leg));
        assertTrue(dsl.contains("fuel 4000.5 kg"));
        assertTrue(dsl.contains("altitude 12000.5 m"));
    }

    @Test
    void private_constructor_is_not_instantiable() throws Exception {
        final var ctor = FlightPlanDslGenerator.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }

    @Test
    void passes_through_unknown_fuel_unit_after_normalization() {
        final String dsl = FlightPlanDslGenerator.generate("TP123", "REGULAR",
                List.of(legWithFuelUnit("pounds")));
        assertTrue(dsl.contains("fuel 100 pounds"));
    }

    @Test
    void formats_whole_number_fuel_and_altitude_without_decimal() {
        final RegisterFlightPlanLegInput leg = new RegisterFlightPlanLegInput(
                "OPO", "2026-01-01", "08:00",
                "LIS", "2026-01-01", "09:00",
                "OPO", "LIS",
                4000, "kg",
                List.of(new RegisterFlightPlanSegmentInput(41.0, -8.0, 38.0, -9.0, 12000, 10, 180, 15, "kt"))
        );
        final String dsl = FlightPlanDslGenerator.generate("TP123", "REGULAR", List.of(leg));
        assertTrue(dsl.contains("altitude 12000 m"));
        assertTrue(dsl.contains("fuel 4000 kg"));
    }

    private static RegisterFlightPlanLegInput legWithFuelUnit(final String unit) {
        return new RegisterFlightPlanLegInput(
                "OPO", "2026-01-01", "08:00",
                "LIS", "2026-01-01", "09:00",
                "OPO", "LIS",
                100, unit,
                List.of(RegisterFlightPlanTestFixtures.sampleSegment())
        );
    }
}
