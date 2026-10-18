package aiSafe.PilotCertification.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;

import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemovePilotControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private UserSession session;

    @Mock
    private SystemUser systemUser;

    @Mock
    private EmailAddress email;

    private MockedStatic<AuthzRegistry> authzMock;
    private MockedStatic<PersistenceContext> persistenceMock;

    private MockedConstruction<RemovePilotService> serviceMock;

    private RemovePilotController controller;

    @BeforeEach
    void setUp() {

        authz = mock(AuthorizationService.class);
        repositoryFactory = mock(RepositoryFactory.class);

        session = mock(UserSession.class);
        systemUser = mock(SystemUser.class);
        email = mock(EmailAddress.class);

        authzMock = mockStatic(AuthzRegistry.class);
        persistenceMock = mockStatic(PersistenceContext.class);

        authzMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        persistenceMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        serviceMock = mockConstruction(RemovePilotService.class);

        controller = new RemovePilotController();
    }

    @AfterEach
    void tearDown() {

        authzMock.close();
        persistenceMock.close();
        serviceMock.close();
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void mockSession(String emailValue) {

        when(authz.session())
                .thenReturn(Optional.of(session));

        when(session.authenticatedUser())
                .thenReturn(systemUser);

        when(systemUser.email())
                .thenReturn(email);

        when(email.toString())
                .thenReturn(emailValue);
    }

    // =====================================================
    // TEST 1 - AUTHORIZATION
    // =====================================================

    @Test
    void ensureAuthorizationIsChecked() {

        mockSession("collab@aisafe.local");

        controller.deactivatePilot("P001");

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );
    }

    // =====================================================
    // TEST 2 - SESSION REQUIRED
    // =====================================================

    @Test
    void ensureAuthenticatedSessionMustExist() {

        when(authz.session())
                .thenReturn(Optional.empty());

        assertThrows(
                Exception.class,
                () -> controller.deactivatePilot("P001")
        );
    }

    // =====================================================
    // TEST 3 - SERVICE INVOCATION
    // =====================================================

    @Test
    void ensureControllerDelegatesToService() {

        mockSession("collab@aisafe.local");

        controller.deactivatePilot("P001");

        final var service =
                serviceMock.constructed().getFirst();

        verify(service).deactivatePilot(
                any(),
                any()
        );
    }

    // =====================================================
    // TEST 4 - CONSTRUCTOR
    // =====================================================

    @Test
    void ensureDefaultConstructorLoadsSuccessfully() {

        assertDoesNotThrow(RemovePilotController::new);
    }
}