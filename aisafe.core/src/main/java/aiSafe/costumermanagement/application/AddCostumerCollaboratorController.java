package aiSafe.costumermanagement.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.costumermanagement.domain.Position;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.repositories.UserRepository;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;

@UseCaseController
public class AddCostumerCollaboratorController {
    private final AuthorizationGuardService authorizationGuardService;
    private final RepositoryFactory factory;
    private final MembershipFactory membershipFactory;

    public AddCostumerCollaboratorController() {
        this(new AuthorizationGuardService(), PersistenceContext.repositories(), new MembershipFactory());
    }

    AddCostumerCollaboratorController(final AuthorizationGuardService authorizationGuardService, final RepositoryFactory factory, final MembershipFactory membershipFactory) {
        this.authorizationGuardService = authorizationGuardService;
        this.factory = factory;
        this.membershipFactory = membershipFactory;
    }

    public boolean registerCostumerCollaborator(final Integer type, final String identifier, final String email, final String position){
        authorizationGuardService.ensureAuthenticatedBackofficeUser();

        final TransactionalContext txCtx = factory.newTransactionalContext();

        UserRepository userRepository = factory.userManagementUsers(txCtx);

        txCtx.beginTransaction();
        try {
            Email email1 = new Email(email);
            User systemUser = userRepository.findByEmail(email1)
                    .orElseThrow(() -> new IllegalArgumentException("Collaborator does not exist in the System!"));

            Position position1 = Position.valueOf(position);

            if (type == 1){
                AirTransportCompanyRepository airTransportCompanyRepository = factory.getAirTransportCompanyRepository(txCtx);
                AirTransportCompany company = airTransportCompanyRepository
                        .findByIataCode(IataCode.valueOf(identifier))
                        .orElseThrow(() -> new IllegalArgumentException("Air Transport Company doesn't exist in the system!"));

                membershipFactory.createMembership(company, systemUser, position1);

                airTransportCompanyRepository.save(company);
            }else {
                AirControlAreaRepository airControlAreaRepository = factory.airControlAreas(txCtx);
                AreaCode areaCode = new AreaCode(identifier);
                AirControlArea airControlArea = airControlAreaRepository.findByAreaCode(areaCode).orElseThrow(() -> new IllegalArgumentException("Air Control Area doesn't exist in the system!"));
                membershipFactory.createMembership(airControlArea, systemUser, position1);

                airControlAreaRepository.save(airControlArea);
            }

            txCtx.commit();
            return true;
        } catch (final RuntimeException ex) {
            txCtx.rollback();
            throw ex;
        }
    }
}