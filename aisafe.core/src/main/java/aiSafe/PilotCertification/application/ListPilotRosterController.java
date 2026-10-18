package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.dto.PilotRosterDTO;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;

import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

@UseCaseController
public class ListPilotRosterController {

    private final AuthorizationService authz =
            AuthzRegistry.authorizationService();

    private final RepositoryFactory factory =
            PersistenceContext.repositories();

    public List<PilotRosterDTO> listRoster() {

        // =====================================================
        // AUTHORIZATION
        // =====================================================

        authz.ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );

        // =====================================================
        // REPOSITORIES
        // =====================================================

        final var txCtx =
                factory.newTransactionalContext();

        final var pilotRepo =
                factory.pilots(txCtx);

        final var companyRepo =
                factory.getAirTransportCompanyRepository(txCtx);

        // =====================================================
        // AUTHENTICATED USER
        // =====================================================

        final var authenticatedUser =
                authz.session()
                        .orElseThrow()
                        .authenticatedUser();

        final var collaboratorEmail =
                new Email(
                        authenticatedUser.email().toString()
                );

        // =====================================================
        // COMPANY
        // =====================================================

        final var company =
                companyRepo.findByCollaboratorEmail(
                                collaboratorEmail
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Collaborator is not associated with any company"
                                )
                        );

        // =====================================================
        // FETCH ROSTER
        // =====================================================

        final var pilots =
                pilotRepo.findByCompany(company);

        // =====================================================
        // MAP DTO
        // =====================================================

        return pilots.stream()
                .map(pilot ->
                        new PilotRosterDTO(
                                pilot.identity().toString(),
                                pilot.userEmail().toString(),
                                pilot.isActive(),
                                pilot.certifications()
                                        .stream()
                                        .map(cert ->
                                                cert.aircraftModelId().toString()
                                        )
                                        .toList()
                        )
                )
                .toList();
    }
}