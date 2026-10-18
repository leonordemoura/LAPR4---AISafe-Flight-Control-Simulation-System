package aiSafe.usermanagement.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.*;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class UpdateAlsafeProfileController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final RepositoryFactory factory = PersistenceContext.repositories();

    public User attachProfileToUser(
            final Long userId,
            final SecurityClearance clearance,
            final SkillsAssessment assessment
    ) {

        authz.ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.ADMIN,
                AisafeRoles.BACKOFFICE_OPERATOR
        );

        final TransactionalContext txCtx = factory.newTransactionalContext();
        final var userRepo = factory.userManagementUsers(txCtx);

        txCtx.beginTransaction();
        try {

            final User user = userRepo.ofIdentity(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            final AlsafeProfile profile = new AlsafeProfile(clearance, assessment);

            user.attachAlsafeProfile(profile);

            final User saved = userRepo.save(user);

            txCtx.commit();
            return saved;

        } catch (RuntimeException e) {
            txCtx.rollback();
            throw e;
        }
    }

    public Iterable<User> listUsers() {

        final TransactionalContext txCtx = factory.newTransactionalContext();
        final var userRepo = factory.userManagementUsers(txCtx);

        return userRepo.findAll();
    }
}
