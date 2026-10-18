package aiSafe.persistence.impl.inmemory;

import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryAirTransportCompanyRepository extends InMemoryDomainRepository<AirTransportCompany, Long> implements AirTransportCompanyRepository {
    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends AirTransportCompany> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final AirTransportCompany entity) {
        try {
            final Field idField = AirTransportCompany.class.getDeclaredField("companyID");
            idField.setAccessible(true);
            idField.set(entity, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to EngineModel", ex);
        }
    }


    @Override
    public Optional<AirTransportCompany> findByCompanyName(CompanyName companyName) {
        return matchOne(airTransportCompany -> airTransportCompany.companyName().equals(companyName));
    }

    @Override
    public Optional<AirTransportCompany> findByIataCode(IataCode iataCode) {
        return matchOne(airTransportCompany -> airTransportCompany.iataCode().equals(iataCode));
    }

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(IcaoCode icaoCode) {
        return matchOne(airTransportCompany -> airTransportCompany.icaoCode().equals(icaoCode));
    }

    @Override
    public Optional<AirTransportCompany> findByCollaboratorEmail(
            final Email email
    ) {

        return matchOne(company ->
                company.hasCollaborator(email)
        );
    }

}
