package aiSafe.flight.application;

import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.PilotCertification.repositories.PilotCertificationRepository;
import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterFlightPlanServiceTest {

    private static final FlightRoute ROUTE = FlightRoute.valueOf(
            RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS"));
    private static final FlightDesignator DESIGNATOR = new FlightDesignator("TP", 123, null);
    @Mock private RepositoryFactory repositories;
    @Mock private TransactionalContext tx;
    @Mock private FlightRepository flightRepository;
    @Mock private AircraftRepository aircraftRepository;
    @Mock private PilotCertificationRepository pilotCertificationRepository;
    @Mock private PilotCertification pilotCertification;
    @Mock private AirTransportCompany pilotCompany;
    @Mock private AirTransportCompany aircraftCompany;
    @Mock private Aircraft aircraft;

    private RegisterFlightPlanService service;

    @BeforeEach
    void setUp() {
        when(repositories.flights(tx)).thenReturn(flightRepository);
        when(repositories.aircrafts(tx)).thenReturn(aircraftRepository);
        when(repositories.pilots(tx)).thenReturn(pilotCertificationRepository);
        service = new RegisterFlightPlanService(repositories);
        stubMatchingCompanies();
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(flightRepository.hasOverlappingScheduleForPilot(any(), any(), any(), any(), any()))
                .thenReturn(false);
    }

    private static Flight newFlight() {
        return new Flight(
                ROUTE, DESIGNATOR, FlightType.REGULAR, AircraftRegistration.valueOf("CS-TUA"));
    }

    private static LocalDateTime futureDeparture() {
        return LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
    }

    private void stubMatchingCompanies() {
        when(pilotCertification.isActive()).thenReturn(true);
        when(pilotCertification.company()).thenReturn(pilotCompany);
        when(pilotCompany.iataCode()).thenReturn(IataCode.valueOf("TP"));
        when(pilotCertification.identity()).thenReturn(new PilotID("P-001"));
        when(aircraft.isActive()).thenReturn(true);
        when(aircraft.company()).thenReturn(aircraftCompany);
        when(aircraftCompany.iataCode()).thenReturn(IataCode.valueOf("TP"));
    }

    @Test
    void validateFlight_returns_flight_when_found() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final Flight result = service.validateFlight("TP", 123, null, tx);

        assertSame(flight, result);
    }

    @Test
    void validateFlight_throws_when_route_not_active() {
        final FlightRoute inactiveRoute = FlightRoute.valueOf(
                RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS"));
        inactiveRoute.deactivate(LocalDate.now().atStartOfDay());
        final Flight flight = new Flight(
                inactiveRoute, DESIGNATOR, FlightType.REGULAR, AircraftRegistration.valueOf("CS-TUA"));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validateFlight("TP", 123, null, tx));

        assertEquals("Flight route is not active.", ex.getMessage());
    }

    @Test
    void validateFlight_throws_when_current_plan_is_validated() throws Exception {
        final Flight flight = newFlight();
        final LocalDateTime departure = futureDeparture();
        final FlightPlan existing = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                new DepartureDateTime(departure),
                new ArrivalDateTime(departure.plusHours(1), departure),
                new FuelQuantity(1000, "KG"),
                RegisterFlightPlanTestFixtures.sampleDslSource());
        setPlanStatus(existing, FlightPlanStatus.VALIDATED);

        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.validateFlight("TP", 123, null, tx));

        assertTrue(ex.getMessage().contains("VALIDATED"));
    }

    @Test
    void registerFlightPlan_throws_when_pilot_schedule_overlaps() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(flightRepository.hasOverlappingScheduleForPilot(any(), any(), any(), any(), any()))
                .thenReturn(true);

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerFlightPlan(
                        RegisterFlightPlanTestFixtures.dtoWithLegs(),
                        tx));

        assertTrue(ex.getMessage().contains("overlaps"));
        verify(flightRepository, never()).save(any());
    }

    @Test
    void registerFlightPlan_throws_when_current_plan_is_validated() throws Exception {
        final Flight flight = newFlight();
        final LocalDateTime departure = futureDeparture();
        final FlightPlan existing = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                new DepartureDateTime(departure),
                new ArrivalDateTime(departure.plusHours(1), departure),
                new FuelQuantity(1000, "KG"),
                RegisterFlightPlanTestFixtures.sampleDslSource());
        setPlanStatus(existing, FlightPlanStatus.VALIDATED);

        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.registerFlightPlan(RegisterFlightPlanTestFixtures.dtoWithLegs(), tx));

        assertTrue(ex.getMessage().contains("VALIDATED"));
        verify(flightRepository, never()).save(any());
    }

    @Test
    void registerFlightPlan_replaces_draft_and_cancels_previous() {
        final Flight flight = newFlight();
        final LocalDateTime departure = futureDeparture();
        flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                new DepartureDateTime(departure),
                new ArrivalDateTime(departure.plusHours(1), departure),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP123 { type REGULAR }"));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registerFlightPlan(RegisterFlightPlanTestFixtures.dtoWithLegs(), tx);

        assertEquals(2, flight.flightPlans().size());
        assertEquals(FlightPlanStatus.CANCELLED, flight.flightPlans().get(0).status());
        assertEquals(FlightPlanStatus.DRAFT, flight.flightPlan().status());
    }

    @Test
    void registerFlightPlan_throws_when_route_inactive_at_departure() {
        final FlightRoute route = FlightRoute.valueOf(
                RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS"));
        route.deactivate(LocalDate.now().plusDays(3).atStartOfDay());
        final Flight flight = new Flight(
                route, DESIGNATOR, FlightType.REGULAR, AircraftRegistration.valueOf("CS-TUA"));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));

        final LocalDateTime departure = LocalDateTime.now().plusDays(10);
        final String date = departure.toLocalDate().toString();
        final RegisterFlightPlanLegInput leg = new RegisterFlightPlanLegInput(
                "OPO", date, "08:00",
                "LIS", date, "12:00",
                "OPO", "LIS",
                1500, "kg",
                List.of(RegisterFlightPlanTestFixtures.sampleSegment()));

        final RegisterFlightPlanDTO dto = new RegisterFlightPlanDTO(
                "TP", 123, null,
                "P-001",
                departure,
                departure.plusHours(4),
                1500, "kg",
                null,
                List.of(leg),
                "REGULAR"
        );

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerFlightPlan(dto, tx));

        assertEquals("Flight route is not active.", ex.getMessage());
    }

    @Test
    void validateFlight_throws_when_not_found() {
        when(flightRepository.findByDesignator(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.validateFlight("TP", 999, null, tx));
    }

    @Test
    void validateFlight_treats_blank_suffix_as_null() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(new FlightDesignator("TP", 123, null)))
                .thenReturn(Optional.of(flight));

        assertSame(flight, service.validateFlight("TP", 123, "  ", tx));
    }

    @Test
    void validateFlight_uses_operational_suffix_when_provided() {
        final Flight flight = newFlight();
        final FlightDesignator withSuffix = new FlightDesignator("TP", 123, "A");
        when(flightRepository.findByDesignator(withSuffix)).thenReturn(Optional.of(flight));

        assertSame(flight, service.validateFlight("TP", 123, "A", tx));
    }

    @Test
    void registerFlightPlan_uses_pasted_dsl_when_content_not_blank() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithDslContent(
                RegisterFlightPlanTestFixtures.sampleDslText());

        service.registerFlightPlan(dto, tx);

        assertTrue(flight.flightPlan().dslSource().content().startsWith("flight TP123"));
        assertEquals("TP123", flight.flightPlan().dslIdentifier());
        assertEquals("OPO", flight.flightPlan().originAirport());
        assertEquals("LIS", flight.flightPlan().destinationAirport());
    }

    @Test
    void registerFlightPlan_persists_full_dsl_content() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(flightRepository.hasOverlappingScheduleForPilot(any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        final String dsl = RegisterFlightPlanTestFixtures.sampleDslText();
        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithDslContent(dsl);

        service.registerFlightPlan(dto, tx);

        final FlightPlan plan = flight.flightPlan();
        assertTrue(plan.dslSource().content().contains("type REGULAR"));
        assertTrue(plan.dslSource().content().contains("leg {"));
        assertEquals("OPO", plan.originAirport());
        assertEquals("LIS", plan.destinationAirport());
    }

    @Test
    void registerFlightPlan_rejects_incomplete_pasted_dsl() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));

        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithDslContent("flight TP123 {");

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerFlightPlan(dto, tx));

        assertTrue(ex.getMessage().contains("DSL is incomplete"));
    }

    @Test
    void registerFlightPlan_rejects_dsl_flight_mismatch() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));

        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithDslContent("flight TP100 {");

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerFlightPlan(dto, tx));

        assertTrue(ex.getMessage().contains("does not match"));
    }

    @Test
    void registerFlightPlan_rejects_pasted_dsl_without_flight_block() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));

        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithDslContent(
                "leg { departure OPO 2026-06-15 10:00 arrival LIS 2026-06-15 11:00 }");

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registerFlightPlan(dto, tx));

        assertTrue(ex.getMessage().contains("flight block"));
    }

    @Test
    void validateFlightAircraft_returns_aircraft_when_active() {
        final Flight flight = newFlight();
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));

        assertSame(aircraft, service.validateFlightAircraft(flight, tx));
    }

    @Test
    void validateFlightAircraft_throws_when_not_found() {
        final Flight flight = newFlight();
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.validateFlightAircraft(flight, tx));
    }

    @Test
    void ensureFlightAircraftMustBeActive() {
        final Flight flight = newFlight();
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(aircraft.isActive()).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.validateFlightAircraft(flight, tx));
    }

    @Test
    void validatePilot_succeeds_when_active_and_same_company() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(any())).thenReturn(Optional.of(flight));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));

        assertSame(pilotCertification, service.validatePilot(flight, "P-001", tx));
    }

    @Test
    void validatePilot_requires_flight() {
        assertThrows(NullPointerException.class,
                () -> service.validatePilot(null, "P-001", tx));
    }

    @Test
    void ensurePilotMustBelongToRouteCompany() {
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(pilotCompany.iataCode()).thenReturn(IataCode.valueOf("FR"));

        assertThrows(IllegalArgumentException.class,
                () -> service.validatePilot(newFlight(), "P-001", tx));
    }

    @Test
    void ensureInactivePilotIsRejected() {
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(pilotCertification.isActive()).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.validatePilot(newFlight(), "P-001", tx));
    }

    @Test
    void ensurePilotCertificationMustExist() {
        when(pilotCertificationRepository.findByPilotId(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.validatePilot(newFlight(), "P-001", tx));
    }

    @Test
    void registerFlightPlan_persists_plan_with_pasted_dsl() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        final Flight saved = service.registerFlightPlan(
                RegisterFlightPlanTestFixtures.dtoWithDslContent(RegisterFlightPlanTestFixtures.sampleDslText()), tx);

        assertEquals(1, saved.flightPlans().size());
        final FlightPlan plan = saved.flightPlan();
        assertEquals(FlightPlanStatus.DRAFT, plan.status());
        assertNotNull(plan.dslSource());
        verify(flightRepository).save(saved);
    }

    @Test
    void registerFlightPlan_generates_dsl_from_legs_when_content_blank() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        final Flight saved = service.registerFlightPlan(
                RegisterFlightPlanTestFixtures.dtoWithLegs(), tx);

        assertTrue(saved.flightPlan().dslSource().content().contains("flight TP123"));
        assertTrue(saved.flightPlan().dslSource().content().contains("type REGULAR"));
    }

    @Test
    void registerFlightPlan_uses_default_regular_type_when_flight_type_blank() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registerFlightPlan(RegisterFlightPlanTestFixtures.dtoWithLegsDefaultFlightType(), tx);

        assertTrue(flight.flightPlan().dslSource().content().contains("type REGULAR"));
    }

    @Test
    void validateFlight_throws_when_assigned_aircraft_wrong_company() {
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(newFlight()));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(aircraftCompany.iataCode()).thenReturn(IataCode.valueOf("FR"));

        assertThrows(IllegalArgumentException.class,
                () -> service.validateFlight("TP", 123, null, tx));
    }

    @Test
    void registerFlightPlan_treats_whitespace_dsl_as_blank_and_generates_from_legs() {
        final Flight flight = newFlight();
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flight));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        final LocalDateTime dep = LocalDateTime.now().plusDays(1);
        final RegisterFlightPlanDTO dto = new RegisterFlightPlanDTO(
                "TP", 123, null,
                "P-001",
                dep,
                dep.plusHours(1),
                1000, "kg",
                "   ",
                RegisterFlightPlanTestFixtures.dtoWithLegs().legs(),
                "REGULAR"
        );

        service.registerFlightPlan(dto, tx);

        assertTrue(flight.flightPlan().dslSource().content().contains("flight TP123"));
    }

    @Test
    void registerFlightPlan_throws_when_no_dsl_or_legs() {
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(newFlight()));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));

        final RegisterFlightPlanDTO emptyDsl = RegisterFlightPlanTestFixtures.dtoWithDslContent(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.registerFlightPlan(emptyDsl, tx));
    }

    @Test
    void registerFlightPlan_requires_dto() {
        assertThrows(NullPointerException.class, () -> service.registerFlightPlan(null, tx));
    }

    @Test
    void registerFlightPlan_throws_when_new_plan_not_draft() {
        final Flight flightSpy = spy(newFlight());
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(flightSpy));
        when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
        when(pilotCertificationRepository.findByPilotId(any()))
                .thenReturn(Optional.of(pilotCertification));
        final FlightPlan validatedPlan = mock(FlightPlan.class);
        when(validatedPlan.status()).thenReturn(FlightPlanStatus.VALIDATED);
        doReturn(validatedPlan).when(flightSpy).registerOrReplaceFlightPlan(any(), any(), any(), any(), any(), any());

        assertThrows(IllegalStateException.class,
                () -> service.registerFlightPlan(
                        RegisterFlightPlanTestFixtures.dtoWithDslContent(RegisterFlightPlanTestFixtures.sampleDslText()),
                        tx));
    }

    @Test
    void constructor_requires_repository_factory() {
        assertThrows(NullPointerException.class, () -> new RegisterFlightPlanService(null));
    }

    private static void setPlanStatus(final FlightPlan plan, final FlightPlanStatus status) throws Exception {
        final var field = FlightPlan.class.getDeclaredField("status");
        field.setAccessible(true);
        field.set(plan, status);
    }
}
