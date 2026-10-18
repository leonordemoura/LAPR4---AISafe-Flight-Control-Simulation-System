package aiSafe.usermanagement.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.*;
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
class RegisterUserControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private UserRepository domainRepo;

    @Mock
    private eapli.framework.infrastructure.authz.domain.repositories.UserRepository authzRepo;

    private RegisterUserController controller;

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

        when(repositoryFactory.newTransactionalContext())
                .thenReturn(txCtx);

        when(repositoryFactory.userManagementUsers(txCtx))
                .thenReturn(domainRepo);

        when(repositoryFactory.users(txCtx))
                .thenReturn(authzRepo);

        doNothing().when(authz)
                .ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN);

        controller = new RegisterUserController();
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================
    // TESTS
    // =========================================================

    @Test
    void ensureGetBackofficeRoleTypesReturnsExpectedRoles() {

        final Role[] roles = controller.getBackofficeRoleTypes();

        assertEquals(6, roles.length);
    }

    @Test
    void ensureRegisterNewUserSucceeds() {

        when(domainRepo.existsByEmail(any()))
                .thenReturn(false);

        when(domainRepo.existsByPhoneNumber(any()))
                .thenReturn(false);

        final User savedUser = mock(User.class);

        when(domainRepo.save(any(User.class)))
                .thenReturn(savedUser);

        final User result = controller.registerNewUser(
                "John",
                "Doe",
                "john.doe@company.com",
                AisafeRoles.ADMIN,
                351,
                912345678,
                "Password1"
        );

        assertNotNull(result);

        verify(txCtx).beginTransaction();
        verify(txCtx).commit();

        verify(domainRepo).save(any(User.class));
        verify(authzRepo).save(any());
    }

    @Test
    void ensureRegisterNewUserThrowsWhenEmailAlreadyExists() {

        when(domainRepo.existsByEmail(any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerNewUser(
                        "John",
                        "Doe",
                        "john.doe@company.com",
                        AisafeRoles.ADMIN,
                        351,
                        912345678,
                        "Password1"
                ));

        verify(txCtx).rollback();
    }

    @Test
    void ensureRegisterNewUserThrowsWhenPhoneAlreadyExists() {

        when(domainRepo.existsByEmail(any()))
                .thenReturn(false);

        when(domainRepo.existsByPhoneNumber(any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerNewUser(
                        "John",
                        "Doe",
                        "john.doe@company.com",
                        AisafeRoles.ADMIN,
                        351,
                        912345678,
                        "Password1"
                ));

        verify(txCtx).rollback();
    }

    @Test
    void ensureRegisterNewUserThrowsWhenPasswordInvalid() {

        when(domainRepo.existsByEmail(any()))
                .thenReturn(false);

        when(domainRepo.existsByPhoneNumber(any()))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerNewUser(
                        "John",
                        "Doe",
                        "john.doe@company.com",
                        AisafeRoles.ADMIN,
                        351,
                        912345678,
                        "abc"
                ));

        verify(txCtx).rollback();
    }

    @Test
    void ensureRegisterNewUserRollsBackOnIntegrityViolation() {

        when(domainRepo.existsByEmail(any()))
                .thenReturn(false);

        when(domainRepo.existsByPhoneNumber(any()))
                .thenReturn(false);

        when(domainRepo.save(any(User.class)))
                .thenThrow(new IntegrityViolationException());

        assertThrows(IntegrityViolationException.class, () ->

                controller.registerNewUser(
                        "John",
                        "Doe",
                        "john.doe@company.com",
                        AisafeRoles.ADMIN,
                        351,
                        912345678,
                        "Password1"
                ));

        verify(txCtx).rollback();
    }

    @Test
    void ensureRegisterNewUserRollsBackOnConcurrencyException() {

        when(domainRepo.existsByEmail(any()))
                .thenReturn(false);

        when(domainRepo.existsByPhoneNumber(any()))
                .thenReturn(false);

        when(domainRepo.save(any(User.class)))
                .thenThrow(new ConcurrencyException("Concurrency error"));

        assertThrows(ConcurrencyException.class, () ->

                controller.registerNewUser(
                        "John",
                        "Doe",
                        "john.doe@company.com",
                        AisafeRoles.ADMIN,
                        351,
                        912345678,
                        "Password1"
                ));

        verify(txCtx).rollback();
    }

    @Test
    void ensureRegisterNewUserRollsBackOnUnexpectedRuntimeException() {

        when(domainRepo.existsByEmail(any()))
                .thenReturn(false);

        when(domainRepo.existsByPhoneNumber(any()))
                .thenReturn(false);

        when(domainRepo.save(any(User.class)))
                .thenThrow(new RuntimeException("unexpected"));

        assertThrows(RuntimeException.class, () ->

                controller.registerNewUser(
                        "John",
                        "Doe",
                        "john.doe@company.com",
                        AisafeRoles.ADMIN,
                        351,
                        912345678,
                        "Password1"
                ));

        verify(txCtx).rollback();
    }
}