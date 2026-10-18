package aiSafe.flight.domain;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.dsl.model.*;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanLifecycleTest {

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
    void registerOrReplace_cancels_previous_draft() {
        flight.registerOrReplaceFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure(1),
                arrival(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP123 { type REGULAR }"));

        flight.registerOrReplaceFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure(2),
                arrival(2),
                new FuelQuantity(2000, "KG"),
                new DslSource("flight TP123 { type CHARTER }"));

        assertEquals(2, flight.flightPlans().size());
        assertEquals(FlightPlanStatus.CANCELLED, flight.flightPlans().get(0).status());
        assertEquals(FlightPlanStatus.DRAFT, flight.flightPlan().status());
        assertTrue(flight.flightPlan().dslSource().content().contains("CHARTER"));
    }

    @Test
    void import_replaces_draft_and_keeps_cancelled_history() {
        flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure(1),
                arrival(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP123 { type REGULAR }"));

        flight.importFlightPlan(FlightPlan.fromDslImport(sampleDslPlan(), sampleDslText()));

        assertEquals(2, flight.flightPlans().size());
        assertEquals(FlightPlanStatus.CANCELLED, flight.flightPlans().get(0).status());
        assertEquals("OPO-LIS", flight.flightPlan().routeSignature());
    }

    @Test
    void registerOrReplace_blocks_when_validated() throws Exception {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure(1),
                arrival(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP123 { type REGULAR }"));
        setStatus(plan, FlightPlanStatus.VALIDATED);

        assertThrows(IllegalStateException.class, () -> flight.registerOrReplaceFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure(2),
                arrival(2),
                new FuelQuantity(2000, "KG"),
                new DslSource("flight TP123 { type REGULAR }")));
    }

    @Test
    void markAsValidated_only_from_draft() throws Exception {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure(1),
                arrival(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP123 { type REGULAR }"));
        plan.markAsValidated();
        assertEquals(FlightPlanStatus.VALIDATED, plan.status());
    }

    @Test
    void cancel_rejects_validated_plan() throws Exception {
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure(1),
                arrival(1),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP123 { type REGULAR }"));
        setStatus(plan, FlightPlanStatus.VALIDATED);

        assertThrows(IllegalStateException.class, plan::cancel);
    }

    private static DepartureDateTime departure(final int daysAhead) {
        return new DepartureDateTime(LocalDateTime.now().plusDays(daysAhead));
    }

    private static ArrivalDateTime arrival(final int daysAhead) {
        final LocalDateTime dep = LocalDateTime.now().plusDays(daysAhead);
        return new ArrivalDateTime(dep.plusHours(1), dep);
    }

    private static void setStatus(final FlightPlan plan, final FlightPlanStatus status) throws Exception {
        final var field = FlightPlan.class.getDeclaredField("status");
        field.setAccessible(true);
        field.set(plan, status);
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
                new DslFuel(1000, "kg", 5),
                1);
        return new DslFlightPlan("TP123", FlightType.REGULAR, List.of(leg), 1);
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
                        fuel 1000 kg
                    }
                }
                """;
    }
}
