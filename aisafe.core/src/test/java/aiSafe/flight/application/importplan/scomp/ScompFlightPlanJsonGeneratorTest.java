package aiSafe.flight.application.importplan.scomp;

import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.application.validate.simulator.FlightValidationProfileCatalog;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flight.domain.FlightType;
import aiSafe.flight.dsl.model.*;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScompFlightPlanJsonGeneratorTest {

    private final ScompFlightPlanJsonGenerator generator = new ScompFlightPlanJsonGenerator();

    @Test
    void generates_scomp_json_with_validation_profiles() {
        final Flight flight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA"));

        final String json = generator.generate(new ScompFlightExportSource(
                flight,
                samplePlan(),
                sampleValidationProfile(447.0)));

        assertTrue(json.contains("\"Flights\""));
        assertTrue(json.contains("\"ID\": 123"));
        assertTrue(json.contains("\"Type\": \"regular\""));
        assertTrue(json.contains("\"DepartureDate\": \"2026-06-10\""));
        assertTrue(json.contains("\"DepartureTime\": \"08:00\""));
        assertTrue(json.contains("\"Departure\": \"OPO\""));
        assertTrue(json.contains("\"Arrival\": \"LIS\""));
        assertTrue(json.contains("\"Quantity\": 5000"));
        assertTrue(json.contains("\"Climb\""));
        assertTrue(json.contains("\"Descend\""));
        assertTrue(json.contains("\"RateClimb\": { \"Value\": 24"));
        assertTrue(json.contains("\"RateDescent\": { \"Value\": -10"));
        assertTrue(json.contains("\"Value\": 447"));
        assertTrue(json.contains("\"Mode\": \"climb\""));
        assertTrue(json.contains("\"Mode\": \"cruise\""));
        assertTrue(json.contains("\"Mode\": \"descend\""));
    }

    @Test
    void generates_single_json_with_multiple_flights() {
        final Flight flightOne = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA"));
        final Flight flightTwo = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 456), RouteDesignator.valueOf("LIS", "OPO")),
                FlightDesignator.of("TP", 456, null),
                FlightType.CHARTER,
                AircraftRegistration.valueOf("CS-TUB"));

        final String json = generator.generate(List.of(
                new ScompFlightExportSource(flightOne, samplePlan(), sampleValidationProfile(447.0)),
                new ScompFlightExportSource(
                        flightTwo,
                        samplePlanFor("TP456", FlightType.CHARTER),
                        sampleValidationProfile(460.0))));

        assertTrue(json.contains("\"ID\": 123"));
        assertTrue(json.contains("\"ID\": 456"));
        assertTrue(json.contains("\"Type\": \"regular\""));
        assertTrue(json.contains("\"Type\": \"charter\""));
        assertTrue(json.contains("\"Value\": 447"));
        assertTrue(json.contains("\"Value\": 460"));
        assertEquals(1, json.split("\"Flights\"", -1).length - 1);
    }

    private static ScompValidationFlightProfile sampleValidationProfile(final double cruiseSpeedKnots) {
        final FlightValidationProfileCatalog validationProfiles = FlightValidationProfileCatalog.defaults();
        return new ScompValidationFlightProfile(
                validationProfiles.climb(),
                ScompValidationFlightProfileResolver.toScompDescentJsonSteps(validationProfiles.descend()),
                cruiseSpeedKnots);
    }

    private static DslFlightPlan samplePlan() {
        return samplePlanFor("TP123", FlightType.REGULAR);
    }

    private static DslFlightPlan samplePlanFor(final String identifier, final FlightType type) {
        final DslLeg leg = new DslLeg(
                new DslSchedulePoint("OPO", "2026-06-10", "08:00", 1),
                new DslSchedulePoint("LIS", "2026-06-10", "09:00", 2),
                new DslRoute("OPO", "LIS", 3),
                List.of(new DslSegment(
                        new DslCoordinates(41.242, -8.678),
                        new DslCoordinates(38.774, -9.134),
                        12000,
                        4)),
                new DslFuel(5000, "kg", 5),
                1);
        return new DslFlightPlan(identifier, type, List.of(leg), 1);
    }
}
