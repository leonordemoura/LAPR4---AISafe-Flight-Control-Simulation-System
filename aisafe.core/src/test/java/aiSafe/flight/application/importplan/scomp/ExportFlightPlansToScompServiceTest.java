package aiSafe.flight.application.importplan.scomp;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.flight.application.validate.simulator.FlightValidationProfileCatalog;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportFlightPlansToScompServiceTest {

    private static final FlightRoute ROUTE = FlightRoute.valueOf(
            RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS"));
    private static final FlightDesignator DESIGNATOR = FlightDesignator.of("TP", 123, null);

    @Mock
    private FlightRepository flightRepository;
    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private ScompValidationFlightProfileResolver profileResolver;

    @TempDir
    private Path tempDir;

    private ExportFlightPlansToScompService service;

    @BeforeEach
    void setUp() {
        System.setProperty("aisafe.scomp.json.dir", tempDir.toString());
        service = new ExportFlightPlansToScompService(
                flightRepository,
                aircraftRepository,
                profileResolver,
                new ScompFlightPlanJsonGenerator(),
                new ScompFlightPlanJsonWriter());
    }

    @Test
    void validatedFlights_returns_only_validated_plans() throws Exception {
        final Flight validated = flightWithPlan(validDsl(), FlightPlanStatus.VALIDATED);
        final Flight draft = flightWithPlan(validDsl(), FlightPlanStatus.DRAFT);
        when(flightRepository.findAll()).thenReturn(List.of(validated, draft));

        final List<Flight> result = service.validatedFlights();

        assertEquals(1, result.size());
        assertEquals(DESIGNATOR, result.getFirst().designator());
    }

    @Test
    void export_writes_single_flight_json() throws Exception {
        final Flight flight = flightWithPlan(validDsl(), FlightPlanStatus.VALIDATED);
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(mockAircraftPlaceholder()));
        stubValidationProfile();

        final ExportFlightPlansToScompResult result = service.export(List.of(DESIGNATOR));

        assertTrue(result.isSuccess(), String.join("; ", result.errors()));
        assertNotNull(result.outputPath());
        assertTrue(result.outputPath().getFileName().toString().contains("Flight_Plan_TP123"));
        assertEquals(List.of("TP123"), result.exportedFlightDesignators());
    }

    @Test
    void export_writes_multi_flight_json() throws Exception {
        final FlightDesignator secondDesignator = FlightDesignator.of("TP", 456, null);
        final FlightRoute secondRoute = FlightRoute.valueOf(
                RouteName.valueOf("TP", 456), RouteDesignator.valueOf("LIS", "OPO"));

        final Flight first = flightWithPlan(validDsl(), FlightPlanStatus.VALIDATED);
        final Flight second = flightWithDesignator(secondRoute, secondDesignator, validDslFor("TP456"), FlightPlanStatus.VALIDATED);

        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(first));
        when(flightRepository.findByDesignator(secondDesignator)).thenReturn(Optional.of(second));
        when(aircraftRepository.findByRegistration(any())).thenReturn(Optional.of(mockAircraftPlaceholder()));
        stubValidationProfile();

        final ExportFlightPlansToScompResult result = service.export(List.of(DESIGNATOR, secondDesignator));

        assertTrue(result.isSuccess(), String.join("; ", result.errors()));
        assertTrue(result.outputPath().getFileName().toString().matches("Flight_Plan_\\d{8}_\\d{6}\\.json"));
        assertEquals(List.of("TP123", "TP456"), result.exportedFlightDesignators());
    }

    @Test
    void export_fails_when_plan_is_not_validated() throws Exception {
        final Flight flight = flightWithPlan(validDsl(), FlightPlanStatus.DRAFT);
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final ExportFlightPlansToScompResult result = service.export(List.of(DESIGNATOR));

        assertFalse(result.isSuccess());
        assertTrue(result.errors().getFirst().contains("no validated flight plan"));
    }

    @Test
    void export_fails_when_validation_profile_cannot_be_resolved() throws Exception {
        final Flight flight = flightWithPlan(validDsl(), FlightPlanStatus.VALIDATED);
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(mockAircraftPlaceholder()));
        when(profileResolver.resolve(any(), any())).thenReturn(Optional.empty());
        when(profileResolver.failureMessage(any())).thenReturn(Optional.of("No active engines"));

        final ExportFlightPlansToScompResult result = service.export(List.of(DESIGNATOR));

        assertFalse(result.isSuccess());
        assertTrue(result.errors().getFirst().contains("No active engines"));
    }

    private void stubValidationProfile() {
        final FlightValidationProfileCatalog validationProfiles = FlightValidationProfileCatalog.defaults();
        when(profileResolver.resolve(any(), any())).thenReturn(Optional.of(new ScompValidationFlightProfile(
                validationProfiles.climb(),
                ScompValidationFlightProfileResolver.toScompDescentJsonSteps(validationProfiles.descend()),
                447.0)));
    }

    private static Aircraft mockAircraftPlaceholder() {
        return org.mockito.Mockito.mock(Aircraft.class);
    }

    private static Flight flightWithPlan(final String dsl, final FlightPlanStatus status) throws Exception {
        return flightWithDesignator(ROUTE, DESIGNATOR, dsl, status);
    }

    private static Flight flightWithDesignator(
            final FlightRoute route,
            final FlightDesignator designator,
            final String dsl,
            final FlightPlanStatus status
    ) throws Exception {
        final Flight flight = new Flight(
                route, designator, FlightType.REGULAR, AircraftRegistration.valueOf("CS-TUA"));
        final LocalDateTime departure = LocalDateTime.now().plusDays(30).withHour(8).withMinute(0).withSecond(0).withNano(0);
        flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                new DepartureDateTime(departure),
                new ArrivalDateTime(departure.plusHours(1), departure),
                new FuelQuantity(5000, "kg"),
                new DslSource(dsl));
        setStatus(flight.flightPlan(), status);
        return flight;
    }

    private static void setStatus(final FlightPlan plan, final FlightPlanStatus status) throws Exception {
        final var field = FlightPlan.class.getDeclaredField("status");
        field.setAccessible(true);
        field.set(plan, status);
    }

    private static String validDsl() {
        return validDslFor("TP123");
    }

    private static String validDslFor(final String identifier) {
        final LocalDateTime dep = LocalDateTime.now().plusDays(30).withHour(8).withMinute(0).withSecond(0).withNano(0);
        final LocalDateTime arr = dep.plusHours(1);
        final String depDate = dep.toLocalDate().toString();
        final String arrDate = arr.toLocalDate().toString();
        final String depTime = String.format("%02d:%02d", dep.getHour(), dep.getMinute());
        final String arrTime = String.format("%02d:%02d", arr.getHour(), arr.getMinute());
        return """
                flight %s {
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
                        fuel 5000 kg
                    }
                }
                """.formatted(identifier, depDate, depTime, arrDate, arrTime);
    }
}
