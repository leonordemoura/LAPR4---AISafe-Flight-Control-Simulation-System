package aiSafe.usermanagement.application;

import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthorizationGuardServiceTest {

    @Mock
    private AuthorizationService authz;

    private MockedStatic<AuthzRegistry> authzRegistryMock;

    private AuthorizationGuardService service;

    @BeforeEach
    void setUp() {

        authzRegistryMock = mockStatic(AuthzRegistry.class);

        authzRegistryMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        service = new AuthorizationGuardService();
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
    }

    // =========================================================
    // TEST 1 - default constructor uses AuthzRegistry
    // =========================================================

    @Test
    void ensureDefaultConstructorUsesAuthzRegistry() {

        // service já foi criado no setUp com registry mockado
        service.ensureAuthenticatedAdmin();

        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN);
    }

    // =========================================================
    // TEST 2 - admin authorization
    // =========================================================

    @Test
    void ensureAuthenticatedAdminCallsAuthService() {

        service.ensureAuthenticatedAdmin();

        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN);
    }

    // =========================================================
    // TEST 3 - backoffice authorization
    // =========================================================

    @Test
    void ensureAuthenticatedBackofficeUserCallsAuthService() {

        service.ensureAuthenticatedBackofficeUser();

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.ADMIN,
                AisafeRoles.BACKOFFICE_OPERATOR
        );
    }

    // =========================================================
    // TEST 4 - constructor injection works
    // =========================================================

    @Test
    void ensureInjectedConstructorWorks() {

        AuthorizationGuardService injected =
                new AuthorizationGuardService(authz);

        injected.ensureAuthenticatedAdmin();

        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN);
    }
}