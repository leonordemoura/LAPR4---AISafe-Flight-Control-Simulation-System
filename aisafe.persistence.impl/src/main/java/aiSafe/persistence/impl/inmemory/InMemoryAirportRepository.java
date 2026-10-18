package aiSafe.persistence.impl.inmemory;

import aiSafe.airport.domain.Airport;
import aiSafe.airport.domain.Coordinate_Airport;
import aiSafe.airport.domain.IataCode_Airport;
import aiSafe.airport.domain.IcaoCode_Airport;
import aiSafe.airport.repositories.AirportRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryAirportRepository
        extends InMemoryDomainRepository<Airport, Long>
        implements AirportRepository {

    @Override
    public Optional<Airport> findByIataCode(final IataCode_Airport code) {
        return matchOne(airport ->
                airport.iataCode().equals(code)
        );
    }

    @Override
    public boolean existsByIataCode(final IataCode_Airport code) {
        return matchOne(airport ->
                airport.iataCode().equals(code)
        ).isPresent();
    }

    @Override
    public boolean existsByIcaoCode(final IcaoCode_Airport code) {
        return matchOne(airport ->
                airport.icaoCode().equals(code)
        ).isPresent();
    }

    @Override
    public boolean existsByCoordinate(
            final Coordinate_Airport coordinate
    ) {

        return matchOne(airport ->
                airport.coordinate().equals(coordinate)
        ).isPresent();
    }

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends Airport> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final Airport entity) {
        try {
            final Field idField = Airport.class.getDeclaredField("id"); // confirma o nome do field @Id na classe Airport
            idField.setAccessible(true);
            idField.set(entity, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to Airport", ex);
        }
    }
}