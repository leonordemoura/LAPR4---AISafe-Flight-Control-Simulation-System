package aiSafe.flight.domain;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.weatherdata.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightTest {

    private Flight validFlight() {
        return new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TKY")
        );
    }

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureFlightIsCreatedSuccessfully() {
        assertNotNull(validFlight());
    }

    @Test
    void ensureDesignatorIsCorrect() {
        assertEquals(FlightDesignator.of("TP", 123, null), validFlight().designator());
    }

    @Test
    void ensureTypeIsCorrect() {
        assertEquals(FlightType.REGULAR, validFlight().type());
    }

    @Test
    void ensureFlightStartsWithoutOperationalPlans() {
        assertNull(validFlight().flightPlan());
        assertTrue(validFlight().flightPlans().isEmpty());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureRouteIdCannotBeNull() {
        assertThrows(Exception.class, () -> new Flight(
                null,
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TKY")
        ));
    }

    @Test
    void ensureDesignatorCannotBeNull() {
        assertThrows(Exception.class, () -> new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                null,
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TKY")
        ));
    }

    @Test
    void ensureTypeCannotBeNull() {
        assertThrows(Exception.class, () -> new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 1234, null),
                null,
                AircraftRegistration.valueOf("CS-TKY")
        ));
    }

    @Test
    void ensureAircraftRegistrationCannotBeNull() {
        assertThrows(Exception.class, () -> new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 1234, null),
                FlightType.REGULAR,
                null
        ));
    }

    // =========================================================
    // BUSINESS LOGIC TESTS
    // =========================================================

    @Test
    void ensureNewFlightIsNotActiveOrPendingWithoutPlans() {
        assertFalse(validFlight().isActiveOrPending());
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureTwoFlightsWithSameIdAreEqual() {
        assertEquals(validFlight(), validFlight());
    }

    @Test
    void ensureTwoFlightsWithDifferentIdsAreNotEqual() {
        final var other = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("FR", 5678, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TKY")
        );
        assertNotEquals(validFlight(), other);
    }

    @Test
    void ensureSameAsReturnsTrueForSameFlightId() {
        assertTrue(validFlight().sameAs(validFlight()));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentType() {
        assertFalse(validFlight().sameAs("TP1234"));
    }

    @Test
    void ensureRouteNameAndAircraftRegistrationGetters() {
        final Flight f = validFlight();
        assertEquals(RouteName.valueOf("TP", 123), f.route().routeName());
        assertEquals(AircraftRegistration.valueOf("CS-TKY"), f.aircraftRegistration());
    }

    @Test
    void ensureToStringAndHashCode() {
        final Flight f = validFlight();
        assertTrue(f.toString().contains("TP123") || f.toString().contains("TP"));
        assertEquals(f.hashCode(), validFlight().hashCode());
    }

    @Test
    void ensureEqualsEdgeCases() {
        assertNotEquals(validFlight(), null);
        assertNotEquals(validFlight(), "not a flight");
    }

    @Test
    void orm_constructor_can_be_invoked() throws Exception {
        final var ctor = Flight.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }

    @Test
    void ensureCharterTypeIsSupported() {
        final Flight flight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 1234, null),
                FlightType.CHARTER,
                AircraftRegistration.valueOf("CS-TKY")
        );
        assertEquals(FlightType.CHARTER, flight.type());
    }

    // =========================================================
    // INSERT WEATHER DATA (US082)
    // =========================================================

    @Test
    void ensureInsertWeatherDataDelegatesToPendingPlan() {
        final Flight flight = validFlight();
        addDraftPlan(flight);

        flight.insertWeatherData(buildWeatherData());

        assertNotNull(flight.flightPlan().weatherData());
    }

    @Test
    void ensureInsertWeatherDataThrowsWhenNoPendingPlan() {
        final Flight flight = validFlight();

        assertThrows(IllegalStateException.class,
                () -> flight.insertWeatherData(buildWeatherData()));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private static FlightPlan addDraftPlan(final Flight flight) {
        final DepartureDateTime departure = new DepartureDateTime(LocalDateTime.now().plusDays(1));
        return flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure,
                FlightPlanTest.sampleArrivalAfter(departure),
                new FuelQuantity(1000, "KG"),
                FlightPlanTest.sampleDslSource()
        );
    }

    private static WeatherData buildWeatherData() {
        return new WeatherData(
                new AreaCode("LPPT"),
                new ObservationDate(
                        LocalDateTime.of(2026, 6, 5, 0, 0),
                        LocalDateTime.of(2026, 6, 5, 23, 59)
                ),
                new WeatherSource("METAR"),
                new WeatherSubArea(38.0, 39.0, -9.5, -8.5, 0, 3000),
                new WindDirection(270),
                new WindSpeed(14, WindSpeedUnit.KT),
                new Temperature(18, TemperatureUnit.C),
                new Pressure(1013, PressureUnit.HPA)
        );
    }
}