package aiSafe.persistence.impl.inmemory;

import aiSafe.aircraftmanagement.domain.FuelType;
import aiSafe.aircraftmanagement.domain.FuelTypeName;
import aiSafe.aircraftmanagement.repositories.FuelTypeRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryFuelTypeRepository extends InMemoryDomainRepository<FuelType, Long> implements FuelTypeRepository {
    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends FuelType> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final FuelType entity) {
        try {
            final Field idField = FuelType.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to FuelType", ex);
        }
    }

    @Override
    public List<FuelType> getAllFuelTypes() {
        final List<FuelType> all = new java.util.ArrayList<>();
        for (final FuelType ft : super.findAll()) {
            all.add(ft);
        }
        return all;
    }

    @Override
    public FuelType findByType(final FuelTypeName type) {
        return matchOne(fuelType -> fuelType.type().equals(type))
                .orElse(null);
    }
}


