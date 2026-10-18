package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.Email;

public class RemovePilotService {

    private final RepositoryFactory factory;

    public RemovePilotService(final RepositoryFactory factory) {
        this.factory = factory;
    }

    public void deactivatePilot(
            final PilotID id,
            final Email collaboratorEmail
    ) {

        final var txCtx = factory.newTransactionalContext();

        final var pilotRepo = factory.pilots(txCtx);
        final var companyRepo = factory.getAirTransportCompanyRepository(txCtx);
        final var flightRepo = factory.flights(txCtx);

        txCtx.beginTransaction();

        try {

            final var company =
                    companyRepo.findByCollaboratorEmail(collaboratorEmail)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Collaborator not linked to company"
                                    ));

            final var pilot =
                    pilotRepo.findByPilotId(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException("Pilot not found")
                            );

            if (!pilot.company().equals(company)) {
                throw new IllegalArgumentException(
                        "Pilot does not belong to your company"
                );
            }

            if (!pilot.isActive()) {
                throw new IllegalArgumentException(
                        "Pilot is already inactive"
                );
            }

            if (flightRepo.hasActiveFlightsForPilot(id)) {
                throw new IllegalArgumentException(
                        "Cannot deactivate pilot with assigned flight plans"
                );
            }

            pilot.deactivate();
            pilotRepo.save(pilot);

            txCtx.commit();

        } catch (Exception e) {
            txCtx.rollback();
            throw e;
        }
    }
}