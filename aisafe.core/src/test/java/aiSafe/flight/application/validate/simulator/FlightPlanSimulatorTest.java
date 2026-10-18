package aiSafe.flight.application.validate.simulator;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.domain.AircraftStatus;
import aiSafe.aircraft.domain.CabinConfiguration;
import aiSafe.aircraft.domain.CurrentEngine;
import aiSafe.aircraft.domain.RegistrationYear;
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
import aiSafe.airport.repositories.AirportRepository;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FuelQuantity;
import aiSafe.flight.dsl.FlightDslValidationResult;
import aiSafe.flight.dsl.FlightDslValidator;
import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.weatherdata.domain.ObservationDate;
import aiSafe.weatherdata.domain.Pressure;
import aiSafe.weatherdata.domain.PressureUnit;
import aiSafe.weatherdata.domain.Temperature;
import aiSafe.weatherdata.domain.TemperatureUnit;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WeatherSource;
import aiSafe.weatherdata.domain.WeatherSubArea;
import aiSafe.weatherdata.domain.WindDirection;
import aiSafe.weatherdata.domain.WindSpeed;
import aiSafe.weatherdata.domain.WindSpeedUnit;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlightPlanSimulatorTest {

    private static final EngineModel CFM56 = engineModel("CFM56-7B", 27000, 1.6e-4);

    private FlightPlanSimulator simulator;
    private SimulatorModelParameters a320Parameters;

    @BeforeEach
    void setUp() {
        simulator = new FlightPlanSimulator();
        final AirportRepository airportRepo = mock(AirportRepository.class);
        final Flight flight = mock(Flight.class);
        final FlightRoute route = mock(FlightRoute.class);
        final RouteDesignator designator = mock(RouteDesignator.class);

        when(flight.route()).thenReturn(route);
        when(route.routeDesignator()).thenReturn(designator);
        when(designator.origin()).thenReturn("OPO");
        when(designator.destination()).thenReturn("LIS");

        a320Parameters = new AircraftSimulationParametersResolver(new StubEngineRepo(CFM56), airportRepo)
                .resolve(flight, a320Aircraft())
                .orElseThrow();
    }

    @Test
    void simulate_succeeds_with_sufficient_fuel() {
        final DslFlightPlan plan = parse(opoLisDsl(9000, 3200));
        final FlightPlanSimulationResult result =
                simulator.simulate(plan, new FuelQuantity(3200, "kg"), a320Parameters);

        assertTrue(result.isSuccess(), () -> result.message().orElse(""));
        assertTrue(result.requiredFuelKg() > 0);
        assertTrue(result.requiredFuelKg() < 3200);
    }

    @Test
    void simulate_fails_with_insufficient_fuel() {
        final DslFlightPlan plan = parse(opoLisDsl(9000, 3200));
        final FlightPlanSimulationResult result =
                simulator.simulate(plan, new FuelQuantity(100, "kg"), a320Parameters);

        assertFalse(result.isSuccess());
        assertTrue(result.requiredFuelKg() > result.availableFuelKg());
    }

    @Test
    void simulate_accepts_liters() {
        final DslFlightPlan plan = parse(opoLisDsl(9000, 3200));
        final double liters = 3200 / 0.804;
        final FlightPlanSimulationResult result =
                simulator.simulate(plan, new FuelQuantity(liters, "l"), a320Parameters);

        assertTrue(result.isSuccess(), () -> result.message().orElse(""));
    }

    @Test
    void simulate_uses_active_engine_count_from_aircraft() {
        assertEquals(2, a320Parameters.activeEngineCount());
    }

    @Test
    void simulate_multi_segment_keeps_altitude_between_segments() {
        final DslFlightPlan plan = parse(multiSegmentDsl());
        final FlightPlanSimulationResult withoutContinuousProfile = new FlightPlanSimulator(
                new FlightValidationProfileCatalog(SimulationCalibration.OPERATIONAL),
                SimulationCalibration.OPERATIONAL)
                .simulate(plan, new FuelQuantity(5000, "kg"), a320Parameters);

        assertTrue(withoutContinuousProfile.isSuccess(), () -> withoutContinuousProfile.message().orElse(""));
        assertTrue(withoutContinuousProfile.requiredFuelKg() < 5000);
        assertTrue(withoutContinuousProfile.requiredFuelKg() > 1000);
    }

    @Test
    void simulate_with_weather_changes_required_fuel() {
        final DslFlightPlan plan = parse(opoLisDsl(9000, 3200));
        final FlightPlanSimulationResult withoutWeather =
                simulator.simulate(plan, new FuelQuantity(3200, "kg"), a320Parameters, Optional.empty());
        final FlightPlanSimulationResult withColdHighPressure = simulator.simulate(
                plan,
                new FuelQuantity(3200, "kg"),
                a320Parameters,
                Optional.of(SimulationWeatherConditions.from(sampleWeather(5, 1030, 0, 0))));
        final FlightPlanSimulationResult withWarmLowPressure = simulator.simulate(
                plan,
                new FuelQuantity(3200, "kg"),
                a320Parameters,
                Optional.of(SimulationWeatherConditions.from(sampleWeather(30, 990, 270, 40))));

        assertTrue(withoutWeather.isSuccess());
        assertTrue(withColdHighPressure.isSuccess());
        assertTrue(withWarmLowPressure.isSuccess());
        assertNotEquals(
                withoutWeather.requiredFuelKg(),
                withWarmLowPressure.requiredFuelKg(),
                0.1);
    }

    @Test
    void simulate_headwind_increases_required_fuel() {
        final DslFlightPlan plan = parse(opoLisDsl(9000, 3200));
        final FlightPlanSimulationResult tailwind = simulator.simulate(
                plan,
                new FuelQuantity(3200, "kg"),
                a320Parameters,
                Optional.of(SimulationWeatherConditions.from(sampleWeather(15, 1013, 90, 30))));
        final FlightPlanSimulationResult headwind = simulator.simulate(
                plan,
                new FuelQuantity(3200, "kg"),
                a320Parameters,
                Optional.of(SimulationWeatherConditions.from(sampleWeather(15, 1013, 270, 30))));

        assertTrue(tailwind.requiredFuelKg() < headwind.requiredFuelKg());
    }

    private static DslFlightPlan parse(final String dsl) {
        final FlightDslValidationResult validation = FlightDslValidator.validate(dsl);
        assertTrue(validation.isValid(), validation.errors().toString());
        return validation.model().orElseThrow();
    }

    private static String multiSegmentDsl() {
        return """
                flight TP123 {
                    type REGULAR
                    leg {
                        departure OPO 2026-07-07 10:00
                        arrival LIS 2026-07-07 11:00
                        route { origin OPO destination LIS }
                        segment {
                            start (41.242, -8.678)
                            end (39.5, -9.0)
                            altitude 10000 m
                            width 10 km
                            wind 180 deg 15 kt
                        }
                        segment {
                            start (39.5, -9.0)
                            end (38.774, -9.134)
                            altitude 12000 m
                            width 12 km
                            wind 200 deg 20 kt
                        }
                        fuel 5000 kg
                    }
                }
                """;
    }

    private static WeatherData sampleWeather(
            final int temperatureC,
            final int pressureHpa,
            final int windDirectionDeg,
            final int windSpeedKt
    ) {
        return new WeatherData(
                new AreaCode("LPPT"),
                new ObservationDate(
                        LocalDateTime.of(2026, 7, 7, 10, 0),
                        LocalDateTime.of(2026, 7, 7, 11, 0)),
                new WeatherSource("METAR"),
                new WeatherSubArea(38.0, 39.0, -9.5, -8.5, 0, 2000),
                new WindDirection(windDirectionDeg),
                new WindSpeed(windSpeedKt, WindSpeedUnit.KT),
                new Temperature(temperatureC, TemperatureUnit.C),
                new Pressure(pressureHpa, PressureUnit.HPA));
    }

    private static String opoLisDsl(final int altitudeMeters, final int legFuelKg) {
        return """
                flight TP123 {
                    type REGULAR
                    leg {
                        departure OPO 2026-07-07 10:00
                        arrival LIS 2026-07-07 11:00
                        route { origin OPO destination LIS }
                        segment {
                            start (41.242, -8.678)
                            end (38.774, -9.134)
                            altitude %d m
                            width 10 km
                            wind 270 deg 10 kt
                        }
                        fuel %d kg
                    }
                }
                """.formatted(altitudeMeters, legFuelKg);
    }

    private static Aircraft a320Aircraft() {
        final Maker maker = new Maker(new MakerID(123L), new MakerName("Airbus"), new Country("France", 33));
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
                List.of(CFM56),
                specs,
                AircraftType.PASSENGER,
                new MaximumSeats(180));
        return new Aircraft(
                AircraftRegistration.valueOf("CS-TUA"),
                model,
                List.of(CurrentEngine.valueOf("CFM56-7B"), CurrentEngine.valueOf("CFM56-7B")),
                new CabinConfiguration(120, 20, 10),
                new RegistrationYear(2018),
                new Country("Portugal", 351),
                AircraftStatus.ACTIVE,
                new AirTransportCompany(
                        CompanyName.valueOf("TAP Air Portugal"),
                        IataCode.valueOf("TP"),
                        IcaoCode.valueOf("TAP")));
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

    private static final class StubEngineRepo implements EngineModelRepository {
        private final EngineModel engine;

        private StubEngineRepo(final EngineModel engine) {
            this.engine = engine;
        }

        @Override
        public Optional<EngineModel> findByModelNameAndMaker(final EngineModelName modelName, final Maker maker) {
            return Optional.of(engine);
        }

        @Override
        public Optional<EngineModel> ofIdentity(final Long id) {
            return Optional.empty();
        }

        @Override
        public Iterable<EngineModel> findAll() {
            return List.of(engine);
        }

        @Override
        public EngineModel save(final EngineModel entity) {
            return entity;
        }

        @Override
        public void delete(final EngineModel entity) {
        }

        @Override
        public void deleteOfIdentity(final Long entityId) {
        }

        @Override
        public long count() {
            return 1;
        }
    }
}
