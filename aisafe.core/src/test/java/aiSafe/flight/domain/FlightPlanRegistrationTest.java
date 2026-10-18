package aiSafe.flight.domain;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanRegistrationTest {

    private Flight flight;
    private AircraftRegistration aircraftRegistration;
    private PilotID pilotId;
    private DepartureDateTime departureDateTime;
    private ArrivalDateTime arrivalDateTime;

    @BeforeEach
    void setUp() {
        flight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA")
        );
        aircraftRegistration = AircraftRegistration.valueOf("CS-TUA");
        pilotId = new PilotID("P-001");
        departureDateTime = new DepartureDateTime(LocalDateTime.now().plusDays(1));
        arrivalDateTime = FlightPlanTest.sampleArrivalAfter(departureDateTime);
    }

    @Test
    void ensureFuelQuantityCannotBeNegative() {
        assertThrows(IllegalArgumentException.class, () -> new FuelQuantity(-100, "KG"));
    }

    @Test
    void ensureFuelQuantityCannotBeZero() {
        assertThrows(IllegalArgumentException.class, () -> new FuelQuantity(0, "KG"));
    }

    @Test
    void ensureFlightPlanCannotHaveNullDepartureDateTime() {
        final FuelQuantity fuel = new FuelQuantity(1000, "KG");
        final Executable action = () -> flight.addFlightPlan(
                aircraftRegistration, pilotId, null, arrivalDateTime, fuel, FlightPlanTest.sampleDslSource());
        assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    void ensureFlightPlanCannotHavePastDepartureDate() {
        final FuelQuantity fuel = new FuelQuantity(1000, "KG");
        assertThrows(IllegalArgumentException.class,
                () -> {
                    final DepartureDateTime past = new DepartureDateTime(LocalDateTime.now().minusDays(1));
                    flight.addFlightPlan(
                            aircraftRegistration,
                            pilotId,
                            past,
                            FlightPlanTest.sampleArrivalAfter(past),
                            fuel,
                            FlightPlanTest.sampleDslSource());
                });
    }

    @Test
    void ensureFlightPlanStartsInDraftState() {
        final FuelQuantity fuel = new FuelQuantity(1000, "KG");
        final FlightPlan plan = flight.addFlightPlan(
                aircraftRegistration, pilotId, departureDateTime, arrivalDateTime, fuel, FlightPlanTest.sampleDslSource());
        assertEquals(FlightPlanStatus.DRAFT, plan.status());
    }

    @Test
    void ensureFlightPlanCannotHaveNullAircraftRegistration() {
        final FuelQuantity fuel = new FuelQuantity(1000, "KG");
        assertThrows(IllegalArgumentException.class,
                () -> flight.addFlightPlan(null, pilotId, departureDateTime, arrivalDateTime, fuel, FlightPlanTest.sampleDslSource()));
    }

    @Test
    void ensureFlightPlanCannotHaveNullPilotId() {
        final FuelQuantity fuel = new FuelQuantity(1000, "KG");
        assertThrows(IllegalArgumentException.class,
                () -> flight.addFlightPlan(aircraftRegistration, null, departureDateTime, arrivalDateTime, fuel, FlightPlanTest.sampleDslSource()));
    }

    @Test
    void ensureFlightPlanCannotHaveNullDslSource() {
        final FuelQuantity fuel = new FuelQuantity(1000, "KG");
        assertThrows(IllegalArgumentException.class,
                () -> flight.addFlightPlan(aircraftRegistration, pilotId, departureDateTime, arrivalDateTime, fuel, null));
    }

    @Test
    void ensureFuelUnitIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> new FuelQuantity(100, null));
        assertThrows(IllegalArgumentException.class, () -> new FuelQuantity(100, "  "));
    }

    @Test
    void ensureFlightPlanIsCreatedSuccessfully() {
        final FuelQuantity fuel = new FuelQuantity(1500, "KG");
        final FlightPlan plan = flight.addFlightPlan(
                aircraftRegistration, pilotId, departureDateTime, arrivalDateTime, fuel, FlightPlanTest.sampleDslSource());

        assertNotNull(plan);
        assertEquals(FlightPlanStatus.DRAFT, plan.status());
        assertEquals(aircraftRegistration, plan.aircraftRegistration());
        assertEquals(pilotId, plan.pilotId());
        assertEquals(departureDateTime, plan.departureDateTime());
        assertEquals(arrivalDateTime, plan.arrivalDateTime());
        assertEquals(fuel, plan.fuelQuantity());
        assertNotNull(plan.dslSource());
    }
}
