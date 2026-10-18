package aiSafe.infrastructure.bootstrap;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.costumermanagement.application.MembershipFactory;
import aiSafe.costumermanagement.domain.Position;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.User;
import aiSafe.usermanagement.repositories.UserRepository;

public class CostumerCollaboratorBootstrapper {
    private final UserRepository userRepository = PersistenceContext.repositories().userManagementUsers();
    private final AirTransportCompanyRepository airTransportCompanyRepository =
            PersistenceContext.repositories().getAirTransportCompanyRepository();
    private final AirControlAreaRepository airControlAreaRepository =
            PersistenceContext.repositories().airControlAreas();
    private final MembershipFactory membershipFactory = new MembershipFactory();

    public void execute() {
        registerCostumerCollaborator(1, "TP", "collaborator@aisafe.local", "Ops Manager");
        registerCostumerCollaborator(2, "LPPT", "fco@aisafe.local", "Tower Operator");
    }

    private void registerCostumerCollaborator(
            final Integer type,
            final String uniqueIdentifier,
            final String email,
            final String position) {
        final Email emailVO = new Email(email);
        final User user = userRepository.findByEmail(emailVO).orElse(null);
        if (user == null) {
            System.out.println("Skipping costumer collaborator " + email + ": User not found.");
            return;
        }

        if (type == 1) {
            final AirTransportCompany airTransportCompany = airTransportCompanyRepository
                    .findByIataCode(IataCode.valueOf(uniqueIdentifier))
                    .orElse(null);
            if (airTransportCompany == null) {
                System.out.println("Skipping costumer collaborator " + email
                        + ": Air Transport Company with IATA code " + uniqueIdentifier + " not found.");
                return;
            }
            if (airTransportCompany.hasCollaborator(emailVO)) {
                System.out.println("Costumer collaborator already registered: " + email);
                return;
            }

            membershipFactory.createMembership(airTransportCompany, user, Position.valueOf(position));

            try {
                airTransportCompanyRepository.save(airTransportCompany);
                System.out.println("Costumer collaborator registered: " + email);
            } catch (final RuntimeException ex) {
                System.out.println("Skipping costumer collaborator " + email + ": " + ex.getMessage());
            }

        } else if (type == 2) {
            final AreaCode areaCode = new AreaCode(uniqueIdentifier);
            final AirControlArea airControlArea = airControlAreaRepository.findByAreaCode(areaCode).orElse(null);
            if (airControlArea == null) {
                System.out.println("Skipping costumer collaborator " + email
                        + ": Air Control Area with code " + uniqueIdentifier + " not found.");
                return;
            }
            if (airControlArea.hasCollaborator(emailVO)) {
                System.out.println("Costumer collaborator already registered: " + email);
                return;
            }

            membershipFactory.createMembership(airControlArea, user, Position.valueOf(position));

            try {
                airControlAreaRepository.save(airControlArea);
                System.out.println("Costumer collaborator registered: " + email);
            } catch (final RuntimeException ex) {
                System.out.println("Skipping costumer collaborator " + email + ": " + ex.getMessage());
            }
        } else {
            System.out.println("Skipping costumer collaborator " + email + ": Invalid customer type.");
        }
    }
}
