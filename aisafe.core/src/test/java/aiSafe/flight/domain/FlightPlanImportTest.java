package aiSafe.flight.domain;

import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.dsl.model.*;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.domain.FlightRoute;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanImportTest {

  @Test
  void ensureFromValidatedImportBuildsDomainPlan() {
    final DslFlightPlan dsl = sampleDslPlan();

    final FlightPlan plan = FlightPlan.fromDslImport(dsl, sampleDslText());

    assertEquals(FlightPlanStatus.DRAFT, plan.status());
    assertNotNull(plan.dslSource());
    assertEquals("TP123", plan.dslIdentifier());
    assertEquals("OPO", plan.originAirport());
    assertEquals("LIS", plan.destinationAirport());
    assertEquals("OPO-LIS", plan.routeSignature());
    assertEquals(1, plan.legCount());
  }

  @Test
  void ensureFlightAcceptsMatchingValidatedPlan() {
    final Flight flight = new Flight(
            FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
            FlightDesignator.of("TP",123,null),
        FlightType.REGULAR,
        AircraftRegistration.valueOf("CS-TUA"));

    flight.importFlightPlan(FlightPlan.fromDslImport(sampleDslPlan(), sampleDslText()));

    assertEquals(FlightPlanStatus.DRAFT, flight.flightPlan().status());
    assertEquals("OPO-LIS", flight.flightPlan().routeSignature());
  }

  @Test
  void ensureRouteMismatchIsRejected() {
    final Flight flight = new Flight(
            FlightRoute.valueOf(RouteName.valueOf("XL", 1234), RouteDesignator.valueOf("LIS", "ASS")),
            FlightDesignator.of("TP", 123,null),
        FlightType.REGULAR,
        AircraftRegistration.valueOf("CS-TUA"));

    assertThrows(IllegalArgumentException.class,
        () -> flight.importFlightPlan(FlightPlan.fromDslImport(sampleDslPlan(), sampleDslText())));
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
