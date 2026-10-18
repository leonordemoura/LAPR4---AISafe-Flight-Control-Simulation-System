package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.*;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.User;

import java.util.HashSet;
import java.util.List;

public class AddPilotService {

    private final RepositoryFactory factory;

    public AddPilotService(RepositoryFactory factory) {
        this.factory = factory;
    }

    public PilotCertification execute(
            final String pilotId,
            final String email,
            final List<ModelCertification> certifications,
            final Email collaboratorEmail
    ) {

        final var txCtx = factory.newTransactionalContext();

        final var userRepo = factory.userManagementUsers(txCtx);
        final var aircraftRepo = factory.aircraftModels(txCtx);
        final var pilotRepo = factory.pilots(txCtx);
        final var companyRepo = factory.getAirTransportCompanyRepository(txCtx);

        txCtx.beginTransaction();

        try {

            // =====================================================
            // COMPANY
            // =====================================================
            final var company =
                    companyRepo.findByCollaboratorEmail(collaboratorEmail)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Collaborator not associated with company"
                                    ));

            // =====================================================
            // PILOT ID
            // =====================================================
            final var id = new PilotID(pilotId);

            if (pilotRepo.ofIdentity(id).isPresent()) {
                throw new IllegalArgumentException("Pilot already exists");
            }

            // =====================================================
            // USER
            // =====================================================
            final var emailVO = new Email(email);

            final var user =
                    userRepo.findByEmail(emailVO)
                            .orElseThrow(() ->
                                    new IllegalArgumentException("User does not exist"));

            if (!user.assignedRole().equals(AisafeRoles.PILOT)) {
                throw new IllegalArgumentException("User is not PILOT");
            }

            if (pilotRepo.findByUserEmail(emailVO).isPresent()) {
                throw new IllegalArgumentException("Pilot already registered");
            }

            // =====================================================
            // CERTIFICATIONS
            // =====================================================
            if (certifications == null || certifications.isEmpty()) {
                throw new IllegalArgumentException("Pilot must have at least one certification");
            }

            for (ModelCertification cert : certifications) {
                if (aircraftRepo.ofIdentity(cert.aircraftModelId()).isEmpty()) {
                    throw new IllegalArgumentException(
                            "Aircraft model not found: " + cert.aircraftModelId()
                    );
                }
            }

            // =====================================================
            // DOMAIN
            // =====================================================
            final var pilot = new PilotCertification(
                    id,
                    emailVO,
                    company,
                    new HashSet<>(certifications)
            );

            final var saved = pilotRepo.save(pilot);

            txCtx.commit();

            return saved;

        } catch (Exception e) {
            txCtx.rollback();
            throw e;
        }
    }
}