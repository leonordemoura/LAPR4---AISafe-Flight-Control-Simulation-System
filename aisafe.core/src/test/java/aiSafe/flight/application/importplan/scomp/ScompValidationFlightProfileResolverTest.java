package aiSafe.flight.application.importplan.scomp;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.domain.AircraftStatus;
import aiSafe.aircraft.domain.CabinConfiguration;
import aiSafe.aircraft.domain.CurrentEngine;
import aiSafe.aircraftmanagement.domain.*;
import aiSafe.aircraftModel.domain.*;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.flight.application.validate.simulator.AircraftSimulationParametersResolver;
import aiSafe.flight.application.validate.simulator.FlightValidationProfileCatalog;
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
class ScompValidationFlightProfileResolverTest {

    @Mock
    private AircraftSimulationParametersResolver parametersResolver;

    private ScompValidationFlightProfileResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ScompValidationFlightProfileResolver(parametersResolver);
    }

    @Test
    void resolve_uses_validation_climb_descend_and_aircraft_cruise_speed() {
        final aiSafe.flight.domain.Flight flight = mock(aiSafe.flight.domain.Flight.class);
        when(parametersResolver.resolve(any(), any())).thenReturn(Optional.of(
                new aiSafe.flight.application.validate.simulator.SimulatorModelParameters(
                        122, 9, 0.84, 42000, 15000, 0.03, 447.0, 0.8, 100.0, 69.0, 69.0,
                        List.of(new aiSafe.flight.application.validate.simulator.ActiveEngineParameters(80000, 1.6e-4)))));

        final Optional<ScompValidationFlightProfile> profile = resolver.resolve(flight, aircraft());

        assertTrue(profile.isPresent());
        final FlightValidationProfileCatalog validationProfiles = FlightValidationProfileCatalog.defaults();
        assertEquals(validationProfiles.climb().getFirst().rateMetersPerSecond(),
                profile.get().climb().getFirst().rateMetersPerSecond());
        assertTrue(profile.get().descend().getFirst().rateMetersPerSecond() < 0);
        assertEquals(447.0, profile.get().cruiseSpeedKnots());
    }

    @Test
    void toScompDescentJsonSteps_negates_positive_validation_rates() {
        final List<FlightProfileStep> steps = ScompValidationFlightProfileResolver.toScompDescentJsonSteps(
                List.of(new FlightProfileStep(12000, 300, 10.0)));

        assertEquals(-10.0, steps.getFirst().rateMetersPerSecond());
    }

    private static Aircraft aircraft() {
        final Maker maker = new Maker(new MakerID(123L), new MakerName("Airbus"), new Country("France", 33));
        final FuelType fuelType = new FuelType(
                new FuelTypeName("JET A-1"),
                new FuelTypeDensity(0.8),
                new FuelTypeSpecificEnergy(43.0));
        final EngineModel engine = new EngineModel(
                new EngineModelName("CFM56-7B"),
                new Power(27000, Unit.HORSEPOWER),
                new TSFC(1.6e-4),
                fuelType,
                MotorizationType.TURBOFAN,
                maker);
        final PhysicalSpecs specs = new PhysicalSpecs(
                new Altitude(11000, AltitudeUnit.METERS),
                new Speed(828, SpeedUnit.KMH),
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
}
