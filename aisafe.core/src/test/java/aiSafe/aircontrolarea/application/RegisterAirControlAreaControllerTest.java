package aiSafe.aircontrolarea.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.Coordinate;
import aiSafe.aircontrolarea.domain.GeoBoundary;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers {@link RegisterAirControlAreaController} orchestration paths (AAA, mocked collaborators).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterAirControlAreaControllerTest {

    @Mock
    private TransactionalContext transactionalContext;

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private AirControlAreaRepository airControlAreaRepository;

    private RegisterAirControlAreaController controller;

    private static GeoBoundary rectangle01() {
        return new GeoBoundary(List.of(
                new Coordinate(0, 0),
                new Coordinate(0, 2),
                new Coordinate(2, 2),
                new Coordinate(2, 0)
        ));
    }

    private static GeoBoundary rectangleOverlap() {
        return new GeoBoundary(List.of(
                new Coordinate(1, 1),
                new Coordinate(1, 3),
                new Coordinate(3, 3),
                new Coordinate(3, 1)
        ));
    }

    @BeforeEach
    void setUp() {
        when(repositoryFactory.newTransactionalContext()).thenReturn(transactionalContext);
        when(repositoryFactory.airControlAreas()).thenReturn(airControlAreaRepository);
        when(repositoryFactory.airControlAreas(any(TransactionalContext.class))).thenReturn(airControlAreaRepository);
        doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);

        doNothing().when(transactionalContext).beginTransaction();
        doNothing().when(transactionalContext).commit();
        doNothing().when(transactionalContext).rollback();

        controller = new RegisterAirControlAreaController(authz, repositoryFactory, () -> 14000.0);
    }

    @Test
    void checkAreaCodeExists_throws_when_code_exists() {
        // Arrange
        when(airControlAreaRepository.existsByAreaCode(any())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.checkAreaCodeExists("LPPT"));
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);
    }

    @Test
    void checkAreaCodeExists_passes_when_code_free() {
        // Arrange
        when(airControlAreaRepository.existsByAreaCode(any())).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> controller.checkAreaCodeExists("LPPT"));
    }

    @Test
    void checkAreaNameExists_throws_when_name_exists() {
        // Arrange
        when(airControlAreaRepository.existsByAreaName(any())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> controller.checkAreaNameExists("Lisbon"));
    }

    @Test
    void registerAirControlArea_throws_when_boundary_list_invalid() {
        // Arrange — null
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerAirControlArea("LX", "Lis-X", 10.0, null));

        // Arrange — wrong size (acts as Arrange for second Act)
        final List<GeoBoundary> boundaries = List.of(rectangle01(), rectangle01());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerAirControlArea("LX", "Lis-X", 10.0, boundaries));
    }

    @Test
    void registerAirControlArea_persists_when_valid() {
        // Arrange
        when(airControlAreaRepository.existsByAreaCode(any())).thenReturn(false);
        when(airControlAreaRepository.existsByAreaName(any())).thenReturn(false);
        when(airControlAreaRepository.findAll()).thenReturn(new ArrayList<>());
        final GeoBoundary boundary = rectangle01();
        final AirControlArea persisted = new AirControlArea(
                new aiSafe.aircontrolarea.domain.AreaCode("NX"),
                new aiSafe.aircontrolarea.domain.AreaName("North"),
                new aiSafe.aircontrolarea.domain.MinFuelAtArrival(10),
                new aiSafe.aircontrolarea.domain.MaxAltitude(14000),
                boundary
        );
        when(airControlAreaRepository.save(any(AirControlArea.class))).thenReturn(persisted);

        // Act
        final AirControlArea result =
                controller.registerAirControlArea("NX", "North", 10.0, List.of(boundary));

        // Assert
        assertEquals("NX", result.areaCode().value());
        assertEquals("North", result.areaName().value());
        verify(airControlAreaRepository).save(any(AirControlArea.class));
    }

    @Test
    void registerAirControlArea_throws_when_boundary_overlaps_existing() {
        // Arrange
        final GeoBoundary inner = rectangle01();

        final AirControlArea existing = new AirControlArea(
                new aiSafe.aircontrolarea.domain.AreaCode("EXIST"),
                new aiSafe.aircontrolarea.domain.AreaName("Existing"),
                new aiSafe.aircontrolarea.domain.MinFuelAtArrival(5),
                new aiSafe.aircontrolarea.domain.MaxAltitude(9000),
                inner
        );
        when(airControlAreaRepository.findAll()).thenReturn(List.of(existing));
        when(airControlAreaRepository.existsByAreaCode(any())).thenReturn(false);
        when(airControlAreaRepository.existsByAreaName(any())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerAirControlArea("NEW", "NewArea", 8.0, List.of(rectangleOverlap())));
    }

    @Test
    void parseConfiguredMaxAltitudeMeters_parse_ok() {
        // Arrange & Act & Assert
        assertEquals(1234.5, RegisterAirControlAreaController.parseConfiguredMaxAltitudeMeters("  1234.5  "));
    }

    @Test
    void parseConfiguredMaxAltitudeMeters_throws_when_missing() {
        // Arrange & Act & Assert
        assertThrows(IllegalStateException.class,
                () -> RegisterAirControlAreaController.parseConfiguredMaxAltitudeMeters(null));

        assertThrows(IllegalStateException.class,
                () -> RegisterAirControlAreaController.parseConfiguredMaxAltitudeMeters("   "));
    }

    @Test
    void parseConfiguredMaxAltitudeMeters_throws_on_invalid_number() {
        // Arrange & Act & Assert
        assertThrows(IllegalStateException.class,
                () -> RegisterAirControlAreaController.parseConfiguredMaxAltitudeMeters("not-a-double"));
    }

    @Test
    void default_constructor_wires_authz_registry_when_available() {
        try (MockedStatic<AuthzRegistry> reg = mockStatic(AuthzRegistry.class);
             MockedStatic<PersistenceContext> persistence = mockStatic(PersistenceContext.class)) {
            reg.when(AuthzRegistry::authorizationService).thenReturn(authz);
            persistence.when(PersistenceContext::repositories).thenReturn(repositoryFactory);
            assertDoesNotThrow(() -> new RegisterAirControlAreaController());
        }
    }

    @Test
    void resolveMaxAltitudeFromSettings_reads_configured_meters() {
        assertEquals(14000.0, RegisterAirControlAreaController.resolveMaxAltitudeFromSettings(), 0.001);
    }

    @Test
    void registerAirControlArea_throws_when_area_code_already_exists() {
        when(airControlAreaRepository.existsByAreaCode(any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> controller.registerAirControlArea("LX", "Unique Name", 10.0, List.of(rectangle01())));
    }

    @Test
    void registerAirControlArea_throws_when_area_name_already_exists() {
        when(airControlAreaRepository.existsByAreaCode(any())).thenReturn(false);
        when(airControlAreaRepository.existsByAreaName(any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> controller.registerAirControlArea("NZ", "Taken Name", 10.0, List.of(rectangle01())));
    }
}
