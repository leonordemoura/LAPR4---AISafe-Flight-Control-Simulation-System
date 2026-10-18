package aiSafe.persistence.impl.inmemory;


import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.aircraftmanagement.domain.EngineModelName;
import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEngineModelRepository extends InMemoryDomainRepository<EngineModel, Long> implements EngineModelRepository {
    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends EngineModel> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final EngineModel entity) {
        try {
            final Field idField = EngineModel.class.getDeclaredField("engineModelId");
            idField.setAccessible(true);
            idField.set(entity, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to EngineModel", ex);
        }
    }

    @Override
    public Optional<EngineModel> findByModelNameAndMaker(EngineModelName modelName, Maker maker) {
        return matchOne(engine -> engine.modelName().equals(modelName) && engine.maker().equals(maker));
    }
}



