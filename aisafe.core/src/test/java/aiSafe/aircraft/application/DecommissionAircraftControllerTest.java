package aiSafe.aircraft.application;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecommissionAircraftControllerTest {

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private Aircraft aircraft;

    @Mock
    private Flight pendingFlight;

    @Mock
    private Flight completedFlight;

    private DecommissionAircraftController controller;

    private static final String VALID_REGISTRATION = "CS-TUA";

    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {
        authzRegistryMock = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        authzRegistryMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.newTransactionalContext()).thenReturn(txCtx);

        when(repositoryFactory.aircrafts(txCtx)).thenReturn(aircraftRepository);
        when(repositoryFactory.flights(txCtx)).thenReturn(flightRepository);

        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();


        doNothing().when(authz)
                .ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);

        controller = new DecommissionAircraftController();
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void stubAircraftFound() {
        when(aircraftRepository.findByRegistration(anyString()))
                .thenReturn(Optional.of(aircraft));
    }

    private void stubNoFlights() {
        when(flightRepository.findByAircraftRegistration(any(AircraftRegistration.class)))
                .thenReturn(List.of());
    }

    private void stubFlights(final List<Flight> flights) {
        when(flightRepository.findByAircraftRegistration(any(AircraftRegistration.class)))
                .thenReturn(flights);
    }

    // =========================================================
    // TESTS — guard clauses
    // =========================================================

    @Test
    void ensureDecommissionThrowsWhenAircraftNotFound() {
        when(aircraftRepository.findByRegistration(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                controller.decommissionAircraft(VALID_REGISTRATION));
    }

    @Test
    void ensureDecommissionThrowsWhenAircraftHasPendingFlights() {
        stubAircraftFound();
        when(pendingFlight.isActiveOrPending()).thenReturn(true);
        stubFlights(List.of(pendingFlight));

        assertThrows(IllegalStateException.class, () ->
                controller.decommissionAircraft(VALID_REGISTRATION));
    }

    @Test
    void ensureDecommissionThrowsWhenAircraftHasActiveFlight() {
        stubAircraftFound();
        when(pendingFlight.isActiveOrPending()).thenReturn(true);
        stubFlights(List.of(completedFlight, pendingFlight));

        assertThrows(IllegalStateException.class, () ->
                controller.decommissionAircraft(VALID_REGISTRATION));
    }

    // =========================================================
    // TESTS — success path
    // =========================================================

    @Test
    void ensureDecommissionSucceedsWithNoFlights() {
        stubAircraftFound();
        stubNoFlights();

        assertDoesNotThrow(() ->
                controller.decommissionAircraft(VALID_REGISTRATION));
    }

    @Test
    void ensureDecommissionSucceedsWhenAllFlightsAreCompleted() {
        stubAircraftFound();
        when(completedFlight.isActiveOrPending()).thenReturn(false);
        stubFlights(List.of(completedFlight));

        assertDoesNotThrow(() ->
                controller.decommissionAircraft(VALID_REGISTRATION));
    }

    @Test
    void ensureDecommissionCallsDecommissionOnAircraft() {
        stubAircraftFound();
        stubNoFlights();

        controller.decommissionAircraft(VALID_REGISTRATION);

        verify(aircraft).decommission();
    }

    @Test
    void ensureDecommissionPersistsAircraftAfterDecommission() {
        stubAircraftFound();
        stubNoFlights();

        controller.decommissionAircraft(VALID_REGISTRATION);

        verify(aircraftRepository).save(aircraft);
    }

    @Test
    void ensureDecommissionPersistsExactlyOnce() {
        stubAircraftFound();
        stubNoFlights();

        controller.decommissionAircraft(VALID_REGISTRATION);

        verify(aircraftRepository, times(1)).save(any(Aircraft.class));
    }

    @Test
    void ensureDecommissionDoesNotSaveWhenAircraftNotFound() {
        when(aircraftRepository.findByRegistration(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                controller.decommissionAircraft(VALID_REGISTRATION));

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void ensureDecommissionDoesNotSaveWhenFlightsArePending() {
        stubAircraftFound();
        when(pendingFlight.isActiveOrPending()).thenReturn(true);
        stubFlights(List.of(pendingFlight));

        assertThrows(IllegalStateException.class, () ->
                controller.decommissionAircraft(VALID_REGISTRATION));

        verify(aircraftRepository, never()).save(any());
    }

    @Test
    void ensureDecommissionDoesNotCallDecommissionWhenFlightsArePending() {
        stubAircraftFound();
        when(pendingFlight.isActiveOrPending()).thenReturn(true);
        stubFlights(List.of(pendingFlight));

        assertThrows(IllegalStateException.class, () ->
                controller.decommissionAircraft(VALID_REGISTRATION));

        verify(aircraft, never()).decommission();
    }

    // =========================================================
    // TESTS — constructor
    // =========================================================

    @Test
    void ensureControllerIsCreatedSuccessfully() {
        assertDoesNotThrow(() ->
                new DecommissionAircraftController());
    }
}