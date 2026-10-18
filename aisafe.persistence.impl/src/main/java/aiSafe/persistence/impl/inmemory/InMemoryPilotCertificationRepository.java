package aiSafe.persistence.impl.inmemory;

import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.usermanagement.domain.Email;
import aiSafe.PilotCertification.repositories.PilotCertificationRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public class InMemoryPilotCertificationRepository
        extends InMemoryDomainRepository<PilotCertification, PilotID>
        implements PilotCertificationRepository {

    @Override
    public Optional<PilotCertification> findByPilotId(final PilotID pilotId) {
        return matchOne(e -> e.identity().equals(pilotId));
    }

    @Override
    public Optional<PilotCertification> findByUserEmail(final Email email) {
        return matchOne(e -> e.userEmail().equals(email));
    }

    @Override
    public List<PilotCertification> findByCompany(
            final AirTransportCompany company
    ) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(p -> p.company().equals(company))
                .toList();
    }
}