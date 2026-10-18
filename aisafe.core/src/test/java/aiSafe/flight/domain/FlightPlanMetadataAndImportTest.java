package aiSafe.flight.domain;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.dsl.model.*;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coverage for {@link FlightPlan} metadata derivation, import merge, and lifecycle flags.
 */
class FlightPlanMetadataAndImportTest {

    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA"));
    }

    @Test
    void flightPlan_returns_null_when_no_plans() {
        assertNull(flight.flightPlan());
        assertTrue(flight.flightPlans().isEmpty());
        assertFalse(flight.isActiveOrPending());
    }

    @Test
    void orm_default_constructor_can_be_invoked() throws Exception {
        final Constructor<FlightPlan> ctor = FlightPlan.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }

    @Test
    void protected_constructor_via_subclass() {
        assertNotNull(new EmptyFlightPlan());
    }

    @Test
    void applyImportFrom_requires_import() {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                FlightPlanTest.sampleDslSource());
        assertThrows(NullPointerException.class, () -> plan.applyImportFrom(null));
    }

    @Test
    void legCount_returns_zero_when_field_null() throws Exception {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                FlightPlanTest.sampleDslSource());
        setField(plan, "legCount", null);
        assertEquals(0, plan.legCount());
    }

    @Test
    void metadata_uses_last_arrival_when_no_departure_lines() {
        final String arrivalsOnly = """
                flight ARR2 {
                    type REGULAR
                    leg { arrival OPO 2026-05-11 08:00 route { origin OPO destination MAD } segment { start (1,1) end (2,2) altitude 1 m } fuel 1 kg }
                    leg { arrival MAD 2026-05-11 09:00 route { origin MAD destination LIS } segment { start (1,1) end (2,2) altitude 1 m } fuel 1 kg }
                    leg { arrival LIS 2026-05-11 10:00 route { origin MAD destination LIS } segment { start (1,1) end (2,2) altitude 1 m } fuel 1 kg }
                }
                """;
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                new DslSource(arrivalsOnly));
        assertEquals("ARR2", plan.dslIdentifier());
        assertEquals(3, plan.legCount());
    }

    @Test
    void fromValidatedImport_rejects_empty_legs() {
        final DslFlightPlan emptyLegs = new DslFlightPlan("TP123", FlightType.REGULAR, List.of(), 1);
        assertThrows(IllegalArgumentException.class, () -> FlightPlan.fromValidatedImport(emptyLegs));
        assertThrows(NullPointerException.class, () -> FlightPlan.fromValidatedImport(null));
    }

    @Test
    void applyImportFrom_keeps_existing_dsl_when_import_has_none() {
        final FlightPlan target = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                FlightPlanTest.sampleDslSource());
        final String original = target.dslSource().content();

        final FlightPlan metadataOnly = FlightPlan.fromValidatedImport(sampleDslPlan());
        target.applyImportFrom(metadataOnly);

        assertEquals(original, target.dslSource().content());
        assertEquals("OPO-LIS", target.routeSignature());
    }

    @Test
    void applyImportFrom_replaces_dsl_when_import_has_content() {
        final FlightPlan target = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight OLD { type REGULAR }"));

        final FlightPlan imported = FlightPlan.fromDslImport(sampleDslPlan(), sampleDslText());
        target.applyImportFrom(imported);

        assertTrue(target.dslSource().content().contains("flight TP123"));
        assertTrue(target.dslSource().content().contains("departure OPO"));
        assertEquals("TP123", target.dslIdentifier());
    }

    @Test
    void importFlightPlan_replaces_draft_and_cancels_previous() {
        flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP123 { type REGULAR }")
        );

        flight.importFlightPlan(FlightPlan.fromDslImport(sampleDslPlan(), sampleDslText()));

        assertEquals(2, flight.flightPlans().size());
        assertEquals(FlightPlanStatus.CANCELLED, flight.flightPlans().get(0).status());
        assertEquals("TP123", flight.flightPlan().dslIdentifier());
        assertEquals("OPO-LIS", flight.flightPlan().routeSignature());
        assertNotNull(flight.flightPlan().dslSource());
    }

    @Test
    void attachToFlight_links_plan_to_aggregate() {
        final FlightPlan plan = FlightPlan.fromDslImport(sampleDslPlan(), sampleDslText());
        plan.attachToFlight(flight);
        assertEquals(0, flight.flightPlans().size());
        flight.importFlightPlan(plan);
        assertSame(plan, flight.flightPlan());
    }

    @Test
    void metadata_skips_route_signature_when_departure_airports_missing() {
        final String arrivalOnly = """
                flight ARR1 {
                    type REGULAR
                    leg {
                        arrival LIS 2026-05-11 09:00
                        route { origin OPO destination LIS }
                        segment { start (41.0, -8.0) end (38.0, -9.0) altitude 10000 m }
                        fuel 1000 kg
                    }
                }
                """;
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                new DslSource(arrivalOnly)
        );

        assertEquals("ARR1", plan.dslIdentifier());
        assertEquals(1, plan.legCount());
        assertNull(plan.routeSignature());
        assertTrue(plan.toString().contains("aircraft="));
    }

    @Test
    void applyRegistrationMetadata_keeps_dsl_identifier_and_fills_airports_from_route() {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP100 {"));

        plan.applyRegistrationMetadata("TP123", "OPO", "LIS", 1);

        assertEquals("TP100", plan.dslIdentifier());
        assertEquals("OPO", plan.originAirport());
        assertEquals("LIS", plan.destinationAirport());
        assertEquals("OPO-LIS", plan.routeSignature());
        assertEquals(1, plan.legCount());
    }

    @Test
    void applyRegistrationMetadata_uses_flight_designator_when_dsl_has_no_flight_line() {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("leg { departure OPO 2026-05-11 08:00 }"));

        plan.applyRegistrationMetadata("TP123", "OPO", "LIS", 1);

        assertEquals("TP123", plan.dslIdentifier());
    }

    @Test
    void extractFirstDepartureAndLastArrivalAirports_uses_last_arrival() {
        final String dsl = """
                flight TP123 {
                    leg { departure OPO 2026-05-11 08:00 arrival MAD 2026-05-11 09:00 }
                    leg { departure MAD 2026-05-11 10:00 arrival LIS 2026-05-11 11:00 }
                }
                """;
        final String[] airports = FlightPlan.extractFirstDepartureAndLastArrivalAirports(dsl);
        assertEquals("OPO", airports[0]);
        assertEquals("LIS", airports[1]);
    }

    @Test
    void metadata_without_flight_block_leaves_identifier_null() {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("leg { departure OPO 2026-05-11 08:00 }")
        );

        assertNull(plan.dslIdentifier());
    }

    @Test
    void status_flags_and_equals_with_id() throws Exception {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                FlightPlanTest.sampleDslSource());

        setField(plan, "id", 1L);
        setField(plan, "status", FlightPlanStatus.VALIDATED);

        assertTrue(plan.isValidated());
        assertTrue(plan.isPending());

        setField(plan, "status", FlightPlanStatus.CANCELLED);
        assertFalse(plan.isPending());
        assertFalse(plan.isValidated());

        final FlightPlan sameId = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-002"),
                departureDays(2),
                arrivalDays(2),
                new FuelQuantity(2000, "KG"),
                FlightPlanTest.sampleDslSource());
        setField(sameId, "id", 1L);

        assertEquals(plan, sameId);
        assertEquals(plan.hashCode(), sameId.hashCode());
    }

    @Test
    void equals_uses_id_when_only_one_side_has_id() throws Exception {
        final FlightPlan withId = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureDays(1),
                arrivalDays(1),
                new FuelQuantity(1000, "KG"),
                FlightPlanTest.sampleDslSource());
        setField(withId, "id", 99L);

        final FlightPlan withoutId = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-002"),
                departureDays(2),
                arrivalDays(2),
                new FuelQuantity(2000, "KG"),
                FlightPlanTest.sampleDslSource());

        assertNotEquals(withId, withoutId);
    }

    private static final class EmptyFlightPlan extends FlightPlan {
        private EmptyFlightPlan() {
            super();
        }
    }

    private static void setField(final Object target, final String name, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static DepartureDateTime departureDays(final int days) {
        return new DepartureDateTime(LocalDateTime.now().plusDays(days));
    }

    private static ArrivalDateTime arrivalDays(final int days) {
        return FlightPlanTest.sampleArrivalAfter(departureDays(days));
    }

    private static String sampleDslText() {
        return """
                flight TP123 {
                    type REGULAR
                    leg {
                        departure OPO 2026-05-11 08:00
                        arrival LIS 2026-05-11 09:00
                        route { origin OPO destination LIS }
                        segment { start (41.0, -8.0) end (38.0, -9.0) altitude 10000 m }
                        fuel 5000 kg
                    }
                }
                """;
    }

    private static DslFlightPlan sampleDslPlan() {
        final DslLeg leg = new DslLeg(
                new DslSchedulePoint("OPO", "2026-05-11", "08:00", 1),
                new DslSchedulePoint("LIS", "2026-05-11", "09:00", 2),
                new DslRoute("OPO", "LIS", 3),
                List.of(new DslSegment(
                        new DslCoordinates(41.0, -8.0),
                        new DslCoordinates(38.0, -9.0),
                        10000,
                        4)),
                new DslFuel(5000, "kg", 5),
                1);
        return new DslFlightPlan("TP123", FlightType.REGULAR, List.of(leg), 1);
    }
}
