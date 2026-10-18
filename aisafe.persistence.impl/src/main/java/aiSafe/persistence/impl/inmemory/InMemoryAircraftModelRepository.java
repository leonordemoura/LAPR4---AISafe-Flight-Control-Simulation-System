package aiSafe.persistence.impl.inmemory;

import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.aircraftModel.domain.AircraftModelName;
import aiSafe.aircraftModel.repositories.AircraftModelRepository;
import aiSafe.aircraftmanagement.domain.Maker;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAircraftModelRepository
        extends InMemoryDomainRepository<AircraftModel, AircraftModelId>
        implements AircraftModelRepository {

    @Override
    public boolean existsByNameAndMaker(AircraftModelName name, Maker maker) {
        return matchOne(m ->
                m.name().equals(name) && m.maker().equals(maker)
        ).isPresent();
    }

    @Override
    public Optional<AircraftModel> findByName(AircraftModelName name) {
        return matchOne(m -> m.name().equals(name));
    }
}