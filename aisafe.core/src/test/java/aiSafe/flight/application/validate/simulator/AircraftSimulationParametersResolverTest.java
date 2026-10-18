package aiSafe.flight.application.validate.simulator;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.domain.AircraftStatus;
import aiSafe.aircraft.domain.CabinConfiguration;
import aiSafe.aircraft.domain.CurrentEngine;
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
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AircraftSimulationParametersResolverTest {

    @Mock
    private EngineModelRepository engineModelRepository;
    @Mock
    private aiSafe.airport.repositories.AirportRepository airportRepository;
    @Mock
    private aiSafe.flight.domain.Flight flight;

    private AircraftSimulationParametersResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new AircraftSimulationParametersResolver(engineModelRepository, airportRepository);
    }

    @Test
    void resolve_uses_active_engines_only() {
        final EngineModel certified = engineModel("CFM56-7B", 27000, 1.6e-4);
        final Aircraft aircraft = aircraftWithActiveEngines(List.of("CFM56-7B", "CFM56-7B"), certified);

        final aiSafe.flightRoute.domain.FlightRoute route = mock(aiSafe.flightRoute.domain.FlightRoute.class);
        final aiSafe.flightRoute.domain.RouteDesignator designator = mock(aiSafe.flightRoute.domain.RouteDesignator.class);
        when(flight.route()).thenReturn(route);
        when(route.routeDesignator()).thenReturn(designator);
        when(designator.origin()).thenReturn("OPO");
        when(designator.destination()).thenReturn("LIS");

        when(engineModelRepository.findByModelNameAndMaker(any(), any())).thenReturn(Optional.of(certified));

        final Optional<SimulatorModelParameters> parameters = resolver.resolve(flight, aircraft);

        assertTrue(parameters.isPresent());
        assertEquals(2, parameters.get().activeEngineCount());
        assertEquals(122.0, parameters.get().wingAreaM2());
        assertEquals(10500.0, parameters.get().payloadKg());
        assertEquals(0.8, parameters.get().fuelDensityKgPerL());
    }

    @Test
    void resolve_rejects_uncertified_active_engine() {
        final EngineModel certified = engineModel("CFM56-7B", 27000, 1.6e-4);
        final EngineModel uncertified = engineModel("OTHER", 20000, 1.6e-4);
        final Aircraft aircraft = aircraftWithActiveEngines(List.of("OTHER"), certified);

        when(engineModelRepository.findByModelNameAndMaker(any(), any())).thenReturn(Optional.of(uncertified));

        assertTrue(resolver.resolve(flight, aircraft).isEmpty());
        assertTrue(resolver.resolveFailureMessage(aircraft).orElse("").contains("not certified"));
    }

    private static EngineModel engineModel(final String name, final int powerHp, final double tsfc) {
        final Maker maker = new Maker(new MakerID(123L), new MakerName("Airbus"), new Country("France", 33));
        final FuelType fuelType = new FuelType(
                new FuelTypeName("JET A-1"),
                new FuelTypeDensity(0.8),
                new FuelTypeSpecificEnergy(43.0));
        return new EngineModel(
                new EngineModelName(name),
                new Power(powerHp, Unit.HORSEPOWER),
                new TSFC(tsfc),
                fuelType,
                MotorizationType.TURBOFAN,
                maker);
    }

    private static Aircraft aircraftWithActiveEngines(
            final List<String> activeEngineRefs,
            final EngineModel certifiedEngine
    ) {
        final Maker maker = certifiedEngine.maker();
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
                List.of(certifiedEngine),
                specs,
                AircraftType.PASSENGER,
                new MaximumSeats(180));
        final AirTransportCompany company = new AirTransportCompany(
                CompanyName.valueOf("TAP Air Portugal"),
                IataCode.valueOf("TP"),
                IcaoCode.valueOf("TAP"));
        final List<CurrentEngine> activeEngines = activeEngineRefs.stream()
                .map(CurrentEngine::valueOf)
                .toList();
        return new Aircraft(
                AircraftRegistration.valueOf("CS-TUA"),
                model,
                activeEngines,
                new CabinConfiguration(120, 20, 10),
                new aiSafe.aircraft.domain.RegistrationYear(2018),
                new Country("Portugal", 351),
                AircraftStatus.ACTIVE,
                company);
    }
}
