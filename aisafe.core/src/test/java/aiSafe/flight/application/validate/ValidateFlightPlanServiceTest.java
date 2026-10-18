package aiSafe.flight.application.validate;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.domain.AircraftStatus;
import aiSafe.aircraft.domain.CabinConfiguration;
import aiSafe.aircraft.domain.CurrentEngine;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.aircraftmanagement.domain.Country;
import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.aircraftmanagement.domain.EngineModelName;
import aiSafe.aircraftmanagement.domain.FuelType;
import aiSafe.aircraftmanagement.domain.FuelTypeDensity;
import aiSafe.aircraftmanagement.domain.FuelTypeName;
import aiSafe.aircraftmanagement.domain.FuelTypeSpecificEnergy;
import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.MakerID;
import aiSafe.aircraftmanagement.domain.MakerName;
import aiSafe.aircraftmanagement.domain.MotorizationType;
import aiSafe.aircraftmanagement.domain.Power;
import aiSafe.aircraftmanagement.domain.TSFC;
import aiSafe.aircraftmanagement.domain.Unit;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.aircraftModel.domain.AircraftModelName;
import aiSafe.aircraftModel.domain.AircraftType;
import aiSafe.aircraftModel.domain.Altitude;
import aiSafe.aircraftModel.domain.AltitudeUnit;
import aiSafe.aircraftModel.domain.Area;
import aiSafe.aircraftModel.domain.AreaUnit;
import aiSafe.aircraftModel.domain.DragCoefficient;
import aiSafe.aircraftModel.domain.LiftCoefficient;
import aiSafe.aircraftModel.domain.MaximumSeats;
import aiSafe.aircraftModel.domain.PhysicalSpecs;
import aiSafe.aircraftModel.domain.Speed;
import aiSafe.aircraftModel.domain.SpeedUnit;
import aiSafe.aircraftModel.domain.Weight;
import aiSafe.aircraftModel.domain.WeightUnit;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.flight.application.validate.simulator.AircraftSimulationParametersResolver;
import aiSafe.flight.application.validate.simulator.FlightPlanSimulationResult;
import aiSafe.flight.application.validate.simulator.FlightPlanSimulator;
import aiSafe.flight.application.validate.simulator.SimulatorModelParameters;
import aiSafe.flight.application.validate.simulator.ActiveEngineParameters;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateFlightPlanServiceTest {

    private static final FlightRoute ROUTE = FlightRoute.valueOf(
            RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS"));
    private static final FlightDesignator DESIGNATOR = FlightDesignator.of("TP", 123, null);

    @Mock private RepositoryFactory repositories;
    @Mock private FlightRepository flightRepository;
    @Mock private AircraftRepository aircraftRepository;
    @Mock private aiSafe.airport.repositories.AirportRepository airportRepository;
    @Mock private FlightPlanSimulator simulator;
    @Mock private AircraftSimulationParametersResolver parametersResolver;

    private ValidateFlightPlanService service;

    private static final SimulatorModelParameters TEST_PARAMETERS = new SimulatorModelParameters(
            122,
            9,
            0.84,
            42000,
            15000,
            0.03,
            243,
            0.8,
            100.0,
            69.0,
            69.0,
            List.of(new ActiveEngineParameters(80000, 1.6e-4)));

    @BeforeEach
    void setUp() {
        when(repositories.flights()).thenReturn(flightRepository);
        service = new ValidateFlightPlanService(repositories, simulator, parametersResolver);
    }

    private void stubSimulationSuccess() {
        when(repositories.aircrafts()).thenReturn(aircraftRepository);
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft()));
        when(parametersResolver.resolve(any(), any())).thenReturn(Optional.of(TEST_PARAMETERS));
        when(simulator.simulate(any(), any(), any(), any()))
                .thenReturn(FlightPlanSimulationResult.success(500_000, 10_000));
    }

    private void stubSimulationInsufficientFuel() {
        when(repositories.aircrafts()).thenReturn(aircraftRepository);
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft()));
        when(parametersResolver.resolve(any(), any())).thenReturn(Optional.of(TEST_PARAMETERS));
        when(simulator.simulate(any(), any(), any(), any()))
                .thenReturn(FlightPlanSimulationResult.insufficientFuel(100, 50_000));
    }

    @Test
    void validate_sets_validated_on_success() {
        final Flight flight = flightWithDraftPlan(validDsl(500_000), 500_000);
        stubSimulationSuccess();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        final ValidateFlightPlanResult result = service.validate(DESIGNATOR);

        assertTrue(result.isSuccess(), () -> result.formattedErrors().toString());
        assertEquals(FlightPlanStatus.VALIDATED, flight.flightPlan().status());
        assertTrue(result.summary().isPresent());
        assertEquals("TP123", result.summary().orElseThrow().flightDesignator());
        assertEquals(10_000, result.summary().orElseThrow().requiredFuelKg());
        verify(flightRepository).save(flight);
    }

    @Test
    void validate_rejects_incomplete_dsl() {
        final Flight flight = flightWithDraftPlan("flight TP123 {", 500_000);
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final ValidateFlightPlanResult result = service.validate(DESIGNATOR);

        assertFalse(result.isSuccess());
        assertEquals(ValidateFlightPlanErrorCode.DSL_VALIDATION, result.errors().getFirst().code);
        assertEquals(FlightPlanStatus.DRAFT, flight.flightPlan().status());
        verify(flightRepository, never()).save(any());
    }

    @Test
    void validate_rejects_when_already_validated() throws Exception {
        final Flight flight = flightWithDraftPlan(validDsl(500_000), 500_000);
        setStatus(flight.flightPlan(), FlightPlanStatus.VALIDATED);
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final ValidateFlightPlanResult result = service.validate(DESIGNATOR);

        assertFalse(result.isSuccess());
        assertEquals(ValidateFlightPlanErrorCode.PLAN_ALREADY_VALIDATED, result.errors().getFirst().code);
        verify(flightRepository, never()).save(any());
    }

    @Test
    void validate_rejects_departure_mismatch() {
        final Flight flight = new Flight(
                ROUTE, DESIGNATOR, FlightType.REGULAR, AircraftRegistration.valueOf("CS-TUA"));
        final String dsl = validDsl(500_000);
        final DepartureDateTime wrongDeparture =
                new DepartureDateTime(LocalDateTime.now().plusDays(10).withMinute(0).withSecond(0).withNano(0));
        flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                wrongDeparture,
                new ArrivalDateTime(wrongDeparture.value().plusHours(1), wrongDeparture.value()),
                new FuelQuantity(500_000, "kg"),
                new DslSource(dsl));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final ValidateFlightPlanResult result = service.validate(DESIGNATOR);

        assertFalse(result.isSuccess());
        assertEquals(ValidateFlightPlanErrorCode.DEPARTURE_MISMATCH, result.errors().getFirst().code);
        assertEquals(FlightPlanStatus.DRAFT, flight.flightPlan().status());
        verify(flightRepository, never()).save(any());
    }

    @Test
    void validate_rejects_insufficient_fuel() throws Exception {
        final Flight flight = flightWithDraftPlan(validDsl(100), 100);
        stubSimulationInsufficientFuel();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final ValidateFlightPlanResult result = service.validate(DESIGNATOR);

        assertFalse(result.isSuccess());
        assertEquals(ValidateFlightPlanErrorCode.INSUFFICIENT_FUEL, result.errors().getFirst().code);
        assertEquals(FlightPlanStatus.DRAFT, flight.flightPlan().status());
        verify(flightRepository, never()).save(any());
    }

    @Test
    void validate_rejects_when_aircraft_not_found() {
        final Flight flight = flightWithDraftPlan(validDsl(500_000), 500_000);
        when(repositories.aircrafts()).thenReturn(aircraftRepository);
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.empty());

        final ValidateFlightPlanResult result = service.validate(DESIGNATOR);

        assertFalse(result.isSuccess());
        assertEquals(ValidateFlightPlanErrorCode.AIRCRAFT_NOT_FOUND, result.errors().getFirst().code);
        verify(flightRepository, never()).save(any());
    }

    @Test
    void validate_rejects_when_no_plan() {
        final Flight flight = new Flight(
                ROUTE, DESIGNATOR, FlightType.REGULAR, AircraftRegistration.valueOf("CS-TUA"));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final ValidateFlightPlanResult result = service.validate(DESIGNATOR);

        assertFalse(result.isSuccess());
        assertEquals(ValidateFlightPlanErrorCode.NO_DRAFT_PLAN, result.errors().getFirst().code);
    }

    @Test
    void validate_rejects_plan_not_found() {
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.empty());

        final ValidateFlightPlanResult result = service.validate(DESIGNATOR);

        assertFalse(result.isSuccess());
        assertEquals(ValidateFlightPlanErrorCode.FLIGHT_NOT_FOUND, result.errors().getFirst().code);
    }

    private static Aircraft aircraft() {
        return aircraft(engineModel());
    }

    private static Aircraft aircraft(final EngineModel engine) {
        final Maker maker = engine.maker();
        final PhysicalSpecs specs = new PhysicalSpecs(
                new Altitude(11000, AltitudeUnit.METERS),
                new Speed(450, SpeedUnit.KMH),
                new Area(122, AreaUnit.m2),
                new Weight(42000, WeightUnit.KG),
                new Weight(79000, WeightUnit.KG),
                new Weight(65000, WeightUnit.KG),
                new Weight(26000, WeightUnit.KG),
                new LiftCoefficient(1.2),
                new DragCoefficient(0.03));
        final AircraftModel model = new AircraftModel(
                new AircraftModelId("A320-001"),
                new AircraftModelName("A320"),
                maker,
                List.of(engine),
                specs,
                AircraftType.PASSENGER,
                new MaximumSeats(180));
        final AirTransportCompany company = new AirTransportCompany(
                CompanyName.valueOf("TAP Air Portugal"),
                IataCode.valueOf("TP"),
                IcaoCode.valueOf("TAP"));
        return new Aircraft(
                AircraftRegistration.valueOf("CS-TUA"),
                model,
                List.of(CurrentEngine.valueOf("CFM56-7B")),
                new CabinConfiguration(120, 20, 10),
                new aiSafe.aircraft.domain.RegistrationYear(2018),
                new Country("Portugal", 351),
                AircraftStatus.ACTIVE,
                company);
    }

    private static EngineModel engineModel() {
        final Maker maker = new Maker(new MakerID(123L), new MakerName("Airbus"), new Country("France", 33));
        final FuelType fuelType = new FuelType(
                new FuelTypeName("JET A-1"),
                new FuelTypeDensity(0.8),
                new FuelTypeSpecificEnergy(43.0));
        return new EngineModel(
                new EngineModelName("CFM56-7B"),
                new Power(27000, Unit.HORSEPOWER),
                new TSFC(1.6e-4),
                fuelType,
                MotorizationType.TURBOFAN,
                maker);
    }

    private static Flight flightWithDraftPlan(final String dsl, final int fuelKg) {
        final Flight flight = new Flight(
                ROUTE, DESIGNATOR, FlightType.REGULAR, AircraftRegistration.valueOf("CS-TUA"));
        final DepartureDateTime departure = dslDepartureForTest(dsl);
        flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                departure,
                new ArrivalDateTime(departure.value().plusHours(1), departure.value()),
                new FuelQuantity(fuelKg, "kg"),
                new DslSource(dsl));
        return flight;
    }

    private static void setStatus(final FlightPlan plan, final FlightPlanStatus status) throws Exception {
        final var field = FlightPlan.class.getDeclaredField("status");
        field.setAccessible(true);
        field.set(plan, status);
    }

    private static DepartureDateTime dslDepartureForTest(final String dsl) {
        final LocalDateTime departure = dslFirstLegDeparture(dsl);
        if (departure.isBefore(LocalDateTime.now())) {
            return DepartureDateTime.fromDslImport(departure);
        }
        return new DepartureDateTime(departure);
    }

    private static LocalDateTime dslFirstLegDeparture(final String dsl) {
        final int idx = dsl.indexOf("departure OPO ");
        if (idx < 0) {
            return LocalDateTime.now().plusDays(5);
        }
        final String tail = dsl.substring(idx + "departure OPO ".length()).trim();
        final String date = tail.substring(0, 10);
        final String time = tail.substring(11, 16);
        return LocalDateTime.parse(date + "T" + time);
    }

    private static String validDsl(final int fuelKg) {
        final LocalDateTime dep = LocalDateTime.now().plusDays(30).withHour(8).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime arr = dep.plusHours(1);
        final String depDate = dep.toLocalDate().toString();
        final String arrDate = arr.toLocalDate().toString();
        final String depTime = String.format("%02d:%02d", dep.getHour(), dep.getMinute());
        final String arrTime = String.format("%02d:%02d", arr.getHour(), arr.getMinute());
        return """
                flight TP123 {
                    type REGULAR
                    leg {
                        departure OPO %s %s
                        arrival LIS %s %s
                        route { origin OPO destination LIS }
                        segment {
                            start (41.242, -8.678)
                            end (38.774, -9.134)
                            altitude 12000 m
                            width 10 km
                            wind 0 deg 0 kt
                        }
                        fuel %d kg
                    }
                }
                """.formatted(depDate, depTime, arrDate, arrTime, fuelKg);
    }
}
