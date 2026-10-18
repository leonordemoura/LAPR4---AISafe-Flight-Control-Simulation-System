package aiSafe.persistence.impl.inmemory;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AreaName;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryAirControlAreaRepository extends InMemoryDomainRepository<AirControlArea, Long>
        implements AirControlAreaRepository {
    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends AirControlArea> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final AirControlArea entity) {
        try {
            final Field idField = AirControlArea.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to AirControlArea", ex);
        }
    }

    @Override
    public boolean existsByAreaCode(final AreaCode areaCode) {
        return matchOne(area -> area.areaCode().equals(areaCode)).isPresent();
    }

    @Override
    public boolean existsByAreaName(final AreaName areaName) {
        return matchOne(area -> area.areaName().equals(areaName)).isPresent();
    }

    @Override
    public Optional<AirControlArea> findByAreaCode(final AreaCode areaCode) {
        return matchOne(area ->
                area.areaCode().equals(areaCode)
        );
    }

    @Override
    public Iterable<AirControlArea> getAirControlAreasBasedOnUser(final Email userEmail) {
        return match(area -> area.hasCollaborator(userEmail));
    }
}
