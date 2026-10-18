package aiSafe.PilotCertification.repositories;

import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.List;
import java.util.Optional;

public interface PilotCertificationRepository
        extends DomainRepository<PilotID, PilotCertification> {

    Optional<PilotCertification> findByPilotId(PilotID pilotId);

    Optional<PilotCertification> findByUserEmail(Email email);

    List<PilotCertification> findByCompany(
            AirTransportCompany company
    );
}