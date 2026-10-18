package aiSafe.usermanagement.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.*;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateAlsafeProfileControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private UserRepository userRepo;

    @Mock
    private User user;

    private UpdateAlsafeProfileController controller;

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
                .thenReturn(userRepo);

        doNothing().when(authz)
                .ensureAuthenticatedUserHasAnyOf(
                        AisafeRoles.ADMIN,
                        AisafeRoles.BACKOFFICE_OPERATOR
                );

        controller = new UpdateAlsafeProfileController();
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================
    // SUCCESS
    // =========================================================

    @Test
    void ensureAttachProfileToUserSucceeds() {

        when(userRepo.ofIdentity(1L))
                .thenReturn(Optional.of(user));

        when(userRepo.save(any(User.class)))
                .thenReturn(user);

        final var clearance = new SecurityClearance(java.time.LocalDate.now().plusDays(10));
        final var assessment = new SkillsAssessment(java.time.LocalDate.now().minusDays(10));

        final var result = controller.attachProfileToUser(
                1L,
                clearance,
                assessment
        );

        assertNotNull(result);

        verify(txCtx).beginTransaction();
        verify(txCtx).commit();
        verify(userRepo).save(any(User.class));
    }

    // =========================================================
    // USER NOT FOUND
    // =========================================================

    @Test
    void ensureAttachProfileThrowsWhenUserNotFound() {

        when(userRepo.ofIdentity(1L))
                .thenReturn(Optional.empty());

        final var clearance = new SecurityClearance(java.time.LocalDate.now().plusDays(10));
        final var assessment = new SkillsAssessment(java.time.LocalDate.now().minusDays(10));

        assertThrows(IllegalArgumentException.class, () ->
                controller.attachProfileToUser(1L, clearance, assessment)
        );

        verify(txCtx).rollback();
    }

    // =========================================================
    // RUNTIME EXCEPTION -> ROLLBACK
    // =========================================================

    @Test
    void ensureRollbackOnRuntimeException() {

        when(userRepo.ofIdentity(1L))
                .thenReturn(Optional.of(user));

        when(userRepo.save(any(User.class)))
                .thenThrow(new RuntimeException("unexpected"));

        final var clearance = new SecurityClearance(java.time.LocalDate.now().plusDays(10));
        final var assessment = new SkillsAssessment(java.time.LocalDate.now().minusDays(10));

        assertThrows(RuntimeException.class, () ->
                controller.attachProfileToUser(1L, clearance, assessment)
        );

        verify(txCtx).rollback();
    }

    // =========================================================
    // LIST USERS
    // =========================================================

    @Test
    void ensureListUsersReturnsRepositoryData() {

        when(userRepo.findAll())
                .thenReturn(java.util.List.of(user));

        final var result = controller.listUsers();

        assertNotNull(result);

        verify(userRepo).findAll();
    }

    // =========================================================
    // AUTHORIZATION CHECK
    // =========================================================

    @Test
    void ensureAuthorizationIsChecked() {

        when(userRepo.ofIdentity(1L))
                .thenReturn(Optional.of(user));

        when(userRepo.save(any(User.class)))
                .thenReturn(user);

        final var clearance = new SecurityClearance(java.time.LocalDate.now().plusDays(10));
        final var assessment = new SkillsAssessment(java.time.LocalDate.now().minusDays(10));

        controller.attachProfileToUser(1L, clearance, assessment);

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.ADMIN,
                AisafeRoles.BACKOFFICE_OPERATOR
        );
    }
}