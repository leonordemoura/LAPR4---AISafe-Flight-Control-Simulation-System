package aiSafe.airport.repositories;

import aiSafe.airport.domain.Airport;
import aiSafe.airport.domain.Coordinate_Airport;
import aiSafe.airport.domain.IataCode_Airport;
import aiSafe.airport.domain.IcaoCode_Airport;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AirportRepository extends DomainRepository<Long, Airport> {

    Optional<Airport> findByIataCode(IataCode_Airport code);

    boolean existsByIataCode(IataCode_Airport code);

    boolean existsByIcaoCode(IcaoCode_Airport code);

    boolean existsByCoordinate(Coordinate_Airport coordinate);
}
