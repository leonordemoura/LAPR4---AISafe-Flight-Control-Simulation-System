package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.usermanagement.domain.Email;
import aiSafe.PilotCertification.repositories.PilotCertificationRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JpaPilotCertificationRepository
        extends JpaAutoTxRepository<PilotCertification, PilotID, PilotID>
        implements PilotCertificationRepository {

    public JpaPilotCertificationRepository(final TransactionalContext autoTx) {
        super(autoTx, "pilotId");
    }

    public JpaPilotCertificationRepository(final String puname) {
        super(
                puname,
                Application.settings().getExtendedPersistenceProperties(),
                "pilotId"
        );
    }

    @Override
    public Optional<PilotCertification> findByPilotId(final PilotID pilotId) {
        return ofIdentity(pilotId);
    }

    @Override
    public Optional<PilotCertification> findByUserEmail(final Email email) {
        final Map<String, Object> params = new HashMap<>();
        params.put("email", email);

        return matchOne(
                "e.userEmail = :email",
                params
        );
    }

    @Override
    public List<PilotCertification> findByCompany(
            AirTransportCompany company
    ) {

        final TypedQuery<PilotCertification> query =
                entityManager().createQuery(
                        "SELECT p FROM PilotCertification p " +
                                "WHERE p.company = :company",
                        PilotCertification.class
                );

        query.setParameter("company", company);

        return query.getResultList();
    }
}