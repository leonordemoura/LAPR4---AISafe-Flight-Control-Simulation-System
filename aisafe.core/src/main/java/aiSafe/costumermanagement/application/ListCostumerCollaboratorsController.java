package aiSafe.costumermanagement.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.costumermanagement.domain.Membership;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.application.UseCaseController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@UseCaseController
public class ListCostumerCollaboratorsController {

    /**
     * Active collaborator row: {@link User} from {@link UserRepository#findByEmail(Email)} and
     * {@link Membership} from the customer's aggregate (sequence diagram SD-ListCostumerCollaborator).
     */
    public record ActiveCollaborator(User user, Membership membership) {
    }
    private final AuthorizationGuardService authorizationGuardService;
    private final RepositoryFactory factory;

    public ListCostumerCollaboratorsController() {
        this(new AuthorizationGuardService(), PersistenceContext.repositories());
    }

    ListCostumerCollaboratorsController(
            final AuthorizationGuardService authorizationGuardService,
            final RepositoryFactory factory
    ) {
        this.authorizationGuardService = authorizationGuardService;
        this.factory = factory;
    }

    public List<AirTransportCompany> getAirTransportCompanies() {
        authorizationGuardService.ensureAuthenticatedBackofficeUser();
        final AirTransportCompanyRepository companyRepository = factory.getAirTransportCompanyRepository();
        return (List<AirTransportCompany>) companyRepository.findAll();
    }

    public List<AirControlArea> getAirControlAreas() {
        authorizationGuardService.ensureAuthenticatedBackofficeUser();
        final AirControlAreaRepository areaRepository = factory.airControlAreas();
        return (List<AirControlArea>) areaRepository.findAll();
    }

    public List<ActiveCollaborator> listActiveCollaboratorsByCompany(final String iataCode) {
        authorizationGuardService.ensureAuthenticatedBackofficeUser();

        final AirTransportCompanyRepository companyRepository = factory.getAirTransportCompanyRepository();
        final AirTransportCompany company = companyRepository.findByIataCode(IataCode.valueOf(iataCode))
                .orElseThrow(() -> new IllegalArgumentException("Air Transport Company doesn't exist in the system!"));

        return resolveActiveCollaborators(company.memberships());
    }

    public List<ActiveCollaborator> listActiveCollaboratorsByArea(final String areaCode) {
        authorizationGuardService.ensureAuthenticatedBackofficeUser();

        final AirControlAreaRepository areaRepository = factory.airControlAreas();
        final AirControlArea area = areaRepository.findByAreaCode(new AreaCode(areaCode))
                .orElseThrow(() -> new IllegalArgumentException("Air Control Area doesn't exist in the system!"));

        return resolveActiveCollaborators(area.memberships());
    }

    private List<ActiveCollaborator> resolveActiveCollaborators(final List<Membership> memberships) {
        final UserRepository userRepository = factory.userManagementUsers();
        return resolveActiveCollaborators(memberships, userRepository::findByEmail);
    }

    static List<ActiveCollaborator> resolveActiveCollaborators(
            final List<Membership> memberships,
            final Function<Email, Optional<User>> userLookup
    ) {
        final List<ActiveCollaborator> collaborators = new ArrayList<>();

        for (Membership membership : memberships) {
            if (!membership.isActive()) {
                continue;
            }

            final var user = userLookup.apply(membership.memberEmail())
                    .orElseThrow(() -> new IllegalStateException("Collaborator user is missing in the system."));

            collaborators.add(new ActiveCollaborator(user, membership));
        }

        return collaborators;
    }
}
