package aiSafe.flight.domain;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.weatherdata.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanTest {

    private Flight flight;
    private FlightPlan draftPlan;

    @BeforeEach
    void setUp() {
        flight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA")
        );
        final DepartureDateTime departure = new DepartureDateTime(LocalDateTime.now().plusDays(1));
        draftPlan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure,
                sampleArrivalAfter(departure),
                new FuelQuantity(1000, "KG"),
                sampleDslSource()
        );
    }

    @Test
    void ensureFlightPlanIsCreatedAsDraft() {
        assertNotNull(draftPlan);
    }

    @Test
    void ensureDraftStatusIsCorrect() {
        assertEquals(FlightPlanStatus.DRAFT, draftPlan.status());
    }

    @Test
    void ensureToStringReturnsCorrectValue() {
        assertEquals(FlightPlanStatus.DRAFT, draftPlan.status());
        assertTrue(draftPlan.toString().contains("DRAFT"));
    }

    @Test
    void ensureDraftIsPending() {
        assertTrue(draftPlan.isPending());
    }

    @Test
    void ensureFlightPlanIsNotEqualToNull() {
        assertNotEquals(null, draftPlan);
    }

    @Test
    void ensureFlightPlanIsNotEqualToDifferentType() {
        assertFalse(draftPlan.equals("DRAFT"));
    }

    @Test
    void ensureHashCodeIsConsistent() {
        assertEquals(draftPlan.hashCode(), draftPlan.hashCode());
    }

    @Test
    void ensureDslSourceIsStored() {
        assertNotNull(draftPlan.dslSource());
        assertTrue(draftPlan.dslSource().content().contains("flight TP123"));
    }

    @Test
    void ensureMetadataDerivedFromMultiLegDsl() {
        final String multiLegDsl = """
                flight TP123 {
                    type REGULAR
                    leg {
                        departure OPO 2026-05-11 08:00
                        arrival MAD 2026-05-11 09:00
                        route { origin OPO destination MAD }
                        segment { start (41.0, -8.0) end (40.0, -4.0) altitude 10000 m }
                        fuel 3000 kg
                    }
                    leg {
                        departure MAD 2026-05-11 10:00
                        arrival LIS 2026-05-11 11:00
                        route { origin MAD destination LIS }
                        segment { start (40.0, -4.0) end (38.0, -9.0) altitude 11000 m }
                        fuel 2000 kg
                    }
                }
                """;
        final DepartureDateTime departure = new DepartureDateTime(LocalDateTime.now().plusDays(1));
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure,
                sampleArrivalAfter(departure),
                new FuelQuantity(1000, "KG"),
                new DslSource(multiLegDsl)
        );

        assertEquals("TP123", plan.dslIdentifier());
        assertEquals(2, plan.legCount());
        assertEquals("OPO", plan.originAirport());
        assertEquals("LIS", plan.destinationAirport());
        assertEquals("OPO-LIS", plan.routeSignature());
        assertTrue(plan.toString().contains("OPO-LIS"));
    }

    @Test
    void ensureEqualsUsesIdWhenPresent() {
        final FlightPlan other = draftPlan;
        assertEquals(draftPlan, other);
    }

    @Test
    void ensureEqualsByBusinessFieldsWithoutId() {
        final DepartureDateTime departureB = new DepartureDateTime(LocalDateTime.now().plusDays(2));
        final FlightPlan b = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departureB,
                sampleArrivalAfter(departureB),
                new FuelQuantity(1000, "KG"),
                sampleDslSource()
        );
        assertNotEquals(draftPlan, b);
    }

    @Test
    void ensureStatusFlags() {
        assertFalse(draftPlan.isValidated());
        assertTrue(draftPlan.isPending());
    }

    @Test
    void ensureToStringWithoutRouteSignatureWhenMetadataMissing() {
        final DepartureDateTime departure = new DepartureDateTime(LocalDateTime.now().plusDays(1));
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure,
                sampleArrivalAfter(departure),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight NOLEGS { type REGULAR }")
        );
        assertTrue(plan.toString().contains("aircraft="));
        assertEquals(0, plan.legCount());
    }

    // =========================================================
    // INSERT WEATHER DATA (US082)
    // =========================================================

    @Test
    void ensureWeatherDataIsAttachedToFlightPlan() {
        draftPlan.insertWeatherData(buildWeatherData());

        assertNotNull(draftPlan.weatherData());
        assertTrue(draftPlan.hasWeatherData());
    }

    @Test
    void ensureValidatedPlanRevertsToDefaultAfterWeatherDataInsertion() {
        setStatus(draftPlan, FlightPlanStatus.VALIDATED);

        draftPlan.insertWeatherData(buildWeatherData());

        assertEquals(FlightPlanStatus.DRAFT, draftPlan.status());
        assertFalse(draftPlan.isValidated());
    }

    @Test
    void ensureNullWeatherDataIsRejected() {
        assertThrows(NullPointerException.class,
                () -> draftPlan.insertWeatherData(null));
    }

    @Test
    void ensureCancelledPlanRejectsWeatherDataInsertion() {
        setStatus(draftPlan, FlightPlanStatus.CANCELLED);

        assertThrows(IllegalStateException.class,
                () -> draftPlan.insertWeatherData(buildWeatherData()));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    static ArrivalDateTime sampleArrivalAfter(final DepartureDateTime departure) {
        return new ArrivalDateTime(departure.value().plusHours(1), departure.value());
    }

    static DslSource sampleDslSource() {
        return new DslSource("""
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
                """);
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

    private static void setStatus(final FlightPlan plan, final FlightPlanStatus status) {
        try {
            final var field = FlightPlan.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(plan, status);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not set FlightPlan status via reflection", e);
        }
    }
}