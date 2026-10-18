package aiSafe.aircraft.repositories;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends DomainRepository<Long, Aircraft> {

    boolean existsByRegistration(String registration);

    Optional<Aircraft> findByRegistration(String registration);

    List<Aircraft> findByCompany(AirTransportCompany company);

    List<Aircraft> findByCompanyAndModel(AirTransportCompany company, String modelName);

    List<Aircraft> findByCompanyAndMaker(AirTransportCompany company, String makerName);

    List<Aircraft> findByCompanyAndMinCapacity(AirTransportCompany company, int minCapacity);

    List<Aircraft> findByCompanyAndMaxCapacity(AirTransportCompany company, int maxCapacity);

    List<Aircraft> findByCompanyAndMaxAge(AirTransportCompany company, int maxAge);
}