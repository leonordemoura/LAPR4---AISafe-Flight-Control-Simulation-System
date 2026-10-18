package aiSafe.persistence.impl.inmemory;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class InMemoryAircraftRepository extends InMemoryDomainRepository<Aircraft, Long> implements AircraftRepository {

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends Aircraft> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final Aircraft entity) {
        try {
            final Field idField = Aircraft.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to Aircraft", ex);
        }
    }

    @Override
    public boolean existsByRegistration(final String registration) {
        if (registration == null || registration.isBlank()) {
            return false;
        }
        final String normalized = registration.trim();
        return matchOne(aircraft ->
                aircraft.registration().value().equalsIgnoreCase(normalized)).isPresent();
    }

    @Override
    public Optional<Aircraft> findByRegistration(final String registration) {
        if (registration == null || registration.isBlank()) {
            return Optional.empty();
        }
        final String normalized = registration.trim();
        return matchOne(aircraft ->
                aircraft.registration().value().equalsIgnoreCase(normalized));
    }

    @Override
    public List<Aircraft> findByCompany(final AirTransportCompany company) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(a -> a.company().sameAs(company))
                .collect(Collectors.toList());
    }

    @Override
    public List<Aircraft> findByCompanyAndModel(final AirTransportCompany company, final String modelName) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(a -> a.company().sameAs(company))
                .filter(a -> a.aircraftModel().name().value().equalsIgnoreCase(modelName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Aircraft> findByCompanyAndMaker(final AirTransportCompany company, final String makerName) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(a -> a.company().sameAs(company))
                .filter(a -> a.aircraftModel().maker().name().name().equalsIgnoreCase(makerName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Aircraft> findByCompanyAndMinCapacity(final AirTransportCompany company, final int minCapacity) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(a -> a.company().sameAs(company))
                .filter(a -> a.cabinConfiguration().totalSeats() >= minCapacity)
                .collect(Collectors.toList());
    }

    // US072c — by max capacity
    @Override
    public List<Aircraft> findByCompanyAndMaxCapacity(final AirTransportCompany company, final int maxCapacity) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(a -> a.company().sameAs(company))
                .filter(a -> a.cabinConfiguration().totalSeats() <= maxCapacity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Aircraft> findByCompanyAndMaxAge(final AirTransportCompany company, final int maxAge) {

        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(a -> a.company().sameAs(company))
                .filter(a -> a.age() <= maxAge)
                .collect(Collectors.toList());
    }
}