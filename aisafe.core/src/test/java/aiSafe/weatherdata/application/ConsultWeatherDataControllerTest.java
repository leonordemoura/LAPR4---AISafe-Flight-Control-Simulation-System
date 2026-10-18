package aiSafe.weatherdata.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsultWeatherDataControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private ConsultWeatherDataService service;

    private ConsultWeatherDataController controller;

    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    @BeforeEach
    void setUp() {
        authzRegistryMock = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        authzRegistryMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        doNothing().when(authz)
                .ensureAuthenticatedUserHasAnyOf(
                        AisafeRoles.WEATHER_PERSON,
                        AisafeRoles.PILOT,
                        AisafeRoles.FLIGHT_CONTROL_OPERATOR,
                        AisafeRoles.ADMIN
                );

        controller = new ConsultWeatherDataController(authz, service);
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================
    // PUBLIC CONSTRUCTOR
    // =========================================================

    @Test
    void ensurePublicConstructorInitializesController() {
        assertDoesNotThrow(() -> new ConsultWeatherDataController());
    }

    // =========================================================
    // CONSULT WEATHER DATA
    // =========================================================

    @Test
    void ensureConsultWeatherDataReturnsResults() {
        final ConsultWeatherDataDTO dto = mock(ConsultWeatherDataDTO.class);

        when(service.consultWeatherData(eq("LPPT"), any(LocalDate.class)))
                .thenReturn(List.of(dto));

        final List<ConsultWeatherDataDTO> result =
                controller.consultWeatherData("LPPT", LocalDate.of(2025, 6, 1));

        assertNotNull(result);
        assertFalse(result.isEmpty());

        verify(service).consultWeatherData(eq("LPPT"), any(LocalDate.class));
    }

    @Test
    void ensureConsultWeatherDataReturnsEmptyListWhenNoResults() {
        when(service.consultWeatherData(any(), any(LocalDate.class)))
                .thenReturn(List.of());

        final List<ConsultWeatherDataDTO> result =
                controller.consultWeatherData("LPPT", LocalDate.of(2025, 6, 1));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureAuthorizationIsEnforced() {
        when(service.consultWeatherData(any(), any()))
                .thenReturn(List.of());

        controller.consultWeatherData("LPPT", LocalDate.of(2025, 6, 1));

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.WEATHER_PERSON,
                AisafeRoles.PILOT,
                AisafeRoles.FLIGHT_CONTROL_OPERATOR,
                AisafeRoles.ADMIN
        );
    }

    @Test
    void ensureConsultWeatherDataThrowsWhenUnauthorized() {
        doThrow(new RuntimeException("Unauthorized"))
                .when(authz)
                .ensureAuthenticatedUserHasAnyOf(
                        AisafeRoles.WEATHER_PERSON,
                        AisafeRoles.PILOT,
                        AisafeRoles.FLIGHT_CONTROL_OPERATOR,
                        AisafeRoles.ADMIN
                );

        assertThrows(RuntimeException.class, () ->
                controller.consultWeatherData("LPPT", LocalDate.of(2025, 6, 1)));
    }
}