package aiSafe.flight.application;

import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.domain.Flight;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterFlightPlanControllerTest {

    @Mock private TransactionalContext tx;
    @Mock private AuthorizationService authz;
    @Mock private RepositoryFactory repositoryFactory;
    @Mock private RegisterFlightPlanService registerFlightPlanService;
    @Mock private Flight flight;
    private AuthorizationGuardService authorizationGuardService;
    private RegisterFlightPlanController controller;

    @BeforeEach
    void setUp() {
        authorizationGuardService = spy(new AuthorizationGuardService(authz));
        doNothing().when(authorizationGuardService).ensureAuthenticatedPilot();
        when(repositoryFactory.newTransactionalContext()).thenReturn(tx);
        doNothing().when(tx).beginTransaction();
        doNothing().when(tx).commit();
        doNothing().when(tx).rollback();
        doNothing().when(tx).close();

        controller = new RegisterFlightPlanController(
                authorizationGuardService, repositoryFactory, registerFlightPlanService);
    }

    @Test
    void validateFlight_delegates_and_commits() {
        when(registerFlightPlanService.validateFlight("TP", 123, null, tx)).thenReturn(flight);

        assertSame(flight, controller.validateFlight("TP", 123, null));

        verify(registerFlightPlanService).validateFlight("TP", 123, null, tx);
        verify(authorizationGuardService).ensureAuthenticatedPilot();
        verify(tx).commit();
        verify(tx).close();
    }

    @Test
    void validateFlight_rolls_back_on_failure() {
        when(registerFlightPlanService.validateFlight(any(), anyInt(), any(), eq(tx)))
                .thenThrow(new IllegalArgumentException("Flight not found."));

        assertThrows(IllegalArgumentException.class,
                () -> controller.validateFlight("TP", 999, null));

        verify(tx).rollback();
        verify(tx, never()).commit();
    }

    @Test
    void validatedFlightAircraftRegistration_requires_flight_validated_first() {
        assertThrows(IllegalStateException.class, controller::validatedFlightAircraftRegistration);
    }

    @Test
    void validatedFlightAircraftRegistration_returns_registration_after_flight_validated() {
        when(flight.aircraftRegistration()).thenReturn(AircraftRegistration.valueOf("CS-TUA"));
        when(registerFlightPlanService.validateFlight(any(), anyInt(), any(), eq(tx))).thenReturn(flight);
        controller.validateFlight("TP", 123, null);

        assertEquals("CS-TUA", controller.validatedFlightAircraftRegistration());
    }

    @Test
    void validatePilot_requires_flight_validated_first() {
        assertThrows(IllegalStateException.class,
                () -> controller.validatePilot("P-001"));
    }

    @Test
    void validatePilot_delegates_after_flight_validated() {
        when(registerFlightPlanService.validateFlight(any(), anyInt(), any(), eq(tx))).thenReturn(flight);
        controller.validateFlight("TP", 123, null);

        controller.validatePilot("P-001");

        verify(registerFlightPlanService).validatePilot(flight, "P-001", tx);
        verify(tx, atLeast(2)).commit();
    }

    @Test
    void validatePilot_rolls_back_on_failure() {
        when(registerFlightPlanService.validateFlight(any(), anyInt(), any(), eq(tx))).thenReturn(flight);
        controller.validateFlight("TP", 123, null);

        doThrow(new IllegalArgumentException("Pilot not active."))
                .when(registerFlightPlanService).validatePilot(flight, "P-001", tx);

        assertThrows(IllegalArgumentException.class,
                () -> controller.validatePilot("P-001"));

        verify(tx, atLeastOnce()).rollback();
    }

    @Test
    void registerFlightPlan_delegates_and_commits() {
        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithDslContent("dsl");
        when(registerFlightPlanService.registerFlightPlan(dto, tx)).thenReturn(flight);

        assertSame(flight, controller.registerFlightPlan(dto));

        verify(registerFlightPlanService).registerFlightPlan(dto, tx);
        verify(tx).commit();
    }

    @Test
    void registerFlightPlan_rolls_back_on_failure() {
        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithLegs();
        when(registerFlightPlanService.registerFlightPlan(dto, tx))
                .thenThrow(new IllegalArgumentException("missing"));

        assertThrows(IllegalArgumentException.class, () -> controller.registerFlightPlan(dto));

        verify(tx).rollback();
    }

    @Test
    void package_constructor_rejects_null_dependencies() {
        assertThrows(NullPointerException.class,
                () -> new RegisterFlightPlanController(null, repositoryFactory, registerFlightPlanService));
        assertThrows(NullPointerException.class,
                () -> new RegisterFlightPlanController(authorizationGuardService, null, registerFlightPlanService));
        assertThrows(NullPointerException.class,
                () -> new RegisterFlightPlanController(authorizationGuardService, repositoryFactory, null));
    }

    @Test
    void default_constructor_wires_dependencies_when_available() {
        try (MockedStatic<AuthzRegistry> authzStatic = mockStatic(AuthzRegistry.class);
             MockedStatic<PersistenceContext> persistence = mockStatic(PersistenceContext.class)) {
            authzStatic.when(AuthzRegistry::authorizationService).thenReturn(authz);
            persistence.when(PersistenceContext::repositories).thenReturn(repositoryFactory);

            assertDoesNotThrow(() -> new RegisterFlightPlanController());
        }
    }
}
