package aiSafe.flightRoute.application;

import aiSafe.airport.domain.Airport;
import aiSafe.airport.domain.IataCode_Airport;
import aiSafe.airport.repositories.AirportRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.flightRoute.domain.FlightRoute;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Acceptance tests for the RegisterFlightRouteController use case (US073).
 *
 * <p>Covers the following scenarios:
 * <ul>
 *   <li>Successful registration of a flight route</li>
 *   <li>Rejection when the origin airport does not exist</li>
 *   <li>Rejection when the destination airport does not exist</li>
 *   <li>Rejection when the route name format is invalid</li>
 *   <li>Rejection when the route number is out of range</li>
 *   <li>Rejection when no air transport company matches the route prefix</li>
 *   <li>Rejection when the user is not a collaborator of the company</li>
 *   <li>Rejection when a route with the same name already exists</li>
 *   <li>Successful retrieval of available airports</li>
 * </ul>
 *
 * @author 2DE - Group 3
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterFlightRouteControllerTest {

    // -------------------------------------------------------------------------
    // Infrastructure mocks
    // -------------------------------------------------------------------------

    @Mock private TransactionalContext txCtx;
    @Mock private AuthorizationService authz;
    @Mock private RepositoryFactory repositoryFactory;

    // Repository mocks
    @Mock private AirportRepository airportRepository;
    @Mock private AirTransportCompanyRepository airTransportCompanyRepository;
    @Mock private FlightRouteRepository flightRouteRepository;

    // Auth session mocks
    @Mock private UserSession session;
    @Mock private SystemUser systemUser;
    @Mock private EmailAddress systemEmail;

    // Domain object mocks
    @Mock private AirTransportCompany airTransportCompany;

    // Static mocks
    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    // Subject under test
    private RegisterFlightRouteController controller;

    // -------------------------------------------------------------------------
    // Shared test data
    // -------------------------------------------------------------------------

    private static final String USER_EMAIL_STR  = "collaborator@aisafe.local";
    private static final String ORIGIN_IATA     = "LIS";
    private static final String DESTINATION_IATA = "OPO";
    private static final String VALID_ROUTE_NAME = "TP123";

    private Email userEmail;
    private FlightRouteDTO validDTO;

    // -------------------------------------------------------------------------
    // Setup / Teardown
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
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
        when(repositoryFactory.airports(txCtx)).thenReturn(airportRepository);
        when(repositoryFactory.getAirTransportCompanyRepository(txCtx)).thenReturn(airTransportCompanyRepository);
        when(repositoryFactory.flightRoutes(txCtx)).thenReturn(flightRouteRepository);

        // Auth – role check is a void method; default stub silently passes
        doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        // Auth – session / user email
        when(authz.session()).thenReturn(Optional.of(session));
        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.email()).thenReturn(systemEmail);
        when(systemEmail.toString()).thenReturn(USER_EMAIL_STR);

        // Build controller AFTER stubs are in place (constructor resolves statics)
        controller = new RegisterFlightRouteController();

        // Common domain data
        userEmail = new Email(USER_EMAIL_STR);
        validDTO  = new FlightRouteDTO(VALID_ROUTE_NAME, ORIGIN_IATA, DESTINATION_IATA);
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================================
    // registerFlightRoute — happy path
    // =========================================================================

    @Test
    void registerFlightRoute_succeeds_when_all_data_is_valid() {
        // Arrange
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class)))
                .thenReturn(true);
        when(airTransportCompanyRepository.findByIataCode(any(IataCode.class)))
                .thenReturn(Optional.of(airTransportCompany));
        when(airTransportCompany.hasCollaborator(any(Email.class))).thenReturn(true);
        when(flightRouteRepository.findByRouteName(any(RouteName.class)))
                .thenReturn(Optional.empty());
        when(flightRouteRepository.save(any(FlightRoute.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act & Assert – must not throw
        assertDoesNotThrow(() -> controller.registerFlightRoute(validDTO));

        // Verify the route was actually persisted and the transaction was committed
        verify(flightRouteRepository).save(any(FlightRoute.class));
        verify(txCtx).commit();
        verify(txCtx, never()).rollback();
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    // =========================================================================
    // registerFlightRoute — airport validations
    // =========================================================================

    @Test
    void registerFlightRoute_throws_when_origin_airport_does_not_exist() {
        // Arrange – origin absent, destination present
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class)))
                .thenReturn(false)   // origin
                .thenReturn(true);   // destination (never reached)

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerFlightRoute(validDTO));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void registerFlightRoute_throws_when_destination_airport_does_not_exist() {
        // Arrange – origin present, destination absent
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class)))
                .thenReturn(true)    // origin
                .thenReturn(false);  // destination

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerFlightRoute(validDTO));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void registerFlightRoute_throws_when_origin_and_destination_are_the_same_airport() {
        // Arrange – DTO with identical origin and destination
        final FlightRouteDTO sameAirportDTO = new FlightRouteDTO(VALID_ROUTE_NAME, ORIGIN_IATA, ORIGIN_IATA);

        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);
        when(airTransportCompanyRepository.findByIataCode(any(IataCode.class)))
                .thenReturn(Optional.of(airTransportCompany));
        when(airTransportCompany.hasCollaborator(any(Email.class))).thenReturn(true);
        when(flightRouteRepository.findByRouteName(any(RouteName.class)))
                .thenReturn(Optional.empty());

        // RouteDesignator.valueOf is expected to reject equal origin/destination
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerFlightRoute(sameAirportDTO));

        verify(flightRouteRepository, never()).save(any());
    }

    // =========================================================================
    // registerFlightRoute — route name format validations
    // =========================================================================

    @Test
    void registerFlightRoute_throws_when_route_name_has_invalid_format_lowercase_prefix() {
        // Arrange – lower-case prefix: "tp123" → prefix extracted as "tp", fails [A-Z]{2}
        final FlightRouteDTO dto = new FlightRouteDTO("tp123", ORIGIN_IATA, DESTINATION_IATA);
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerFlightRoute(dto));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void registerFlightRoute_throws_when_route_name_has_no_numeric_suffix() {
        // Arrange – "TPABC" → numeric part "ABC" does not match \d{1,4}
        final FlightRouteDTO dto = new FlightRouteDTO("TPABC", ORIGIN_IATA, DESTINATION_IATA);
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> controller.registerFlightRoute(dto));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void registerFlightRoute_throws_when_route_number_is_zero() {
        // Arrange – "TP0" → routeNumber = 0, out of [1, 9999]
        final FlightRouteDTO dto = new FlightRouteDTO("TP0", ORIGIN_IATA, DESTINATION_IATA);
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> controller.registerFlightRoute(dto));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void registerFlightRoute_throws_when_route_number_exceeds_9999() {
        // Arrange – "TP10000" → 5 digits, fails \d{1,4}
        final FlightRouteDTO dto = new FlightRouteDTO("TP10000", ORIGIN_IATA, DESTINATION_IATA);
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> controller.registerFlightRoute(dto));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void registerFlightRoute_accepts_boundary_route_number_1() {
        // Arrange – minimum valid route number
        final FlightRouteDTO dto = new FlightRouteDTO("TP1", ORIGIN_IATA, DESTINATION_IATA);
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);
        when(airTransportCompanyRepository.findByIataCode(any(IataCode.class)))
                .thenReturn(Optional.of(airTransportCompany));
        when(airTransportCompany.hasCollaborator(any(Email.class))).thenReturn(true);
        when(flightRouteRepository.findByRouteName(any(RouteName.class))).thenReturn(Optional.empty());
        when(flightRouteRepository.save(any(FlightRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> controller.registerFlightRoute(dto));
        verify(flightRouteRepository).save(any(FlightRoute.class));
    }

    @Test
    void registerFlightRoute_accepts_boundary_route_number_9999() {
        // Arrange – maximum valid route number
        final FlightRouteDTO dto = new FlightRouteDTO("TP9999", ORIGIN_IATA, DESTINATION_IATA);
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);
        when(airTransportCompanyRepository.findByIataCode(any(IataCode.class)))
                .thenReturn(Optional.of(airTransportCompany));
        when(airTransportCompany.hasCollaborator(any(Email.class))).thenReturn(true);
        when(flightRouteRepository.findByRouteName(any(RouteName.class))).thenReturn(Optional.empty());
        when(flightRouteRepository.save(any(FlightRoute.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> controller.registerFlightRoute(dto));
        verify(flightRouteRepository).save(any(FlightRoute.class));
    }

    // =========================================================================
    // registerFlightRoute — company / collaborator validations
    // =========================================================================

    @Test
    void registerFlightRoute_throws_when_no_company_matches_route_prefix() {
        // Arrange
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);
        when(airTransportCompanyRepository.findByIataCode(any(IataCode.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerFlightRoute(validDTO));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    @Test
    void registerFlightRoute_throws_when_user_is_not_a_collaborator_of_the_company() {
        // Arrange
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);
        when(airTransportCompanyRepository.findByIataCode(any(IataCode.class)))
                .thenReturn(Optional.of(airTransportCompany));
        when(airTransportCompany.hasCollaborator(any(Email.class))).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.registerFlightRoute(validDTO));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    // =========================================================================
    // registerFlightRoute — uniqueness validation
    // =========================================================================

    @Test
    void registerFlightRoute_throws_when_route_name_already_exists() {
        // Arrange
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class))).thenReturn(true);
        when(airTransportCompanyRepository.findByIataCode(any(IataCode.class)))
                .thenReturn(Optional.of(airTransportCompany));
        when(airTransportCompany.hasCollaborator(any(Email.class))).thenReturn(true);

        final FlightRoute existingRoute = mock(FlightRoute.class);
        when(flightRouteRepository.findByRouteName(any(RouteName.class)))
                .thenReturn(Optional.of(existingRoute));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.registerFlightRoute(validDTO));

        verify(flightRouteRepository, never()).save(any());
        verify(txCtx).rollback();
    }

    // =========================================================================
    // registerFlightRoute — transaction integrity
    // =========================================================================

    @Test
    void registerFlightRoute_rolls_back_and_rethrows_on_unexpected_repository_failure() {
        // Arrange – force a runtime exception from the airport repository
        when(airportRepository.existsByIataCode(any(IataCode_Airport.class)))
                .thenThrow(new RuntimeException("DB connection lost"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> controller.registerFlightRoute(validDTO));

        verify(txCtx).rollback();
        verify(txCtx, never()).commit();
    }

    // =========================================================================
    // registerFlightRoute — authorization
    // =========================================================================

    @Test
    void registerFlightRoute_enforces_air_transport_company_collaborator_role() {
        // Arrange – role check throws to simulate unauthorized access
        doThrow(new RuntimeException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> controller.registerFlightRoute(validDTO));

        // No business logic should have been reached
        verify(airportRepository, never()).existsByIataCode(any());
        verify(flightRouteRepository, never()).save(any());
    }

    // =========================================================================
    // getAvailableAirports
    // =========================================================================

    @Test
    void getAvailableAirports_returns_all_airports_from_repository() {
        // Arrange
        final Airport lisbon = mock(Airport.class);
        final Airport porto  = mock(Airport.class);
        final List<Airport> expected = List.of(lisbon, porto);

        final AirportRepository noTxAirportRepo = mock(AirportRepository.class);
        when(repositoryFactory.airports()).thenReturn(noTxAirportRepo);
        when(noTxAirportRepo.findAll()).thenReturn(expected);

        // Act
        final Iterable<Airport> result = controller.getAvailableAirports();

        // Assert
        assertEquals(expected, result);
        verify(noTxAirportRepo).findAll();
        // Role check must still be enforced for this query method
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    @Test
    void getAvailableAirports_returns_empty_iterable_when_no_airports_registered() {
        // Arrange
        final AirportRepository noTxAirportRepo = mock(AirportRepository.class);
        when(repositoryFactory.airports()).thenReturn(noTxAirportRepo);
        when(noTxAirportRepo.findAll()).thenReturn(List.of());

        // Act
        final Iterable<Airport> result = controller.getAvailableAirports();

        // Assert
        assertFalse(result.iterator().hasNext());
        verify(noTxAirportRepo).findAll();
    }
}