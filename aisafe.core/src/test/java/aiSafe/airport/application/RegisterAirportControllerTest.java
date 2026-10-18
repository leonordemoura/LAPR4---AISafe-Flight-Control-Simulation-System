package aiSafe.airport.application;

import aiSafe.aircontrolarea.domain.*;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.airport.domain.*;
import aiSafe.airport.repositories.AirportRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterAirportControllerTest {

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private AirportRepository airportRepo;

    @Mock
    private AirControlAreaRepository areaRepo;

    private RegisterAirportController controller;

    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    private AirControlArea validArea;

    @BeforeEach
    void setUp() {

        authzRegistryMock = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        authzRegistryMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.newTransactionalContext())
                .thenReturn(txCtx);

        when(repositoryFactory.airports(txCtx))
                .thenReturn(airportRepo);

        when(repositoryFactory.airControlAreas(txCtx))
                .thenReturn(areaRepo);

        doNothing().when(authz)
                .ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);

        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();

        controller = new RegisterAirportController();

        validArea = new AirControlArea(
                new AreaCode("LPPT"),
                new AreaName("Lisbon FIR"),
                new MinFuelAtArrival(10),
                new MaxAltitude(14000),
                new GeoBoundary(List.of(
                        new Coordinate(0, 0),
                        new Coordinate(0, 10),
                        new Coordinate(10, 10),
                        new Coordinate(10, 0)
                ))
        );
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================
    // REGISTER AIRPORT
    // =========================================================

    @Test
    void ensureRegisterAirportSucceeds() {

        when(airportRepo.existsByIataCode(any()))
                .thenReturn(false);

        when(airportRepo.existsByIcaoCode(any()))
                .thenReturn(false);

        when(airportRepo.existsByCoordinate(any()))
                .thenReturn(false);

        when(areaRepo.findByAreaCode(any()))
                .thenReturn(Optional.of(validArea));

        final Airport savedAirport = mock(Airport.class);

        when(airportRepo.save(any(Airport.class)))
                .thenReturn(savedAirport);

        final Airport result = controller.registerAirport(
                "LIS",
                "LPPT",
                "Lisbon Airport",
                "Lisbon",
                "Portugal",
                "PT",
                114,
                5,
                5,
                "LPPT"
        );

        assertNotNull(result);

        verify(airportRepo).save(any(Airport.class));
    }

    @Test
    void ensureRegisterAirportThrowsWhenIataExists() {

        when(airportRepo.existsByIataCode(any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAirport(
                        "LIS",
                        "LPPT",
                        "Lisbon Airport",
                        "Lisbon",
                        "Portugal",
                        "PT",
                        114,
                        5,
                        5,
                        "LPPT"
                ));
    }

    @Test
    void ensureRegisterAirportThrowsWhenIcaoExists() {

        when(airportRepo.existsByIataCode(any()))
                .thenReturn(false);

        when(airportRepo.existsByIcaoCode(any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAirport(
                        "LIS",
                        "LPPT",
                        "Lisbon Airport",
                        "Lisbon",
                        "Portugal",
                        "PT",
                        114,
                        5,
                        5,
                        "LPPT"
                ));
    }

    @Test
    void ensureRegisterAirportThrowsWhenAreaNotFound() {

        when(airportRepo.existsByIataCode(any()))
                .thenReturn(false);

        when(airportRepo.existsByIcaoCode(any()))
                .thenReturn(false);

        when(areaRepo.findByAreaCode(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAirport(
                        "LIS",
                        "LPPT",
                        "Lisbon Airport",
                        "Lisbon",
                        "Portugal",
                        "PT",
                        114,
                        5,
                        5,
                        "LPPT"
                ));
    }

    @Test
    void ensureRegisterAirportThrowsWhenCoordinatesAlreadyExist() {

        when(airportRepo.existsByIataCode(any()))
                .thenReturn(false);

        when(airportRepo.existsByIcaoCode(any()))
                .thenReturn(false);

        when(areaRepo.findByAreaCode(any()))
                .thenReturn(Optional.of(validArea));

        when(airportRepo.existsByCoordinate(any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAirport(
                        "LIS",
                        "LPPT",
                        "Lisbon Airport",
                        "Lisbon",
                        "Portugal",
                        "PT",
                        114,
                        5,
                        5,
                        "LPPT"
                ));
    }

    @Test
    void ensureRegisterAirportThrowsWhenCoordinatesOutsideArea() {

        when(airportRepo.existsByIataCode(any()))
                .thenReturn(false);

        when(airportRepo.existsByIcaoCode(any()))
                .thenReturn(false);

        when(airportRepo.existsByCoordinate(any()))
                .thenReturn(false);

        when(areaRepo.findByAreaCode(any()))
                .thenReturn(Optional.of(validArea));

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAirport(
                        "LIS",
                        "LPPT",
                        "Lisbon Airport",
                        "Lisbon",
                        "Portugal",
                        "PT",
                        114,
                        50,
                        50,
                        "LPPT"
                ));
    }

    // =========================================================
    // VALIDATE IATA
    // =========================================================

    @Test
    void ensureValidateIataThrowsWhenDuplicateExists() {

        when(airportRepo.existsByIataCode(any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> controller.validateIata("LIS"));
    }

    @Test
    void ensureValidateIataSucceedsWhenUnique() {

        when(airportRepo.existsByIataCode(any()))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                controller.validateIata("LIS"));
    }

    // =========================================================
    // VALIDATE ICAO
    // =========================================================

    @Test
    void ensureValidateIcaoThrowsWhenDuplicateExists() {

        when(airportRepo.existsByIcaoCode(any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> controller.validateIcao("LPPT"));
    }

    @Test
    void ensureValidateIcaoSucceedsWhenUnique() {

        when(airportRepo.existsByIcaoCode(any()))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                controller.validateIcao("LPPT"));
    }

    // =========================================================
    // VALIDATE COORDINATES
    // =========================================================

    @Test
    void ensureValidateCoordinatesSucceedsInsideArea() {

        when(areaRepo.findByAreaCode(any()))
                .thenReturn(Optional.of(validArea));

        assertDoesNotThrow(() ->

                controller.validateCoordinates(
                        5,
                        5,
                        "LPPT"
                ));
    }

    @Test
    void ensureValidateCoordinatesThrowsOutsideArea() {

        when(areaRepo.findByAreaCode(any()))
                .thenReturn(Optional.of(validArea));

        assertThrows(IllegalArgumentException.class, () ->

                controller.validateCoordinates(
                        50,
                        50,
                        "LPPT"
                ));
    }

    @Test
    void ensureValidateCoordinatesThrowsWhenAreaMissing() {

        when(areaRepo.findByAreaCode(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->

                controller.validateCoordinates(
                        5,
                        5,
                        "XXXX"
                ));
    }

    // =========================================================
    // ALL AIR CONTROL AREAS
    // =========================================================

    @Test
    void ensureAllAirControlAreasReturnsRepositoryValues() {

        when(areaRepo.findAll())
                .thenReturn(List.of(validArea));

        final Iterable<AirControlArea> result =
                controller.allAirControlAreas();

        assertNotNull(result);

        verify(areaRepo).findAll();
    }
}