package aiSafe.flight.application;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.repositories.FlightRouteRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterFlightControllerTest {

    @Mock
    private TransactionalContext transactionalContext;

    @Mock
    private AuthorizationGuardService authorizationGuardService;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private FlightRouteRepository flightRouteRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AirTransportCompanyRepository airTransportCompanyRepository;

    private RegisterFlightController controller;

    private static final RouteName ROUTE_NAME = RouteName.valueOf("LP", 1);
    private static final RouteDesignator ROUTE_DESIGNATOR = RouteDesignator.valueOf("OPO", "LIS");
    private static final FlightDesignator DESIGNATOR = new FlightDesignator("TP", 123, null);
    private static final FlightType TYPE = FlightType.REGULAR;
    private static final AircraftRegistration REGISTRATION = AircraftRegistration.valueOf("CS-TUA");

    private FlightRoute flightRoute;

    @BeforeEach
    void setUp() {
        flightRoute = FlightRoute.valueOf(ROUTE_NAME, ROUTE_DESIGNATOR);

        doNothing().when(authorizationGuardService).ensureAuthenticatedPilot();

        when(repositoryFactory.newTransactionalContext()).thenReturn(transactionalContext);
        when(repositoryFactory.flightRoutes()).thenReturn(flightRouteRepository);
        when(repositoryFactory.flights(any())).thenReturn(flightRepository);
        when(repositoryFactory.aircrafts(any())).thenReturn(aircraftRepository);
        when(repositoryFactory.getAirTransportCompanyRepository()).thenReturn(airTransportCompanyRepository);

        doNothing().when(transactionalContext).beginTransaction();
        doNothing().when(transactionalContext).commit();
        doNothing().when(transactionalContext).rollback();
        doNothing().when(transactionalContext).close();

        controller = new RegisterFlightController(authorizationGuardService, repositoryFactory);
    }

    // ----------------------------------------------------------------
    // registerFlight
    // ----------------------------------------------------------------

    @Test
    void registerFlight_succeeds_when_all_valid() {
        // Arrange
        when(flightRouteRepository.findByRouteName(ROUTE_NAME)).thenReturn(Optional.of(flightRoute));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.empty());
        when(aircraftRepository.findByRegistration(REGISTRATION.toString())).thenReturn(Optional.of(mock(Aircraft.class)));

        final Flight saved = new Flight(flightRoute, DESIGNATOR, TYPE, REGISTRATION);
        when(flightRepository.save(any(Flight.class))).thenReturn(saved);

        // Act
        final boolean result = controller.registerFlight(flightRoute, DESIGNATOR, TYPE, REGISTRATION);

        // Assert
        assertTrue(result);
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void registerFlight_throws_when_route_not_found() {
        // Arrange
        when(flightRouteRepository.findByRouteName(ROUTE_NAME)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.registerFlight(flightRoute, DESIGNATOR, TYPE, REGISTRATION));

        verify(flightRepository, never()).save(any());
    }

    @Test
    void registerFlight_throws_when_designator_already_exists() {
        // Arrange
        when(flightRouteRepository.findByRouteName(ROUTE_NAME)).thenReturn(Optional.of(mock(FlightRoute.class)));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.of(mock(Flight.class)));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.registerFlight(flightRoute, DESIGNATOR, TYPE, REGISTRATION));

        verify(flightRepository, never()).save(any());
    }

    @Test
    void registerFlight_throws_when_aircraft_not_found() {
        // Arrange
        when(flightRouteRepository.findByRouteName(ROUTE_NAME)).thenReturn(Optional.of(mock(FlightRoute.class)));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.empty());
        when(aircraftRepository.findByRegistration(REGISTRATION.toString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.registerFlight(flightRoute, DESIGNATOR, TYPE, REGISTRATION));

        verify(flightRepository, never()).save(any());
    }

    @Test
    void registerFlight_ensures_pilot_is_authenticated() {
        // Arrange
        when(flightRouteRepository.findByRouteName(ROUTE_NAME)).thenReturn(Optional.of(flightRoute));
        when(flightRepository.findByDesignator(DESIGNATOR)).thenReturn(Optional.empty());
        when(aircraftRepository.findByRegistration(REGISTRATION.toString())).thenReturn(Optional.of(mock(Aircraft.class)));
        when(flightRepository.save(any(Flight.class))).thenReturn(new Flight(flightRoute, DESIGNATOR, TYPE, REGISTRATION));

        // Act
        controller.registerFlight(flightRoute, DESIGNATOR, TYPE, REGISTRATION);

        // Assert
        verify(authorizationGuardService).ensureAuthenticatedPilot();
    }

    // ----------------------------------------------------------------
    // generateFlightDesignator
    // ----------------------------------------------------------------

    @Test
    void generateFlightDesignator_returns_designator_when_airline_exists() {
        // Arrange
        when(airTransportCompanyRepository.findByIataCode(IataCode.valueOf("TP")))
                .thenReturn(Optional.of(mock(AirTransportCompany.class)));

        // Act
        final FlightDesignator result = controller.generateFlightDesignator("TP", 100, null);

        // Assert
        assertNotNull(result);
        assertEquals("TP", result.airlineCode());
        assertEquals(100, result.flightNumber());
    }

    @Test
    void generateFlightDesignator_throws_when_airline_not_found() {
        // Arrange
        when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.generateFlightDesignator("ZZ", 999, null));
    }

    @Test
    void generateFlightDesignator_ensures_pilot_is_authenticated() {
        // Arrange
        when(airTransportCompanyRepository.findByIataCode(IataCode.valueOf("TP")))
                .thenReturn(Optional.of(mock(AirTransportCompany.class)));

        // Act
        controller.generateFlightDesignator("TP", 100, null);

        // Assert
        verify(authorizationGuardService, atLeastOnce()).ensureAuthenticatedPilot();
    }

    @Test
    void default_constructor_wires_dependencies_when_available() {
        try (var persistence = org.mockito.Mockito.mockStatic(aiSafe.infrastructure.persistence.PersistenceContext.class);
             var authzRegistry = org.mockito.Mockito.mockStatic(
                     eapli.framework.infrastructure.authz.application.AuthzRegistry.class)) {
            authzRegistry.when(eapli.framework.infrastructure.authz.application.AuthzRegistry::authorizationService)
                    .thenReturn(org.mockito.Mockito.mock(
                            eapli.framework.infrastructure.authz.application.AuthorizationService.class));
            persistence.when(aiSafe.infrastructure.persistence.PersistenceContext::repositories)
                    .thenReturn(repositoryFactory);
            assertDoesNotThrow(() -> new RegisterFlightController());
        }
    }

    @Test
    void generateFlightDesignator_with_operational_suffix_is_accepted() {
        // Arrange
        when(airTransportCompanyRepository.findByIataCode(IataCode.valueOf("TP")))
                .thenReturn(Optional.of(mock(AirTransportCompany.class)));

        // Act
        final FlightDesignator result = controller.generateFlightDesignator("TP", 200, "A");

        // Assert
        assertNotNull(result);
    }
}