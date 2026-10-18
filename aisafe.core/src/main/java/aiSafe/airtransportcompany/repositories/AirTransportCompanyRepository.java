package aiSafe.airtransportcompany.repositories;

import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AirTransportCompanyRepository extends DomainRepository<Long, AirTransportCompany> {

    Optional<AirTransportCompany> findByCompanyName(CompanyName companyName);

    Optional<AirTransportCompany> findByIataCode(IataCode iataCode);

    Optional<AirTransportCompany> findByIcaoCode(IcaoCode icaoCode);

    Optional<AirTransportCompany> findByCollaboratorEmail(
            Email email
    );
}
