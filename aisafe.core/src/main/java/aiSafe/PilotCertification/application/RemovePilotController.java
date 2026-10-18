package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RemovePilotController {

    private final AuthorizationService authz =
            AuthzRegistry.authorizationService();

    private final RepositoryFactory factory =
            PersistenceContext.repositories();

    private final RemovePilotService service =
            new RemovePilotService(factory);

    public void deactivatePilot(final String pilotId) {

        authz.ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );

        final var session =
                authz.session().orElseThrow();

        final var email =
                new Email(session.authenticatedUser().email().toString());

        service.deactivatePilot(
                new PilotID(pilotId),
                email
        );
    }
}