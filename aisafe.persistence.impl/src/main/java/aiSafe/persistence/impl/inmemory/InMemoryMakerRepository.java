package aiSafe.persistence.impl.inmemory;

import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.MakerID;
import aiSafe.aircraftmanagement.domain.MakerName;
import aiSafe.aircraftmanagement.repositories.MakerRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryMakerRepository extends InMemoryDomainRepository<Maker, MakerID> implements MakerRepository {
    @Override
    public Optional<Maker> findByID(MakerID makerID) {
        return matchOne(maker -> maker.makerID().equals(makerID));
    }

    @Override
    public Optional<Maker> findByName(final MakerName name) {
        final var target = name.name();
        return matchOne(maker -> maker.name().name().equalsIgnoreCase(target));
    }
}
