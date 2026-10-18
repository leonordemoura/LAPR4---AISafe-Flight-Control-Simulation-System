package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.ModelCertification;
import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraftModel.application.RegisterAircraftModelController;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.airport.domain.Airport;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.User;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddPilotControllerTest {

    @Mock private AuthorizationService authz;
    @Mock private RepositoryFactory repositoryFactory;
    @Mock private AddPilotService service;

    @Mock private User user;
    @Mock private AirTransportCompany company;

    @Mock private UserSession session;
    @Mock private SystemUser systemUser;
    @Mock private EmailAddress email;

    private MockedStatic<AuthzRegistry> authzMock;
    private MockedStatic<PersistenceContext> persistenceMock;


    private AddPilotController controller;

    // =====================================================
    // SETUP
    // =====================================================

    @BeforeEach
    void setUp() {

        authzMock = mockStatic(AuthzRegistry.class);
        persistenceMock = mockStatic(PersistenceContext.class);

        authzMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        controller = new AddPilotController(service);
    }

    @AfterEach
    void tearDown() {
        authzMock.close();
        persistenceMock.close();
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void mockSession(String emailValue) {

        when(authz.session()).thenReturn(Optional.of(session));

        when(session.authenticatedUser())
                .thenReturn(systemUser);

        when(systemUser.email())
                .thenReturn(email);

        when(email.toString())
                .thenReturn(emailValue);
    }

    private ModelCertification validCert() {
        return new ModelCertification(
                new AircraftModelId("A320"),
                LocalDate.of(2025, 1, 1)
        );
    }

    // =====================================================
    // TEST 1 - SUCCESS (UPDATED)
    // =====================================================

    @Test
    void ensurePilotIsCreatedSuccessfully() {

        mockSession("collaborator@aisafe.local");

        when(service.execute(
                any(), any(), any(), any()
        )).thenReturn(mock(PilotCertification.class));

        var result = controller.addPilot(
                "P-001",
                "pilot@aisafe.local",
                List.of(validCert())
        );

        assertNotNull(result);
        verify(service).execute(
                any(), any(), any(), any()
        );
    }

    // =====================================================
    // TEST 2 - AUTH
    // =====================================================

    @Test
    void ensureAuthorizationIsChecked() {

        mockSession("pilot@aisafe.local");

        when(service.execute(any(), any(), any(), any()))
                .thenReturn(mock(PilotCertification.class));

        controller.addPilot("P-002", "pilot@aisafe.local", List.of(validCert()));

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );
    }

    // =====================================================
    // TEST 3 - DELEGATION CHECK
    // =====================================================

    @Test
    void ensureControllerDelegatesToService() {

        mockSession("x@aisafe.local");

        when(service.execute(any(), any(), any(), any()))
                .thenReturn(mock(PilotCertification.class));

        controller.addPilot("P-003", "pilot@aisafe.local", List.of(validCert()));

        verify(service).execute(
                eq("P-003"),
                eq("pilot@aisafe.local"),
                any(),
                any()
        );
    }

    // =====================================================
    // TEST 4 - SESSION MUST EXIST
    // =====================================================

    @Test
    void ensureAuthenticatedSessionMustExist() {

        when(authz.session())
                .thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
                controller.addPilot(
                        "P-004",
                        "pilot@aisafe.local",
                        List.of(validCert())
                )
        );
    }

    // =====================================================
    // TEST 5 - DEFAULT CONSTRUCTOR
    // =====================================================

    @Test
    void ensureDefaultConstructorLoadsSuccessfully() {

        assertDoesNotThrow(() ->
                new AddPilotController());
    }
}