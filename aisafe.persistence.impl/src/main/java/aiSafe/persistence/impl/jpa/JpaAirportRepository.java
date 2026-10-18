package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.airport.domain.Airport;
import aiSafe.airport.domain.Coordinate_Airport;
import aiSafe.airport.domain.IataCode_Airport;
import aiSafe.airport.domain.IcaoCode_Airport;
import aiSafe.airport.repositories.AirportRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaAirportRepository
        extends JpaAutoTxRepository<Airport, Long, Long>
        implements AirportRepository {

    public JpaAirportRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaAirportRepository(final String puname) {
        super(
                puname,
                Application.settings().getExtendedPersistenceProperties(),
                "id"
        );
    }

    @Override
    public Optional<Airport> findByIataCode(final IataCode_Airport code) {
        final Map<String, Object> params = new HashMap<>();
        params.put("iata", code.value());

        return matchOne(
                "e.iataCode.iataCode = :iata",
                params
        );
    }

    @Override
    public boolean existsByIataCode(final IataCode_Airport code) {

        final Map<String, Object> params = new HashMap<>();
        params.put("iata", code.value());

        return matchOne(
                "e.iataCode.iataCode = :iata",
                params
        ).isPresent();
    }

    @Override
    public boolean existsByIcaoCode(final IcaoCode_Airport code) {

        final Map<String, Object> params = new HashMap<>();
        params.put("icao", code.value());

        return matchOne(
                "e.icaoCode.icaoCode = :icao",
                params
        ).isPresent();
    }

    @Override
    public boolean existsByCoordinate(
            final Coordinate_Airport coordinate
    ) {

        final var query = entityManager().createQuery(
                "SELECT COUNT(a) FROM Airport a " +
                        "WHERE a.coordinate.latitude = :lat " +
                        "AND a.coordinate.longitude = :lon",
                Long.class
        );

        query.setParameter("lat", coordinate.latitude());
        query.setParameter("lon", coordinate.longitude());

        return query.getSingleResult() > 0;
    }
}