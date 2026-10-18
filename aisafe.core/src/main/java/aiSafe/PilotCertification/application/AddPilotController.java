package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.ModelCertification;
import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

public class AddPilotController {

    private final AuthorizationService authz =
            AuthzRegistry.authorizationService();

    private final RepositoryFactory factory =
            PersistenceContext.repositories();

    private final AddPilotService service;

    // =====================================================
    // DEFAULT
    // =====================================================
    public AddPilotController() {
        this.service = new AddPilotService(factory);
    }

    // =====================================================
    // TESTING
    // =====================================================
    public AddPilotController(AddPilotService service) {
        this.service = service;
    }

    public PilotCertification addPilot(
            final String pilotId,
            final String email,
            final List<ModelCertification> certifications
    ) {

        authz.ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );

        final var systemUser =
                authz.session().orElseThrow().authenticatedUser();

        final var collaboratorEmail =
                new Email(systemUser.email().toString());

        return service.execute(
                pilotId,
                email,
                certifications,
                collaboratorEmail
        );
    }
}