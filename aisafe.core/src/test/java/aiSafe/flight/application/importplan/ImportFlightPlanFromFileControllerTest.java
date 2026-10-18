package aiSafe.flight.application.importplan;

import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.PilotCertification.repositories.PilotCertificationRepository;
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
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportFlightPlanFromFileControllerTest {

  @Mock private AuthorizationService authz;
  @Mock private RepositoryFactory repositoryFactory;
  @Mock private FlightRepository flightRepository;
  @Mock private PilotCertificationRepository pilotRepository;
  @Mock private AirTransportCompanyRepository airTransportCompanyRepository;
  @Mock private AircraftRepository aircraftRepository;
  @Mock private Aircraft aircraft;
  @Mock private AirTransportCompany company;
  @Mock private UserSession session;
  @Mock private SystemUser authenticatedUser;
  @Mock private EmailAddress emailAddress;

  private ImportFlightPlanFromFileController controller;
  private MockedStatic<AuthzRegistry> authzRegistryMock;
  private MockedStatic<PersistenceContext> persistenceContextMock;

  @BeforeEach
  void setUp() {
    authzRegistryMock = mockStatic(AuthzRegistry.class);
    persistenceContextMock = mockStatic(PersistenceContext.class);

    authzRegistryMock.when(AuthzRegistry::authorizationService).thenReturn(authz);
    persistenceContextMock.when(PersistenceContext::repositories).thenReturn(repositoryFactory);

    when(repositoryFactory.flights()).thenReturn(flightRepository);
    when(repositoryFactory.pilots()).thenReturn(pilotRepository);
    when(repositoryFactory.aircrafts()).thenReturn(aircraftRepository);
    when(repositoryFactory.getAirTransportCompanyRepository()).thenReturn(airTransportCompanyRepository);
    when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(aircraft));
    when(aircraft.isActive()).thenReturn(true);
    when(aircraft.company()).thenReturn(company);
    when(company.iataCode()).thenReturn(IataCode.valueOf("TP"));
    doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.PILOT);
    when(authz.session()).thenReturn(Optional.of(session));
    when(session.authenticatedUser()).thenReturn(authenticatedUser);
    when(authenticatedUser.email()).thenReturn(emailAddress);
    when(emailAddress.toString()).thenReturn("pilot@aisafe.local");
    final PilotCertification pilot = org.mockito.Mockito.mock(PilotCertification.class);
    when(pilot.isActive()).thenReturn(true);
    when(pilot.identity()).thenReturn(new PilotID("PILOT-001"));
    when(pilot.company()).thenReturn(company);
    when(pilotRepository.findByUserEmail(any(Email.class))).thenReturn(Optional.of(pilot));

    controller = new ImportFlightPlanFromFileController();
  }

  @AfterEach
  void tearDown() {
    authzRegistryMock.close();
    persistenceContextMock.close();
  }

  private static Flight flightWithRoute(final String routeSignature) {
    final String[] parts = routeSignature.split("-");

    return new Flight(
            FlightRoute.valueOf(
                    RouteName.valueOf("TP", 123), // pode ser fixo para o teste
                    RouteDesignator.valueOf(parts[0], parts[1])
            ),
            FlightDesignator.of("TP", 123, null),
            FlightType.REGULAR,
            AircraftRegistration.valueOf("CS-TUA")
    );
  }

  private static Flight flightWithName(final String airline, final int number) {
    return new Flight(
            FlightRoute.valueOf(
                    RouteName.valueOf(airline, number),
                    RouteDesignator.valueOf("OPO", "LIS")
            ),
            FlightDesignator.of(airline, number, null),
            FlightType.REGULAR,
            AircraftRegistration.valueOf("CS-TUA")
    );
  }

  @Test
  void ensureValidDslFileIsImportedAndPersisted(@TempDir final Path tempDir) throws IOException {
    final Flight flight = flightWithName("TP", 123);
    when(flightRepository.findByDesignator(FlightDesignator.of("TP", 123,null))).thenReturn(Optional.of(flight));
    when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

    final Path file = tempDir.resolve("plan.flight");
    Files.writeString(file, validDsl(), StandardCharsets.UTF_8);

    final ImportFlightPlanResult result =
        controller.importFlightPlanFromFile(FlightDesignator.of("TP",123,null), file);

    assertTrue(result.isSuccess(), () -> result.formattedErrors().toString());
    assertEquals(FlightPlanStatus.DRAFT, result.flightPlan().orElseThrow().status());
    assertEquals("OPO-LIS", result.flightPlan().orElseThrow().routeSignature());
    assertNotNull(result.flightPlan().orElseThrow().dslSource());
    assertTrue(result.scompJsonPath().isEmpty());
    verify(flightRepository).save(flight);
    verify(flightRepository, never()).delete(any());
  }

  @Test
  void ensureSemanticallyInvalidDslIsRejectedDuringImport() {
    final Flight flight = flightWithRoute("OPO-LIS");
    when(flightRepository.findByDesignator(FlightDesignator.of("TP", 123, null))).thenReturn(Optional.of(flight));

    final ImportFlightPlanResult result = controller.importFlightPlanFromContent(
        FlightDesignator.of("TP", 123, null),
        semanticallyInvalidButParsableDsl(),
        "plan.flight");

    assertFalse(result.isSuccess());
    assertEquals(ImportFlightPlanErrorCode.DSL_VALIDATION, result.errors().getFirst().code());
    verify(flightRepository, never()).save(any());
  }

  @Test
  void ensureInvalidDslDoesNotPersist() {
    final Flight flight = flightWithRoute("OPO-LIS");
    when(flightRepository.findByDesignator(FlightDesignator.of("TP",123, null))).thenReturn(Optional.of(flight));

    final ImportFlightPlanResult result = controller.importFlightPlanFromContent(
        FlightDesignator.of("TP",123,null),
        "flight BROKEN {",
        "plan.flight");

    assertFalse(result.isSuccess());
    assertEquals(ImportFlightPlanErrorCode.DSL_VALIDATION, result.errors().getFirst().code());
    verify(flightRepository, never()).save(any());
  }

  @Test
  void ensureJsonFormatIsRejected() {
    final ImportFlightPlanResult result = controller.importFlightPlanFromContent(FlightDesignator.of("TP",123,null),
        """
            {
              "Leg": [{ "Departure": "OPO" }]
            }
            """,
        "plan.json");

    assertFalse(result.isSuccess());
    assertEquals(ImportFlightPlanErrorCode.UNSUPPORTED_FORMAT, result.errors().getFirst().code());
    verify(flightRepository, never()).save(any());
  }

  @Test
  void ensureUnknownFormatIsRejected() {
    final ImportFlightPlanResult result = controller.importFlightPlanFromContent(FlightDesignator.of("TP",123,null),
        "random text",
        "notes.txt");

    assertFalse(result.isSuccess());
    assertEquals(ImportFlightPlanErrorCode.UNSUPPORTED_FORMAT, result.errors().getFirst().code());
  }

  @Test
  void ensureFlightNotFoundDoesNotPersist() {
    when(flightRepository.findByDesignator(FlightDesignator.of("TP",123,null))).thenReturn(Optional.empty());

    final ImportFlightPlanResult result = controller.importFlightPlanFromContent(
        FlightDesignator.of("TP",123,null),
        validDsl(),
        "plan.flight");

    assertFalse(result.isSuccess());
    assertEquals(ImportFlightPlanErrorCode.FLIGHT_NOT_FOUND, result.errors().getFirst().code());
    verify(flightRepository, never()).save(any());
  }

  @Test
  void ensureRouteMismatchDoesNotPersist() {
    final Flight flight = flightWithRoute("OPO-MAD");
    when(flightRepository.findByDesignator(FlightDesignator.of("TP",123,null))).thenReturn(Optional.of(flight));

    final ImportFlightPlanResult result = controller.importFlightPlanFromContent(
        FlightDesignator.of("TP",123,null),
        validDsl(),
        "plan.flight");

    assertFalse(result.isSuccess());
    assertEquals(ImportFlightPlanErrorCode.ROUTE_MISMATCH, result.errors().getFirst().code());
    verify(flightRepository, never()).save(any());
  }

  @Test
  void ensureFlightIdMismatchIsRejected() {
    final Flight flight = flightWithRoute("OPO-LIS");
    when(flightRepository.findByDesignator(FlightDesignator.of("TP", 123, null))).thenReturn(Optional.of(flight));

    final ImportFlightPlanResult result = controller.importFlightPlanFromContent(
        FlightDesignator.of("TP", 123, null),
        validDsl().replace("TP123", "TP999"),
        "plan.flight");

    assertFalse(result.isSuccess());
    assertEquals(ImportFlightPlanErrorCode.FLIGHT_ID_MISMATCH, result.errors().getFirst().code());
  }

  @Test
  void generateFlightDesignator_returns_designator_when_airline_exists() {
    when(airTransportCompanyRepository.findByIataCode(IataCode.valueOf("TP")))
        .thenReturn(Optional.of(mock(AirTransportCompany.class)));

    final FlightDesignator designator = controller.generateFlightDesignator("TP", 456, "A");

    assertEquals("TP", designator.airlineCode());
    assertEquals(456, designator.flightNumber());
    assertEquals("A", designator.operationalSuffix());
  }

  @Test
  void generateFlightDesignator_throws_when_airline_missing() {
    when(airTransportCompanyRepository.findByIataCode(any())).thenReturn(Optional.empty());
    assertThrows(IllegalStateException.class, () -> controller.generateFlightDesignator("ZZ", 1, null));
  }

  @Test
  void ensureEmptyFileIsRejected(@TempDir final Path tempDir) throws IOException {
    final Path file = tempDir.resolve("empty.flight");
    Files.writeString(file, "   ", StandardCharsets.UTF_8);

    final ImportFlightPlanResult result =
        controller.importFlightPlanFromFile(FlightDesignator.of("TP",123,null), file);

    assertFalse(result.isSuccess());
    assertEquals(ImportFlightPlanErrorCode.EMPTY_FILE, result.errors().getFirst().code());
    verify(flightRepository, never()).save(any());
  }

  private static String semanticallyInvalidButParsableDsl() {
    return """
        flight TP123 {
            type CHARTER
            leg {
                departure OPO 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
                route {
                    origin LIS
                    destination OPO
                }
                segment {
                    start (41.0, -8.0)
                    end (41.0, -8.0)
                    altitude -100 m
                    width 10 km
                    wind 0 deg 0 kt
                }
                fuel 100 kg
            }
        }
        """;
  }

  private static String validDsl() {
    return """
        flight TP123 {
            type REGULAR
            leg {
                departure OPO 2026-05-11 08:00
                arrival LIS 2026-05-11 09:00
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
        """;
  }
}
