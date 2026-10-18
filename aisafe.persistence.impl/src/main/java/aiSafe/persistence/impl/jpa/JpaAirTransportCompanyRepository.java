package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaAirTransportCompanyRepository extends JpaAutoTxRepository<AirTransportCompany, Long, Long> implements AirTransportCompanyRepository {

    public JpaAirTransportCompanyRepository(final TransactionalContext autoTx) {
        super(autoTx, "companyID");
    }

    public JpaAirTransportCompanyRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "companyID");
    }

    @Override
    public Optional<AirTransportCompany> findByCompanyName(CompanyName companyName) {
        final Map<String, Object> params = new HashMap<>();
        params.put("companyName", companyName.name());
        return matchOne("lower(e.companyName.name) = lower(:companyName)", params);
    }

    @Override
    public Optional<AirTransportCompany> findByIataCode(IataCode iataCode) {
        final Map<String, Object> params = new HashMap<>();
        params.put("iataCode", iataCode.code());
        return matchOne("lower(e.iataCode.code) = lower(:iataCode)", params);
    }

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(IcaoCode icaoCode) {
        final Map<String, Object> params = new HashMap<>();
        params.put("icaoCode", icaoCode.code());
        return matchOne("lower(e.icaoCode.code) = lower(:icaoCode)", params);
    }

    @Override
    public Optional<AirTransportCompany> findByCollaboratorEmail(
            final Email email
    ) {

        final var query = entityManager().createQuery(
                "SELECT c FROM AirTransportCompany c " +
                        "JOIN c.memberships m " +
                        "WHERE m.memberEmail = :email " +
                        "AND m.active = true",
                AirTransportCompany.class
        );

        query.setParameter("email", email);

        return query.getResultStream().findFirst();
    }
}
