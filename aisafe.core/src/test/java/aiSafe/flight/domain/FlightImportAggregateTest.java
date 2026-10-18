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

class FlightImportAggregateTest {

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
    void importFlightPlan_rejects_null_plan() {
        assertThrows(Exception.class, () -> flight.importFlightPlan(null));
    }

    @Test
    void importFlightPlan_rejects_plan_without_dsl() {
        final FlightPlan plan = FlightPlan.fromValidatedImport(sampleDslPlan());
        assertThrows(IllegalArgumentException.class, () -> flight.importFlightPlan(plan));
    }

    @Test
    void importFlightPlan_rejects_route_mismatch() {
        final DepartureDateTime departure = new DepartureDateTime(LocalDateTime.now().plusDays(1));
        flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure,
                FlightPlanTest.sampleArrivalAfter(departure),
                new FuelQuantity(1000, "KG"),
                FlightPlanTest.sampleDslSource());

        final String mismatchedRouteDsl = """
                flight TP123 {
                    type REGULAR
                    leg {
                        departure MAD 2026-05-11 08:00
                        arrival LIS 2026-05-11 09:00
                        route { origin MAD destination LIS }
                        segment { start (41.0, -8.0) end (38.0, -9.0) altitude 10000 m }
                        fuel 5000 kg
                    }
                }
                """;
        final DslFlightPlan mismatchedPlan = new DslFlightPlan(
                "TP123",
                FlightType.REGULAR,
                List.of(new DslLeg(
                        new DslSchedulePoint("MAD", "2026-05-11", "08:00", 1),
                        new DslSchedulePoint("LIS", "2026-05-11", "09:00", 2),
                        new DslRoute("MAD", "LIS", 3),
                        List.of(new DslSegment(
                                new DslCoordinates(41.0, -8.0),
                                new DslCoordinates(38.0, -9.0),
                                10000,
                                4)),
                        new DslFuel(5000, "kg", 5),
                        1)),
                1);
        assertThrows(IllegalArgumentException.class,
                () -> flight.importFlightPlan(FlightPlan.fromDslImport(mismatchedPlan, mismatchedRouteDsl)));
    }

    @Test
    void identity_returns_null_before_persistence() {
        assertNull(flight.identity());
    }

    @Test
    void importFlightPlan_attaches_when_no_pending_plan_exists() {
        final FlightPlan imported = FlightPlan.fromDslImport(sampleDslPlan(), sampleDslText());
        flight.importFlightPlan(imported);
        assertEquals(1, flight.flightPlans().size());
        assertTrue(flight.isActiveOrPending());
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
