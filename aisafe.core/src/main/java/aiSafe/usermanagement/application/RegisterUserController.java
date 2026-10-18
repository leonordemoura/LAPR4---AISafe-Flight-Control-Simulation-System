package aiSafe.usermanagement.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.FullName;
import aiSafe.usermanagement.domain.PhoneNumber;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.domain.UserBuilderHelper;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.util.HashSet;
import java.util.Set;

@UseCaseController
public class RegisterUserController {
    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final RepositoryFactory factory = PersistenceContext.repositories();

    /**
     * Backoffice roles that can be assigned by an administrator.
     */
    public Role[] getBackofficeRoleTypes() {
        return new Role[]{AisafeRoles.BACKOFFICE_OPERATOR, AisafeRoles.WEATHER_PERSON,
        AisafeRoles.FLIGHT_CONTROL_OPERATOR, AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR, AisafeRoles.PILOT,
        AisafeRoles.ADMIN};
    }

    public User registerNewUser(final String firstName,
                                final String lastName,
                                final String email,
                                final Role assignedRole,
                                final Integer phoneCode,
                                final Integer phoneNumber,
                                final String password) {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN);

        final var emailVO = new Email(email);
        final var phoneVO = new PhoneNumber(phoneCode, phoneNumber);

        final TransactionalContext txCtx = factory.newTransactionalContext();
        final var domainRepo = factory.userManagementUsers(txCtx);
        final var authzRepo = factory.users(txCtx);

        txCtx.beginTransaction();
        try {
            if (domainRepo.existsByEmail(emailVO)) {
                throw new IllegalArgumentException("Email already in use");
            }
            if (domainRepo.existsByPhoneNumber(phoneVO)) {
                throw new IllegalArgumentException("Phone number already in use");
            }

            // Framework auth record (username == validated email).
            final var builder = UserBuilderHelper.builder();

            try {
                builder.withPassword(password);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(" Password inválida. \nDeve conter:\n- mínimo 6 caracteres \n- 1 número \n- 1 letra maiúscula");
            }
            builder.withUsername(emailVO.value())
                    .withName(firstName, lastName)
                    .withEmail(emailVO.value())
                    .withRoles(new HashSet<>(Set.of(assignedRole)));

            final var savedAuthUser = authzRepo.save(builder.build());


            // Domain aggregate (validated email/domain rules + business fields).
            final var user = new User(new FullName(firstName, lastName), emailVO, phoneVO, assignedRole);
            final var saved = domainRepo.save(user);

            txCtx.commit();
            return saved;
        } catch (final IntegrityViolationException | ConcurrencyException e) {
            txCtx.rollback();
            throw e;
        } catch (final RuntimeException e) {
            txCtx.rollback();
            throw e;
        }
    }
}
