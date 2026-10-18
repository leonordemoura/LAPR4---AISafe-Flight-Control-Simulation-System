package aiSafe.flightRoute.application;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.domain.dto.FlightRouteDTO;
import aiSafe.flightRoute.repositories.FlightRouteRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeactivateFlightRouteControllerTest {

    // -------------------------------------------------------------------------
    // Infrastructure mocks
    // -------------------------------------------------------------------------

    @Mock
    private TransactionalContext txCtx;
    @Mock
    private AuthorizationService authz;
    @Mock
    private RepositoryFactory repositoryFactory;

    // Repository mocks
    @Mock
    private FlightRouteRepository flightRouteRepository;
    @Mock
    private AirTransportCompanyRepository airTransportCompanyRepository;
    @Mock
    private FlightRepository flightRepository;

    // Auth session mocks
    @Mock
    private UserSession session;
    @Mock
    private SystemUser systemUser;
    @Mock
    private EmailAddress systemEmail;

    // Domain object mocks
    @Mock
    private AirTransportCompany airTransportCompany;

    // Static mocks
    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    // Subject under test
    private DeactivateFlightRouteController controller;

    private static final String USER_EMAIL_STR = "collaborator@aisafe.local";
    private static final String VALID_ROUTE_NAME = "TP123";

    private FlightRoute activeRoute;

    private FlightRouteDTO validDTO;

    // -------------------------------------------------------------------------
    // Setup / Teardown
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        // Fresh domain object per test — never static, never shared
        activeRoute = new FlightRoute(
                RouteName.valueOf("TP", 123),
                RouteDesignator.valueOf("OPO", "LIS"));

        // Open static mocks
        authzRegistryMock      = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        // Wire static lookups
        authzRegistryMock.when(AuthzRegistry::authorizationService).thenReturn(authz);
        persistenceContextMock.when(PersistenceContext::repositories).thenReturn(repositoryFactory);

        // Transaction setup
        when(repositoryFactory.newTransactionalContext()).thenReturn(txCtx);
        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();
        doNothing().when(txCtx).close();

        // Repository wiring through factory (transactional variants)
        when(repositoryFactory.flightRoutes(txCtx)).thenReturn(flightRouteRepository);
        when(repositoryFactory.getAirTransportCompanyRepository(txCtx)).thenReturn(airTransportCompanyRepository);
        when(repositoryFactory.flights(txCtx)).thenReturn(flightRepository);

        // Auth – role check is a void method; default stub silently passes
        doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        // Auth – session / user email
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.email()).thenReturn(systemEmail);
        when(systemEmail.toString()).thenReturn(USER_EMAIL_STR);

        // Default company behaviour
        when(airTransportCompany.iataCode()).thenReturn(IataCode.valueOf("TP"));
        when(airTransportCompany.hasCollaborator(any(Email.class))).thenReturn(true);

        // Build controller AFTER stubs are in place (constructor resolves statics)
        controller = new DeactivateFlightRouteController();

        // Common domain data
        validDTO = new FlightRouteDTO(VALID_ROUTE_NAME, "OPO", "LIS");
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================================
    // deactivateFlightRoute — happy path
    // =========================================================================

    @Test
    void deactivateFlightRoute_succeeds_when_no_conflicting_plans() {
        // Arrange
        final LocalDateTime deactivationDate = LocalDate.now().plusDays(3).atStartOfDay();

        when(airTransportCompanyRepository.findByIataCode(IataCode.valueOf("TP")))
                .thenReturn(Optional.of(airTransportCompany));
        when(flightRouteRepository.findByRouteName(RouteName.valueOf("TP", 123)))
                .thenReturn(Optional.of(activeRoute));
        when(flightRepository.findByRoute(activeRoute)).thenReturn(List.of());
        when(flightRouteRepository.save(any(FlightRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert – must not throw
        assertDoesNotThrow(() -> controller.deactivateFlightRoute(validDTO, deactivationDate));

        // Verify the route was persisted and the transaction was committed
        verify(flightRouteRepository).save(any(FlightRoute.class));
        verify(txCtx).commit();
        verify(txCtx, never()).rollback();
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    // =========================================================================
    // deactivateFlightRoute — flight plan conflict
    // =========================================================================

    @Test
    void deactivateFlightRoute_throws_when_plan_departs_strictly_after_deactivation_date() {
        // Arrange
        final LocalDateTime deactivationDate = LocalDate.now().plusDays(2).atStartOfDay();
        final Flight conflicting = flightWithPlanAt(deactivationDate.plusDays(1));

        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.of(airTransportCompany));
        when(flightRouteRepository.findByRouteName(any())).thenReturn(Optional.of(activeRoute));
        when(flightRepository.findByRoute(activeRoute)).thenReturn(List.of(conflicting));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.deactivateFlightRoute(validDTO, deactivationDate));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
        verify(txCtx, never()).commit();
    }

    @Test
    void deactivateFlightRoute_succeeds_when_plan_departs_on_or_before_deactivation_date() {
        // Arrange – departure is one hour before the deactivation cut-off
        final LocalDateTime deactivationDate = LocalDate.now().plusDays(5).atStartOfDay();
        final Flight nonConflicting = flightWithPlanAt(deactivationDate.minusHours(1));

        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.of(airTransportCompany));
        when(flightRouteRepository.findByRouteName(any())).thenReturn(Optional.of(activeRoute));
        when(flightRepository.findByRoute(activeRoute)).thenReturn(List.of(nonConflicting));
        when(flightRouteRepository.save(any(FlightRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() -> controller.deactivateFlightRoute(validDTO, deactivationDate));
        verify(flightRouteRepository).save(any(FlightRoute.class));
        verify(txCtx).commit();
    }

    // =========================================================================
    // deactivateFlightRoute — deactivation date validations
    // =========================================================================

    @Test
    void deactivateFlightRoute_throws_when_deactivation_date_is_in_the_past() {
        // Arrange – yesterday is always invalid
        final LocalDateTime past = LocalDate.now().minusDays(1).atStartOfDay();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivateFlightRoute(validDTO, past));

        verify(flightRouteRepository, never()).save(any());
    }

    @Test
    void deactivateFlightRoute_accepts_today_as_deactivation_date() {
        // Arrange – today is a valid boundary (present or future rule)
        final LocalDateTime today = LocalDate.now().atStartOfDay();

        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.of(airTransportCompany));
        when(flightRouteRepository.findByRouteName(any())).thenReturn(Optional.of(activeRoute));
        when(flightRepository.findByRoute(activeRoute)).thenReturn(List.of());
        when(flightRouteRepository.save(any(FlightRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() -> controller.deactivateFlightRoute(validDTO, today));
        verify(flightRouteRepository).save(any(FlightRoute.class));
    }

    // =========================================================================
    // deactivateFlightRoute — route name format validations
    // =========================================================================

    @Test
    void deactivateFlightRoute_throws_when_route_name_has_lowercase_prefix() {
        // Arrange – "tp123" → prefix extracted as "tp", fails [A-Z]{2}
        final FlightRouteDTO dto = new FlightRouteDTO("tp123", "OPO", "LIS");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivateFlightRoute(dto, LocalDate.now().plusDays(1).atStartOfDay()));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void deactivateFlightRoute_throws_when_route_name_has_no_numeric_suffix() {
        // Arrange – "TPABC" → suffix "ABC" does not match \d{1,4}
        final FlightRouteDTO dto = new FlightRouteDTO("TPABC", "OPO", "LIS");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivateFlightRoute(dto, LocalDate.now().plusDays(1).atStartOfDay()));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void deactivateFlightRoute_throws_when_route_number_is_zero() {
        // Arrange – "TP0" → routeNumber = 0, out of [1, 9999]
        final FlightRouteDTO dto = new FlightRouteDTO("TP0", "OPO", "LIS");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivateFlightRoute(dto, LocalDate.now().plusDays(1).atStartOfDay()));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void deactivateFlightRoute_throws_when_route_number_exceeds_9999() {
        // Arrange – "TP10000" → 5 digits, fails \d{1,4}
        final FlightRouteDTO dto = new FlightRouteDTO("TP10000", "OPO", "LIS");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivateFlightRoute(dto, LocalDate.now().plusDays(1).atStartOfDay()));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void deactivateFlightRoute_accepts_boundary_route_number_1() {
        // Arrange – minimum valid route number
        final FlightRouteDTO dto = new FlightRouteDTO("TP1", "OPO", "LIS");
        final FlightRoute route = new FlightRoute(
                RouteName.valueOf("TP", 1), RouteDesignator.valueOf("OPO", "LIS"));

        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.of(airTransportCompany));
        when(flightRouteRepository.findByRouteName(any())).thenReturn(Optional.of(route));
        when(flightRepository.findByRoute(route)).thenReturn(List.of());
        when(flightRouteRepository.save(any(FlightRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() -> controller.deactivateFlightRoute(dto, LocalDate.now().plusDays(1).atStartOfDay()));
        verify(flightRouteRepository).save(any(FlightRoute.class));
    }

    @Test
    void deactivateFlightRoute_accepts_boundary_route_number_9999() {
        // Arrange – maximum valid route number
        final FlightRouteDTO dto = new FlightRouteDTO("TP9999", "OPO", "LIS");
        final FlightRoute route = new FlightRoute(
                RouteName.valueOf("TP", 9999), RouteDesignator.valueOf("OPO", "LIS"));

        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.of(airTransportCompany));
        when(flightRouteRepository.findByRouteName(any())).thenReturn(Optional.of(route));
        when(flightRepository.findByRoute(route)).thenReturn(List.of());
        when(flightRouteRepository.save(any(FlightRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() -> controller.deactivateFlightRoute(dto, LocalDate.now().plusDays(1).atStartOfDay()));
        verify(flightRouteRepository).save(any(FlightRoute.class));
    }

    // =========================================================================
    // deactivateFlightRoute — company / collaborator validations
    // =========================================================================

    @Test
    void deactivateFlightRoute_throws_when_no_company_matches_route_prefix() {
        // Arrange
        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivateFlightRoute(validDTO, LocalDate.now().plusDays(1).atStartOfDay()));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void deactivateFlightRoute_throws_when_user_is_not_a_collaborator_of_the_company() {
        // Arrange
        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.of(airTransportCompany));
        when(airTransportCompany.hasCollaborator(any(Email.class))).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.deactivateFlightRoute(validDTO, LocalDate.now().plusDays(1).atStartOfDay()));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    // =========================================================================
    // deactivateFlightRoute — route existence validation
    // =========================================================================

    @Test
    void deactivateFlightRoute_throws_when_route_does_not_exist() {
        // Arrange
        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.of(airTransportCompany));
        when(flightRouteRepository.findByRouteName(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.deactivateFlightRoute(validDTO, LocalDate.now().plusDays(1).atStartOfDay()));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    // =========================================================================
    // deactivateFlightRoute — transaction integrity
    // =========================================================================

    @Test
    void deactivateFlightRoute_rolls_back_and_rethrows_on_unexpected_repository_failure() {
        // Arrange – force a runtime exception from the company repository
        when(airTransportCompanyRepository.findByIataCode(any()))
                .thenThrow(new RuntimeException("DB connection lost"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> controller.deactivateFlightRoute(validDTO, LocalDate.now().plusDays(1).atStartOfDay()));

        verify(txCtx).rollback();
        verify(txCtx, never()).commit();
    }

    // =========================================================================
    // deactivateFlightRoute — authorization
    // =========================================================================

    @Test
    void deactivateFlightRoute_enforces_air_transport_company_collaborator_role() {
        // Arrange – role check throws to simulate unauthorized access
        doThrow(new RuntimeException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> controller.deactivateFlightRoute(validDTO, LocalDate.now().plusDays(1).atStartOfDay()));

        // No business logic should have been reached
        verify(flightRouteRepository, never()).save(any());
        verify(repositoryFactory, never()).newTransactionalContext();
    }

    // =========================================================================
    // getActiveRoutes
    // =========================================================================

    @Test
    void getActiveRoutes_returns_routes_belonging_to_authenticated_user_company() {
        // Arrange
        final FlightRouteRepository noTxRouteRepo = mock(FlightRouteRepository.class);
        final AirTransportCompanyRepository noTxCompanyRepo = mock(AirTransportCompanyRepository.class);
        final List<FlightRoute> expected = List.of(activeRoute);

        when(repositoryFactory.getAirTransportCompanyRepository()).thenReturn(noTxCompanyRepo);
        when(noTxCompanyRepo.findByCollaboratorEmail(any(Email.class))).thenReturn(Optional.of(airTransportCompany));
        when(repositoryFactory.flightRoutes()).thenReturn(noTxRouteRepo);
        when(noTxRouteRepo.findActiveRoutesByCompanyAndDate(any(), any(LocalDateTime.class))).thenReturn(expected);

        // Act
        final Iterable<FlightRoute> result = controller.getActiveRoutes();

        // Assert
        assertEquals(expected, result);
        verify(noTxRouteRepo).findActiveRoutesByCompanyAndDate(any(), any(LocalDateTime.class));
        // Role check must still be enforced for this query method
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    @Test
    void getActiveRoutes_returns_empty_iterable_when_no_active_routes_exist() {
        // Arrange
        final FlightRouteRepository noTxRouteRepo = mock(FlightRouteRepository.class);
        final AirTransportCompanyRepository noTxCompanyRepo = mock(AirTransportCompanyRepository.class);

        when(repositoryFactory.getAirTransportCompanyRepository()).thenReturn(noTxCompanyRepo);
        when(noTxCompanyRepo.findByCollaboratorEmail(any(Email.class))).thenReturn(Optional.of(airTransportCompany));
        when(repositoryFactory.flightRoutes()).thenReturn(noTxRouteRepo);
        when(noTxRouteRepo.findActiveRoutesByCompanyAndDate(any(), any(LocalDateTime.class))).thenReturn(List.of());

        // Act
        final Iterable<FlightRoute> result = controller.getActiveRoutes();

        // Assert
        assertFalse(result.iterator().hasNext());
        verify(noTxRouteRepo).findActiveRoutesByCompanyAndDate(any(), any(LocalDateTime.class));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Builds a real {@link Flight} on {@link #activeRoute} with a single pending plan
     * whose departure is set to {@code departure}.
     */
    private Flight flightWithPlanAt(final LocalDateTime departure) {
        final Flight flight = new Flight(
                activeRoute,
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA"));
        final DepartureDateTime dep = new DepartureDateTime(departure);
        flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("P-001"),
                dep,
                new ArrivalDateTime(departure.plusHours(1), dep.value()),
                new FuelQuantity(1000, "KG"),
                new DslSource("flight TP123 { type REGULAR leg { departure OPO 2026-12-01 08:00 arrival LIS 2026-12-01 09:00 } }"));
        return flight;
    }
}